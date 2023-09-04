package com.github.sszuev.graphs

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/**
 * A dummy [Lock] that does nothing.
 * When iterator is realised to snapshot we replace [ReentrantLock] with this one since is no longer needed.
 */
internal object NoOpLock : Lock {
    override fun lock() {
    }

    override fun lockInterruptibly() {
    }

    override fun tryLock(): Boolean {
        return true
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        return true
    }

    override fun unlock() {
    }

    override fun newCondition(): Condition {
        throw IllegalStateException()
    }
}