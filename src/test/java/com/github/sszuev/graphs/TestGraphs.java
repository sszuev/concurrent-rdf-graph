package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.sparql.graph.GraphTxn;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public enum TestGraphs {
    SYNCHRONIZED_GRAPH_V1 {
        @Override
        public Graph createNew() {
            return new SimpleSynchronizedGraph(GraphMemFactory.createGraphMem2());
        }
    },
    SYNCHRONIZED_GRAPH_V2 {
        @Override
        public Graph createNew() {
            return new ExtendedSynchronizedGraph(
                    GraphMemFactory.createGraphMem2(),
                    new ConcurrentGraphConfiguration(
                            1024,
                            false
                    )
            );
        }
    },
    RW_LOCKING_GRAPH_V1 {
        @Override
        public Graph createNew() {
            return new ReadWriteLockingGraph(
                    GraphMemFactory.createGraphMem2(),
                    new ReentrantReadWriteLock(),
                    new ConcurrentGraphConfiguration(
                            1024,
                            false
                    )
            );
        }
    },
    RW_LOCKING_GRAPH_V2 {
        @Override
        public Graph createNew() {
            return new ReadWriteLockingGraph(
                    GraphMemFactory.createGraphMem2(),
                    new ReentrantReadWriteLock(),
                    new ConcurrentGraphConfiguration(
                            1024,
                            true
                    )
            );
        }
    },
    TXN_GRAPH {
        @Override
        public Graph createNew() {
            return new GraphTxn();
        }
    },
    MEM_GRAPH {
        @Override
        public Graph createNew() {
            return GraphMemFactory.createGraphMem2();
        }
    };

    public abstract Graph createNew();

    public Graph createFrom(Graph source) {
        Graph res = createNew();
        source.find().forEach(res::add);
        return res;
    }
}