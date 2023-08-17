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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
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
) : GraphWrapper(graph), Graph {

    constructor(graph: Graph, lock: ReadWriteLock) : this(
        graph = graph,
        readLock = lock.readLock(),
        writeLock = lock.writeLock()
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

    override fun getPrefixMapping(): PrefixMapping = LockingPrefixMapping(get().prefixMapping, readLock, writeLock)

    private inline fun <X> modify(action: () -> X): X = writeLock.withLock {
        val unstartedIterators = hashMapOf<String, Lock>()
        openIterators.forEach {
            it.value.lock.lock()
            if (it.value.started) {
                it.value.lock.unlock()
            } else {
                // If the iterator is not running yet, we run it after modification
                unstartedIterators[it.key] = it.value.lock
            }
        }
        try {
            // Replace graph-iterators with snapshots.
            // We can't just wait for finish iteration,
            // as someone might not close or not exhaust this inner graph-iterator.
            // If this a case than performance and memory consumption might be even worse
            // than for transactional graphs.
            // Also, we can't use triple-pattern selection,
            // since even if the modification does not affect the iterated data,
            // a ConcurrentModificationException can still happen
            openIterators.keys().asIterator().forEach { id ->
                if (!unstartedIterators.containsKey(id)) {
                    releaseToSnapshot(id)
                }
            }
            action()
        } finally {
            unstartedIterators.values.forEach { it.unlock() }
        }
    }

    private inline fun <X> read(action: () -> X): X = readLock.withLock(action)

    /**
     * Creates a new lazy iterator wrapper.
     */
    private fun remember(source: () -> ExtendedIterator<Triple>): ExtendedIterator<Triple> {
        val id = UUID.randomUUID().toString()
        val res = OuterTriplesIterator(
            base = InnerTriplesIterator(source) { releaseToNull(id) },
            lock = ReentrantLock(),
        )
        openIterators[id] = res
        return res
    }

    /**
     * Replaces the base iterator with the snapshot iterator.
     */
    private fun releaseToSnapshot(id: String) = openIterators.remove(id)?.let { wrapper ->
        wrapper.lock.withLock {
            val inner = wrapper.base as InnerTriplesIterator
            val rest = inner.collect { ArrayDeque() }
            wrapper.base = rest.erasingIterator()
            wrapper.lock = NoOpLock
        }
    }

    /**
     * For exhausted or closed iterators.
     */
    private fun releaseToNull(id: String) = openIterators.remove(id)?.let { wrapper ->
        wrapper.lock.withLock {
            wrapper.base = WrappedIterator.emptyIterator()
            wrapper.lock = NoOpLock
        }
    }
}

/**
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation (see [ReadWriteLockingGraph.releaseToNull]).
 */
private class InnerTriplesIterator(
    val source: () -> ExtendedIterator<Triple>,
    val onClose: () -> Unit,
) : NiceIterator<Triple>() {

    var base: ExtendedIterator<Triple>? = null

    private fun base(): ExtendedIterator<Triple> {
        if (base == null) {
            base = source()
        }
        return checkNotNull(base)
    }

    override fun hasNext(): Boolean = try {
        val res = base().hasNext()
        if (!res) {
            onClose()
        }
        res
    } catch (ex: Exception) {
        onClose()
        throw ex
    }

    override fun next(): Triple = try {
        base().next()
    } catch (ex: Exception) {
        onClose()
        throw ex
    }

    override fun close() {
        base?.close()
        onClose()
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

class LockingPrefixMapping(
    private val pm: PrefixMapping,
    private val readLock: Lock,
    private val writeLock: Lock,
) : PrefixMapping {

    override fun setNsPrefix(prefix: String, uri: String): PrefixMapping = writeLock.withLock {
        also { pm.setNsPrefix(prefix, uri) }
    }

    override fun removeNsPrefix(prefix: String): PrefixMapping = writeLock.withLock {
        also { pm.removeNsPrefix(prefix) }
    }

    override fun clearNsPrefixMap(): PrefixMapping = writeLock.withLock {
        also { pm.clearNsPrefixMap() }
    }

    override fun setNsPrefixes(other: PrefixMapping): PrefixMapping = writeLock.withLock {
        also { pm.setNsPrefixes(other) }
    }

    override fun setNsPrefixes(map: Map<String, String>): PrefixMapping = writeLock.withLock {
        pm.setNsPrefixes(map)
    }

    override fun withDefaultMappings(map: PrefixMapping): PrefixMapping = writeLock.withLock {
        also { pm.withDefaultMappings(pm) }
    }

    override fun getNsPrefixURI(prefix: String): String = readLock.withLock {
        pm.getNsPrefixURI(prefix)
    }

    override fun getNsURIPrefix(uri: String): String = readLock.withLock {
        pm.getNsPrefixURI(uri)
    }

    override fun getNsPrefixMap(): Map<String, String> = readLock.withLock {
        pm.nsPrefixMap
    }

    override fun expandPrefix(prefixed: String): String = readLock.withLock {
        pm.expandPrefix(prefixed)
    }

    override fun shortForm(uri: String): String = readLock.withLock {
        pm.shortForm(uri)
    }

    override fun qnameFor(uri: String): String = readLock.withLock {
        pm.qnameFor(uri)
    }

    override fun lock(): PrefixMapping = writeLock.withLock {
        also { pm.lock() }
    }

    override fun numPrefixes(): Int = readLock.withLock {
        pm.numPrefixes()
    }

    override fun samePrefixMappingAs(other: PrefixMapping): Boolean = readLock.withLock {
        pm.samePrefixMappingAs(other)
    }
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