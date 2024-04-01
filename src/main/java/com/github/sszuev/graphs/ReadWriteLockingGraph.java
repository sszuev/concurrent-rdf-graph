package com.github.sszuev.graphs;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Thread-safe concurrent {@link GraphWrapper} which use {@link Lock}s for synchronization.
 * <p>
 * Note that method {@link GraphWrapper#get} is not synchronized.
 * Also, components {@link TransactionHandler}, {@link Capabilities}, {@link GraphEventManager} are not synchronized.
 * <p>
 * Complex operations like {@link org.apache.jena.rdf.model.Model#write} are not thread-safe.
 */
public class ReadWriteLockingGraph extends BaseNonBlockingReadGraph implements ConcurrentGraph {
    private final Lock readLock;
    private final Lock writeLock;

    public ReadWriteLockingGraph(
            Graph base,
            Lock readLock,
            Lock writeLock,
            ConcurrentGraphConfiguration config
    ) {
        super(base, config);
        this.readLock = readLock;
        this.writeLock = writeLock;
    }

    public ReadWriteLockingGraph(
            Graph graph,
            ReadWriteLock lock,
            ConcurrentGraphConfiguration config
    ) {
        this(
                graph,
                lock.readLock(),
                lock.writeLock(),
                config == null ? ConcurrentGraphConfiguration.DEFAULT : config
        );
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        readLock.lock();
        try {
            return new ReadWriteLockingPrefixMapping(get().getPrefixMapping(), readLock, writeLock);
        } finally {
            readLock.unlock();
        }
    }

    public <X> X read(Action<X> action) {
        readLock.lock();
        try {
            return action.execute();
        } finally {
            readLock.unlock();
        }
    }

    public void modify(Runnable action) {
        writeLock.lock();
        try {
            syncModify(action);
        } finally {
            writeLock.unlock();
        }
    }
}


