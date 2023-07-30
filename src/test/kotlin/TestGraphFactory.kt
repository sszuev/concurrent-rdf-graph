package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.loadGraph
import org.apache.jena.graph.Graph
import org.apache.jena.sparql.graph.GraphFactory
import org.apache.jena.sparql.graph.GraphTxn
import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("unused")
internal enum class TestGraphFactory {
    SYNCHRONIZED_GRAPH {
        override fun create(): Graph = SynchronizedGraph(GraphFactory.createGraphMem())

        override fun load(resource: String): Graph = SynchronizedGraph(loadGraph(resource, { GraphFactory.createGraphMem() }))
    },
    RW_LOCKING_GRAPH {
        override fun create(): Graph = ReadWriteLockingGraph(GraphFactory.createGraphMem(), ReentrantReadWriteLock())

        override fun load(resource: String): Graph =
            ReadWriteLockingGraph(loadGraph(resource, { GraphFactory.createGraphMem() }), ReentrantReadWriteLock())
    },
    TXN_GRAPH {
        override fun create(): Graph {
            return GraphTxn()
        }

        override fun load(resource: String): Graph {
            return loadGraph(resource, { GraphTxn() })
        }
    },
    MEM_GRAPH {
        override fun create(): Graph = GraphFactory.createGraphMem()

        override fun load(resource: String): Graph = loadGraph(resource, { GraphFactory.createGraphMem() })
    },
    ;

    abstract fun create(): Graph

    abstract fun load(resource: String): Graph
}