package com.github.sszuev.graphs

import org.apache.jena.graph.Capabilities
import org.apache.jena.graph.Graph
import org.apache.jena.graph.GraphEventManager
import org.apache.jena.graph.Node
import org.apache.jena.graph.TransactionHandler
import org.apache.jena.graph.Triple
import org.apache.jena.shared.AddDeniedException
import org.apache.jena.shared.DeleteDeniedException
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.sparql.graph.GraphWrapper
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.NiceIterator
import org.apache.jena.util.iterator.WrappedIterator
import java.util.ArrayDeque
import java.util.Queue
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream
import kotlin.concurrent.withLock

/**
 * A base for concurrent thread-safe graph-wrapper.
 * Almost each read operation is not blocking (with except of count and contains), producing lazy iterators.
 * This means that iteration is possible even during modification;
 * modification operations collect data over which iteration occurs into snapshots, replacing internal iterator,
 * but try the best to save memory.
 *
 * Note that the method [GraphWrapper.get]
 * and components [TransactionHandler], [Capabilities], [GraphEventManager] are not thread-safe.
 * Complex operation like [org.apache.jena.rdf.model.Model.write] are not thread-safe as well.
 */
sealed class BaseNonBlockingReadGraph(
    base: Graph,
    private val config: ConcurrentGraphConfiguration,
) : GraphWrapper(base), ConcurrentGraph {

    private val openIterators = ConcurrentHashMap<String, OuterTriplesIterator>()

    @Throws(AddDeniedException::class)
    override fun add(triple: Triple) = modify { get().add(triple) }

    @Throws(DeleteDeniedException::class)
    override fun delete(triple: Triple) = modify { get().delete(triple) }

    @Throws(DeleteDeniedException::class)
    override fun remove(s: Node?, p: Node?, o: Node?) = modify { get().remove(s, p, o) }

    @Throws(DeleteDeniedException::class)
    override fun clear() = modify { get().clear() }

    override fun close() = modify { get().close() }

    override fun find(triple: Triple): ExtendedIterator<Triple> = read { remember { get().find(triple) } }

    override fun find(s: Node?, p: Node?, o: Node?): ExtendedIterator<Triple> =
        read { remember { get().find(s, p, o) } }

    override fun find(): ExtendedIterator<Triple> = read { remember { get().find() } }

    override fun stream(s: Node?, p: Node?, o: Node?): Stream<Triple> = read {
        remember { get().stream(s, p, o).asExtendedIterator() }.asStream()
    }

    override fun stream(): Stream<Triple> = read { remember { get().stream().asExtendedIterator() }.asStream() }

    override fun contains(s: Node?, p: Node?, o: Node?): Boolean = read { get().contains(s, p, o) }

    override fun contains(t: Triple): Boolean = read { get().contains(t) }

    override fun isClosed(): Boolean = read { get().isClosed }

    override fun isIsomorphicWith(g: Graph?): Boolean = read { get().isIsomorphicWith(g) }

    override fun isEmpty(): Boolean = read { get().isEmpty }

    override fun size(): Int = read { get().size() }

    override fun dependsOn(other: Graph): Boolean = read { get().dependsOn(other) }

    override fun getTransactionHandler(): TransactionHandler = read { get().transactionHandler }

    override fun getCapabilities(): Capabilities = read { get().capabilities }

    override fun getEventManager(): GraphEventManager = read { get().eventManager }

    abstract override fun getPrefixMapping(): PrefixMapping

    abstract fun <X> read(action: () -> X): X

    abstract fun <X> modify(action: () -> X): X

    /**
     * Creates a new lazy iterator wrapper.
     */
    private fun remember(source: () -> ExtendedIterator<Triple>): ExtendedIterator<Triple> {
        val id = UUID.randomUUID().toString()
        val res = OuterTriplesIterator(
            base = InnerTriplesIterator(source, ArrayDeque()) {
                // For exhausted or closed iterators
                openIterators.remove(id)?.let {
                    it.lock.withLock {
                        it.base = WrappedIterator.emptyIterator()
                        it.lock = NoOpLock
                    }
                }
            },
            lock = ReentrantLock(),
        )
        openIterators[id] = res
        return res
    }

    internal inline fun <X> syncModify(action: () -> X): X {
        val unstartedIterators = mutableListOf<Lock>()
        val processing = mutableListOf<Pair<String, Long>>()
        return try {
            openIterators.forEach {
                if (it.value.startAt == 0L) {
                    processing.add(it.key to it.value.startAt)
                    return@forEach
                }
                it.value.lock.lock()
                if (it.value.startAt == 0L) {
                    it.value.lock.unlock()
                    processing.add(it.key to it.value.startAt)
                } else {
                    // If the iterator is not running yet, we run it after modification
                    unstartedIterators.add(it.value.lock)
                }
            }
            if (config.processOldestFirst) {
                processing.sortBy { it.second }
            }
            // Replace graph-iterators with snapshots.
            // We can't just wait for finish iteration,
            // as someone might not exhaust its iterator.
            // If there are many open iterators in a non-exhausted state,
            // this may lead to performance degradation and increased memory consumption.
            // Also note, we can't use triple-pattern selection,
            // since even if the modification does not affect the iterated data,
            // a ConcurrentModificationException can still happen.
            // We use an iterator queue for processing, which maybe sorted by age of iterators (depending on the parameter `processOldestFirst`).
            // This strategy gives the other iterators time to complete their work in their threads while we process the current one.
            // Collection to the snapshot is also divided on steps (controlled by parameter `chunkSize`) for the same reason,
            // partially collected iterators could finish their work while the next iterator is processing.
            while (processing.isNotEmpty()) {
                val processed = processing.removeAt(0)
                val id = processed.first
                val it = openIterators[id]
                if (it == null || it.base !is InnerTriplesIterator) { // released on close
                    openIterators.remove(id)
                    continue
                }
                it.lock.withLock {
                    val inner = it.base as? InnerTriplesIterator
                    if (inner == null) { // released on close
                        openIterators.remove(id)
                    } else if (!inner.cache(config.iteratorCacheChunkSize)) {
                        it.base = inner.cacheIterator
                        it.lock = NoOpLock
                        openIterators.remove(id)
                    } else {
                        processing.add(processed)
                    }
                }
            }
            action()
        } finally {
            unstartedIterators.forEach { it.unlock() }
        }
    }
}

/**
 * @property [iteratorCacheChunkSize] `Int`, the size of the snapshot fragments;
 * each snapshot is collected iteratively to allow the [OuterTriplesIterator] to move forward a bit
 * between blocked collecting into snapshots;
 * in environments with a high degree of parallelism, this should speed up modification operations
 * @readonly [processOldestFirst] if `true`, then collection into snapshots gives priority to those open Iterators
 * that were created earlier than others; in a highly concurrent environment, this speeds up modification operations
 */
data class ConcurrentGraphConfiguration(val iteratorCacheChunkSize: Int, val processOldestFirst: Boolean) {
    companion object {
        val default = ConcurrentGraphConfiguration(
            iteratorCacheChunkSize = 1024,
            processOldestFirst = false,
        )
    }
}

/**
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation.
 */
internal class InnerTriplesIterator(
    val source: () -> ExtendedIterator<Triple>,
    private val cache: Queue<Triple>,
    val onClose: () -> Unit,
) : NiceIterator<Triple>() {

    val cacheIterator: Iterator<Triple> = cache.erasingIterator()
    private var baseIterator: ExtendedIterator<Triple>? = null
    private var closed = false

    private fun base(): ExtendedIterator<Triple> {
        if (baseIterator == null) {
            baseIterator = source()
        }
        return checkNotNull(baseIterator)
    }

    private fun invokeOnClose() {
        if (closed) {
            return
        }
        onClose()
        closed = true
    }

    fun cache(size: Int): Boolean {
        val base = base()
        var count = 0
        while (count++ < size && base.hasNext()) {
            cache.add(base.next())
        }
        return base.hasNext()
    }

    override fun hasNext(): Boolean = try {
        if (cacheIterator.hasNext()) {
            true
        } else {
            val res = base().hasNext()
            if (!res) {
                onClose()
            }
            res
        }
    } catch (ex: Exception) {
        invokeOnClose()
        throw ex
    }

    override fun next(): Triple = try {
        cacheIterator.nextOrNull() ?: base().next()
    } catch (ex: Exception) {
        invokeOnClose()
        throw ex
    }

    override fun close() {
        cache.clear()
        baseIterator?.close()
        invokeOnClose()
    }

}

/**
 * An [ExtendedIterator] wrapper for triples that allows to substitute the base iterator.
 * Synchronized by [Lock].
 * Has no reference to the graph when it is released ([base] replaced by snapshot)
 */
internal class OuterTriplesIterator(
    @Volatile var base: Iterator<Triple>,
    @Volatile var lock: Lock,
) : NiceIterator<Triple>() {

    @Volatile
    var startAt: Long = 0

    override fun hasNext(): Boolean = lock.withLock {
        if (startAt != 0L) startAt = System.currentTimeMillis()
        base.hasNext()
    }

    override fun next(): Triple = lock.withLock {
        if (startAt != 0L) startAt = System.currentTimeMillis()
        base.next()
    }

    override fun close() = lock.withLock { close(base) }

}

/**
 * A dummy [Lock] that does nothing.
 * When iterator is realised to snapshot we replace [ReentrantLock] with this one since is no longer needed.
 */
internal object NoOpLock : Lock {
    override fun lock() {
    }

    override fun lockInterruptibly() {
    }

    override fun tryLock(): Boolean {
        return true
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        return true
    }

    override fun unlock() {
    }

    override fun newCondition(): Condition {
        throw IllegalStateException()
    }
}