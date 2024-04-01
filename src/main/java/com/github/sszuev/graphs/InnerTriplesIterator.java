package com.github.sszuev.graphs;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

import java.util.Iterator;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation.
 */
class InnerTriplesIterator extends NiceIterator<Triple> {
    final Supplier<ExtendedIterator<Triple>> source;
    final Queue<Triple> cache;
    final Runnable onClose;
    final Iterator<Triple> cacheIterator;
    private ExtendedIterator<Triple> baseIterator;
    private boolean closed;

    InnerTriplesIterator(Supplier<ExtendedIterator<Triple>> source, Queue<Triple> cache, Runnable onClose) {
        this.source = source;
        this.cache = cache;
        this.onClose = onClose;
        this.cacheIterator = Iterators.erasingIterator(cache);
    }

    private ExtendedIterator<Triple> base() {
        if (baseIterator == null) {
            baseIterator = source.get();
        }
        return baseIterator;
    }

    private void invokeOnClose() {
        if (closed) {
            return;
        }
        onClose.run();
        closed = true;
    }

    boolean cache(int size) {
        ExtendedIterator<Triple> base = base();
        int count = 0;
        while (count++ < size && base.hasNext()) {
            cache.add(base.next());
        }
        return base.hasNext();
    }

    @Override
    public boolean hasNext() {
        try {
            if (cacheIterator.hasNext()) {
                return true;
            } else {
                boolean res = base().hasNext();
                if (!res) {
                    onClose.run();
                }
                return res;
            }
        } catch (Exception ex) {
            invokeOnClose();
            throw ex;
        }
    }

    @Override
    public Triple next() {
        try {
            return cacheIterator.hasNext() ? cacheIterator.next() : base().next();
        } catch (Exception ex) {
            invokeOnClose();
            throw ex;
        }
    }

    @Override
    public void close() {
        cache.clear();
        if (baseIterator != null) {
            baseIterator.close();
        }
        invokeOnClose();
    }
}
