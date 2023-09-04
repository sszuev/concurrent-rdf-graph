package com.github.sszuev.graphs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
import org.apache.jena.util.iterator.WrappedIterator
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Stream
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

class ReadWriteLockingGraphKImpl(
    graph: Graph,
    private val coroutineContext: CoroutineContext,
    private val readLock: Lock,
    private val writeLock: Lock,
    private val iteratorCacheChunkSize: Int,
) : GraphWrapper(graph), Graph {

    constructor(
        graph: Graph,
        lock: ReadWriteLock = ReentrantReadWriteLock(),
        iteratorCacheChunkSize: Int = 1024,
        coroutineContext: CoroutineContext = Dispatchers.Default,
    ) : this(
        graph = graph,
        coroutineContext = coroutineContext,
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
    @Suppress("DuplicatedCode")
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

    // This function should not be used from a coroutine.
    private fun <X> modify(action: () -> X): X = writeLock.withLock {
        runBlocking(coroutineContext) {
            modifyInCoroutine(action)
        }
    }

    @Suppress("DuplicatedCode")
    private inline fun <X> modifyInCoroutine(action: () -> X): X {
        val unstartedIterators = mutableListOf<Lock>()
        val processing = mutableListOf<String>()
        return try {
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
            while (processing.isNotEmpty()) {
                val id = processing.removeFirst()
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