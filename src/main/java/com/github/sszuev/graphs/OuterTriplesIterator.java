package com.github.sszuev.graphs;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.NiceIterator;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

/**
 * An [ExtendedIterator] wrapper for triples that allows to substitute the base iterator.
 * Synchronized by [Lock].
 * Has no reference to the graph when it is released ([base] replaced by snapshot)
 */
class OuterTriplesIterator extends NiceIterator<Triple> {
    volatile Iterator<Triple> base;
    volatile Lock lock;
    volatile long startAt;

    OuterTriplesIterator(Iterator<Triple> base, Lock lock) {
        this.base = base;
        this.lock = lock;
    }

    @Override
    public boolean hasNext() {
        lock.lock();
        try {
            if (startAt != 0L) {
                startAt = System.currentTimeMillis();
            }
            return base.hasNext();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Triple next() {
        lock.lock();
        try {
            if (startAt != 0L) {
                startAt = System.currentTimeMillis();
            }
            return base.next();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            close(base);
        } finally {
            lock.unlock();
        }
    }
}
