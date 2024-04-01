package com.github.sszuev.graphs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A dummy [Lock] that does nothing.
 * When iterator is realised to snapshot, we replace [ReentrantLock] with this one since is no longer needed.
 */
@SuppressWarnings("NullableProblems")
class NoOpLock implements Lock {
    static final NoOpLock INSTANCE = new NoOpLock();

    @Override
    public void lock() {
    }

    @Override
    public void lockInterruptibly() {
    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return true;
    }

    @Override
    public void unlock() {
    }

    @Override
    public Condition newCondition() {
        throw new IllegalStateException();
    }
}
