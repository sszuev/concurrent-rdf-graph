package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("unused")
internal enum class TestGraphFactory {
    SYNCHRONIZED_GRAPH {
        override fun create(graph: Graph): Graph = SynchronizedGraph(graph)
    },
    RW_LOCKED_GRAPH {
        override fun create(graph: Graph): Graph = LockingGraph(graph, ReentrantReadWriteLock())
    };

    abstract fun create(graph: Graph): Graph
}