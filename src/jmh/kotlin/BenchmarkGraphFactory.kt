@file:Suppress("unused")

package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.sparql.graph.GraphFactory
import java.util.concurrent.locks.ReentrantReadWriteLock

enum class BenchmarkGraphFactory {
    TXN_GRAPH {
        override fun newGraph(): Graph {
            return GraphFactory.createTxnGraph()
        }
    },
    MEM_GRAPH {
        override fun newGraph(): Graph {
            return GraphFactory.createGraphMem()
        }
    },
    SYNCHRONIZED_GRAPH {
        override fun newGraph(): Graph {
            return SynchronizedGraph(GraphFactory.createGraphMem())
        }
    },
    RW_LOCKING_GRAPH {
        override fun newGraph(): Graph {
            return ReadWriteLockingGraph(GraphFactory.createGraphMem(), ReentrantReadWriteLock())
        }
    },
    ;

    abstract fun newGraph(): Graph
}