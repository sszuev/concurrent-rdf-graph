package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;

/**
 * Thread-safe concurrent {@link GraphWrapper} which use java synchronization.
 */
public class ExtendedSynchronizedGraph extends BaseNonBlockingReadGraph implements ConcurrentGraph {
    private final Object lock;

    public ExtendedSynchronizedGraph(Graph base, Object lock, ConcurrentGraphConfiguration config) {
        super(base, config);
        this.lock = (lock != null) ? lock : this;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        synchronized (lock) {
            return new SynchronizedPrefixMapping(get().getPrefixMapping(), lock);
        }
    }

    @Override
    public <X> X read(Action<X> action) {
        synchronized (lock) {
            return action.execute();
        }
    }

    @Override
    public void modify(Runnable action) {
        synchronized (lock) {
            syncModify(action);
        }
    }
}


