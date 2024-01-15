package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.sparql.graph.GraphWrapper

/**
 * Thread-safe concurrent [GraphWrapper] which use java synchronization.
 */
class ExtendedSynchronizedGraph(
    base: Graph,
    lock: Any? = null,
    config: ConcurrentGraphConfiguration = ConcurrentGraphConfiguration.default,
) : BaseNonBlockingReadGraph(base, config), ConcurrentGraph {

    private val lock: Any = lock ?: this

    override fun getPrefixMapping(): PrefixMapping = SynchronizedPrefixMapping(get().prefixMapping, lock)

    override fun <X> read(action: () -> X): X = synchronized(lock, action)

    override fun <X> modify(action: () -> X): X = synchronized(lock) { syncModify(action) }
}