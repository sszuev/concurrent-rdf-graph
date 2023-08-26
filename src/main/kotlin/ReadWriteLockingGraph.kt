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
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Stream
import kotlin.concurrent.withLock

/**
 * Thread-safe [GraphWrapper] which use [Lock]s for synchronization.
 *
 * Note that the method [GraphWrapper.get]
 * and components [TransactionHandler], [Capabilities], [GraphEventManager] are not thread-safe.
 * Complex operation like [org.apache.jena.rdf.model.Model.write] are not thread-safe as well.
 */
class ReadWriteLockingGraph(
    graph: Graph,
    private val readLock: Lock,
    private val writeLock: Lock,
    private val iteratorCacheChunkSize: Int,
) : GraphWrapper(graph), Graph {

    constructor(
        graph: Graph,
        lock: ReadWriteLock = ReentrantReadWriteLock(),
        iteratorCacheChunkSize: Int = 1024,
    ) : this(
        graph = graph,
        readLock = lock.readLock(),
        writeLock = lock.writeLock(),
        iteratorCacheChunkSize = iteratorCacheChunkSize
    )

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

    override fun getTransactionHandler(): TransactionHandler = get().transactionHandler

    override fun getCapabilities(): Capabilities = get().capabilities

    override fun getEventManager(): GraphEventManager = get().eventManager

    override fun getPrefixMapping(): PrefixMapping =
        ReadWriteLockingPrefixMapping(get().prefixMapping, readLock, writeLock)

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

    private inline fun <X> read(action: () -> X): X = readLock.withLock(action)

    private inline fun <X> modify(action: () -> X): X = writeLock.withLock {
        val unstartedIterators = mutableListOf<Lock>()
        val processing = mutableListOf<String>()
        try {
            openIterators.forEach {
                if (it.value.started) {
                    processing.add(it.key)
                    return@forEach
                }
                it.value.lock.lock()
                if (it.value.started) {
                    it.value.lock.unlock()
                    processing.add(it.key)
                } else {
                    // If the iterator is not running yet, we run it after modification
                    unstartedIterators.add(it.value.lock)
                }
            }
            // Replace graph-iterators with snapshots.
            // We can't just wait for finish iteration,
            // as someone might not close or not exhaust this inner graph-iterator.
            // If this a case than performance and memory consumption might be even worse
            // than for transactional graphs.
            // Also, we can't use triple-pattern selection,
            // since even if the modification does not affect the iterated data,
            // a ConcurrentModificationException can still happen
            while (processing.isNotEmpty()) {
                val id = processing.removeAt(0)
                val it = openIterators[id]
                if (it == null || it.base !is InnerTriplesIterator) { // released on close
                    openIterators.remove(id)
                    continue
                }
                it.lock.withLock {
                    val inner = it.base as? InnerTriplesIterator
                    if (inner == null) { // released on close
                        openIterators.remove(id)
                    } else if (!inner.cache(iteratorCacheChunkSize)) {
                        it.base = inner.cacheIterator
                        it.lock = NoOpLock
                        openIterators.remove(id)
                    } else {
                        processing.add(id)
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
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation.
 */
private class InnerTriplesIterator(
    val source: () -> ExtendedIterator<Triple>,
    val cache: Queue<Triple>,
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
private class OuterTriplesIterator(
    @Volatile var base: Iterator<Triple>,
    @Volatile var lock: Lock,
) : NiceIterator<Triple>() {

    @Volatile
    var started = false

    override fun hasNext(): Boolean = lock.withLock {
        started = true
        base.hasNext()
    }

    override fun next(): Triple = lock.withLock {
        started = true
        base.next()
    }

    override fun close() = lock.withLock { close(base) }
}

/**
 * A dummy [Lock] that does nothing.
 * When iterator is realised to snapshot we replace [ReentrantLock] with this one since is no longer needed.
 */
private object NoOpLock : Lock {
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