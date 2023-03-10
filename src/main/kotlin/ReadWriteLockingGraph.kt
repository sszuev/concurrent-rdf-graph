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

    private val openIterators: MutableMap<String, OuterTriplesIterator> = ConcurrentHashMap()

    @Throws(AddDeniedException::class)
    override fun add(triple: Triple) = modify { get().add(triple) }

    @Throws(DeleteDeniedException::class)
    override fun delete(triple: Triple) = modify { get().delete(triple) }

    @Throws(DeleteDeniedException::class)
    override fun remove(s: Node?, p: Node?, o: Node?) = modify { get().remove(s, p, o) }

    @Throws(DeleteDeniedException::class)
    override fun clear() = modify { get().clear() }

    override fun close() = modify { get().close() }

    override fun find(triple: Triple): ExtendedIterator<Triple> = read { get().find(triple).remember() }

    override fun find(s: Node?, p: Node?, o: Node?): ExtendedIterator<Triple> = read { get().find(s, p, o).remember() }

    override fun find(): ExtendedIterator<Triple> = read { get().find().remember() }

    override fun stream(s: Node?, p: Node?, o: Node?): Stream<Triple> = read {
        get().stream(s, p, o).asExtendedIterator().remember().asStream()
    }

    override fun stream(): Stream<Triple> = read { get().stream().asExtendedIterator().remember().asStream() }

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
        openIterators.keys.forEach { releaseToSnapshot(it) }
        action()
    }

    private inline fun <X> read(action: () -> X): X = readLock.withLock(action)

    private fun ExtendedIterator<Triple>.remember(): ExtendedIterator<Triple> {
        val id = UUID.randomUUID().toString()
        val res = OuterTriplesIterator(
            InnerTriplesIterator(base = this) { releaseToNull(id) },
            ReentrantLock(),
        )
        openIterators[id] = res
        return res
    }

    private fun releaseToSnapshot(id: String) = openIterators.remove(id)?.let { wrapper ->
        wrapper.lock.withLock {
            val inner = wrapper.base as InnerTriplesIterator
            val rest = inner.collect { mutableListOf() }
            wrapper.base = rest.iterator()
            wrapper.lock = NoOpLock
        }
    }

    private fun releaseToNull(id: String) = openIterators.remove(id)?.let { wrapper ->
        wrapper.base = WrappedIterator.emptyIterator()
        wrapper.lock = NoOpLock
    }

}

/**
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation (see [ReadWriteLockingGraph.releaseToNull]).
 */
private class InnerTriplesIterator(
    val base: ExtendedIterator<Triple>,
    inline val onClose: () -> Unit,
) : NiceIterator<Triple>() {

    override fun hasNext(): Boolean = try {
        val res = base.hasNext()
        if (!res) {
            onClose()
        }
        res
    } catch (ex: Exception) {
        onClose()
        throw ex
    }

    override fun next(): Triple = try {
        base.next()
    } catch (ex: Exception) {
        onClose()
        throw ex
    }

    override fun close() {
        base.close()
        onClose()
    }
}

/**
 * An [ExtendedIterator] wrapper for triples that allows to substitute the base iterator.
 * Synchronized by [Lock].
 * Has no reference to the graph when it is released (see [ReadWriteLockingGraph.releaseToSnapshot])
 */
private class OuterTriplesIterator(
    var base: Iterator<Triple>,
    var lock: Lock,
) : NiceIterator<Triple>() {

    override fun hasNext(): Boolean = lock.withLock { base.hasNext() }

    override fun next(): Triple = lock.withLock { base.next() }

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