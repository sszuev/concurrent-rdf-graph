package com.github.sszuev.graphs;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Synchronized {@link GraphWrapper}.
 * Each operation is synchronized,
 * each iterator created by the graph is collected into memory immediately after the iterator is created,
 * so user-code always works with snapshots.
 * <p>
 * Note that method {@link GraphWrapper#get} is not synchronized.
 * Also, components {@link TransactionHandler}, {@link Capabilities}, {@link GraphEventManager} are not synchronized.
 * <p>
 * Complex operations like {@link org.apache.jena.rdf.model.Model#write} are not thread-safe.
 */
public class SimpleSynchronizedGraph extends GraphWrapper implements ConcurrentGraph {
    private final Object lock;

    public SimpleSynchronizedGraph(Graph base) {
        this(base, null);
    }

    public SimpleSynchronizedGraph(Graph base, Object lock) {
        super(base);
        this.lock = lock != null ? lock : this;
    }

    @Override
    public void add(Triple triple) throws AddDeniedException {
        synchronized (lock) {
            get().add(triple);
        }
    }

    @Override
    public void delete(Triple triple) throws DeleteDeniedException {
        synchronized (lock) {
            get().delete(triple);
        }
    }

    @Override
    public void remove(Node s, Node p, Node o) throws DeleteDeniedException {
        synchronized (lock) {
            get().remove(s, p, o);
        }
    }

    @Override
    public void clear() throws DeleteDeniedException {
        synchronized (lock) {
            get().clear();
        }
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        synchronized (lock) {
            return WrappedIterator.create(get().find(triple).toList().iterator());
        }
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        synchronized (lock) {
            return WrappedIterator.create(get().find(s, p, o).toList().iterator());
        }
    }

    @Override
    public ExtendedIterator<Triple> find() {
        synchronized (lock) {
            return WrappedIterator.create(get().find().toList().iterator());
        }
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        synchronized (lock) {
            return get().stream(s, p, o).collect(Collectors.toList()).stream();
        }
    }

    @Override
    public Stream<Triple> stream() {
        synchronized (lock) {
            return get().stream().collect(Collectors.toList()).stream();
        }
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        synchronized (lock) {
            return get().contains(s, p, o);
        }
    }

    @Override
    public boolean contains(Triple t) {
        synchronized (lock) {
            return get().contains(t);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            get().close();
        }
    }

    @Override
    public boolean isClosed() {
        synchronized (lock) {
            return get().isClosed();
        }
    }

    @Override
    public boolean isIsomorphicWith(Graph g) {
        synchronized (lock) {
            return get().isIsomorphicWith(g);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return get().isEmpty();
        }
    }

    @Override
    public int size() {
        synchronized (lock) {
            return get().size();
        }
    }

    @Override
    public boolean dependsOn(Graph other) {
        synchronized (lock) {
            return get().dependsOn(other);
        }
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        synchronized (lock) {
            return get().getTransactionHandler();
        }
    }

    @Override
    public Capabilities getCapabilities() {
        synchronized (lock) {
            return get().getCapabilities();
        }
    }

    @Override
    public GraphEventManager getEventManager() {
        synchronized (lock) {
            return get().getEventManager();
        }
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return new SynchronizedPrefixMapping(get().getPrefixMapping(), lock);
    }
}


