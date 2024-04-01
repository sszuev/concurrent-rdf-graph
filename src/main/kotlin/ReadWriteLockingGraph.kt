package com.github.sszuev.graphs

import org.apache.jena.graph.Capabilities
import org.apache.jena.graph.Graph
import org.apache.jena.graph.GraphEventManager
import org.apache.jena.graph.TransactionHandler
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.sparql.graph.GraphWrapper
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Thread-safe concurrent [GraphWrapper] which use [Lock]s for synchronization.
 *
 * Note that the method [GraphWrapper.get]
 * and components [TransactionHandler], [Capabilities], [GraphEventManager] are not thread-safe.
 * Complex operation like [org.apache.jena.rdf.model.Model.write] are not thread-safe as well.
 */
class ReadWriteLockingGraph(
    base: Graph,
    private val readLock: Lock,
    private val writeLock: Lock,
    config: ConcurrentGraphConfiguration,
) : BaseNonBlockingReadGraph(base, config), ConcurrentGraph {

    constructor(
        graph: Graph,
        lock: ReadWriteLock = ReentrantReadWriteLock(),
        config: ConcurrentGraphConfiguration = ConcurrentGraphConfiguration.default,
    ) : this(
        base = graph,
        readLock = lock.readLock(),
        writeLock = lock.writeLock(),
        config = config,
    )

    override fun getPrefixMapping(): PrefixMapping =
        ReadWriteLockingPrefixMapping(get().prefixMapping, readLock, writeLock)

    override fun <X> read(action: () -> X): X = readLock.withLock(action)

    override fun <X> modify(action: () -> X): X = writeLock.withLock { syncModify(action) }
}
