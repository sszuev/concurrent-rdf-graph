package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A base for concurrent thread-safe graph-wrapper.
 * Almost each read operation is not blocking (with except of count and contains), producing lazy iterators.
 * This means that iteration is possible even during modification;
 * modification operations collect data over which iteration occurs into snapshots, replacing internal iterator,
 * but try the best to save memory.
 * <p>
 * Note that the method {@link GraphWrapper#get()}
 * and components {@link org.apache.jena.graph.TransactionHandler},
 * {@link org.apache.jena.graph.Capabilities}, {@link org.apache.jena.graph.GraphEventManager} are not thread-safe.
 * Complex operations like {@link org.apache.jena.rdf.model.Model#write} are not thread-safe as well.
 */
public abstract class BaseNonBlockingReadGraph extends GraphWrapper implements ConcurrentGraph {
    private final ConcurrentHashMap<String, OuterTriplesIterator> openIterators = new ConcurrentHashMap<>();
    private final ConcurrentGraphConfiguration config;

    protected BaseNonBlockingReadGraph(Graph base, ConcurrentGraphConfiguration config) {
        super(base);
        this.config = config;
    }

    @Override
    public void add(Triple triple) throws AddDeniedException {
        modify(() -> get().add(triple));
    }

    @Override
    public void delete(Triple triple) throws DeleteDeniedException {
        modify(() -> get().delete(triple));
    }

    @Override
    public void remove(Node s, Node p, Node o) throws DeleteDeniedException {
        modify(() -> get().remove(s, p, o));
    }

    @Override
    public void clear() {
        modify(() -> get().clear());
    }

    @Override
    public void close() {
        modify(() -> get().close());
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        return read(() -> remember(() -> get().find(triple)));
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        return read(() -> remember(() -> get().find(s, p, o)));
    }

    @Override
    public ExtendedIterator<Triple> find() {
        return read(() -> remember(() -> get().find()));
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        return read(() -> Iterators.asStream(remember(() -> Iterators.asExtendedIterator(get().stream(s, p, o)))));
    }

    @Override
    public Stream<Triple> stream() {
        return read(() -> Iterators.asStream(remember(() -> Iterators.asExtendedIterator(get().stream()))));
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return read(() -> get().contains(s, p, o));
    }

    @Override
    public boolean contains(Triple t) {
        return read(() -> get().contains(t));
    }

    @Override
    public boolean isClosed() {
        return read(() -> get().isClosed());
    }

    @Override
    public boolean isIsomorphicWith(Graph g) {
        return read(() -> get().isIsomorphicWith(g));
    }

    @Override
    public boolean isEmpty() {
        return read(() -> get().isEmpty());
    }

    @Override
    public int size() {
        return read(() -> get().size());
    }

    @Override
    public boolean dependsOn(Graph other) {
        return read(() -> get().dependsOn(other));
    }

    @Override
    public abstract PrefixMapping getPrefixMapping();

    protected abstract <X> X read(Action<X> action);

    protected abstract void modify(Runnable action);

    /**
     * Creates a new lazy iterator wrapper.
     */
    private ExtendedIterator<Triple> remember(Supplier<ExtendedIterator<Triple>> source) {
        String id = UUID.randomUUID().toString();
        OuterTriplesIterator res = new OuterTriplesIterator(
                new InnerTriplesIterator(source, new ArrayDeque<>(), () -> openIterators.remove(id)),
                new ReentrantLock()
        );
        openIterators.put(id, res);
        return res;
    }

    protected void syncModify(Runnable action) {
        List<Lock> unstartedIterators = new ArrayList<>();
        List<Pair<String, Long>> processing = new ArrayList<>();
        try {
            openIterators.forEach((id, it) -> {
                if (it.startAt == 0L) {
                    processing.add(new Pair<>(id, it.startAt));
                    return;
                }
                it.lock.lock();
                if (it.startAt == 0L) {
                    it.lock.unlock();
                    processing.add(new Pair<>(id, it.startAt));
                } else {
                    // If the iterator is not running yet, we run it after modification
                    unstartedIterators.add(it.lock);
                }
            });
            if (config.isProcessOldestFirst()) {
                processing.sort(Comparator.comparingLong(Pair::getSecond));
            }
            while (!processing.isEmpty()) {
                Pair<String, Long> processed = processing.remove(0);
                String id = processed.getFirst();
                OuterTriplesIterator it = openIterators.get(id);
                if (it == null || !(it.base instanceof InnerTriplesIterator)) { // released on close
                    openIterators.remove(id);
                    continue;
                }
                it.lock.lock();
                InnerTriplesIterator inner = (InnerTriplesIterator) it.base;
                if (inner == null) { // released on close
                    openIterators.remove(id);
                } else if (!inner.cache(config.getIteratorCacheChunkSize())) {
                    it.base = inner.cacheIterator;
                    it.lock = NoOpLock.INSTANCE;
                    openIterators.remove(id);
                } else {
                    processing.add(processed);
                }
            }
            action.run();
        } finally {
            unstartedIterators.forEach(Lock::unlock);
        }
    }

}


