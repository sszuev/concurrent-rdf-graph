package com.github.sszuev.graphs

import org.apache.jena.graph.Triple
import org.apache.jena.util.iterator.ClosableIterator
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.NiceIterator
import org.apache.jena.util.iterator.NullIterator
import org.apache.jena.util.iterator.WrappedIterator
import java.util.Queue
import java.util.Spliterator
import java.util.Spliterators
import java.util.concurrent.locks.Lock
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.concurrent.withLock

fun <X> Stream<X>.asExtendedIterator(): ExtendedIterator<X> = object : WrappedIterator<X>(this.iterator()) {
    override fun close() {
        this@asExtendedIterator.close()
    }
}

fun <X> ExtendedIterator<X>.asStream(): Stream<X> {
    if (this is NullIterator) {
        return Stream.empty()
    }
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.NONNULL), false)
        .onClose { this.close() }
}

inline fun <T, R : MutableCollection<T>> ClosableIterator<T>.collect(create: () -> R): R {
    val res = create()
    try {
        while (this.hasNext()) {
            res.add(this.next())
        }
    } finally {
        this.close()
    }
    return res
}

fun <X> Iterator<X>.nextOrNull(): X? = if (this.hasNext()) this.next() else null

fun <X> Queue<X>.erasingIterator(): Iterator<X> = object : Iterator<X> {
    override fun hasNext(): Boolean = this@erasingIterator.isNotEmpty()

    override fun next(): X = this@erasingIterator.remove()
}

/**
 * For `ReadWriteLockingGraph`.
 * A wrapper for base graph iterator.
 * Has a reference to the graph through [onClose] operation.
 */
internal class InnerTriplesIterator(
    val source: () -> ExtendedIterator<Triple>,
    val cache: Queue<Triple>,
    val onClose: () -> Unit,
) : NiceIterator<Triple>() {

    val cacheIterator: Iterator<Triple> = cache.erasingIterator()
    private var baseIterator: ExtendedIterator<Triple>? = null
    private var closed = false

    private fun base(): ExtendedIterator<Triple> {
        if (baseIterator == null) {
            baseIterator = source()
        }
        return checkNotNull(baseIterator)
    }

    private fun invokeOnClose() {
        if (closed) {
            return
        }
        onClose()
        closed = true
    }

    fun cache(size: Int): Boolean {
        val base = base()
        var count = 0
        while (count++ < size && base.hasNext()) {
            cache.add(base.next())
        }
        return base.hasNext()
    }

    override fun hasNext(): Boolean = try {
        if (cacheIterator.hasNext()) {
            true
        } else {
            val res = base().hasNext()
            if (!res) {
                onClose()
            }
            res
        }
    } catch (ex: Exception) {
        invokeOnClose()
        throw ex
    }

    override fun next(): Triple = try {
        cacheIterator.nextOrNull() ?: base().next()
    } catch (ex: Exception) {
        invokeOnClose()
        throw ex
    }

    override fun close() {
        cache.clear()
        baseIterator?.close()
        invokeOnClose()
    }

}

/**
 * For `ReadWriteLockingGraph`.
 * An [ExtendedIterator] wrapper for triples that allows to substitute the base iterator.
 * Synchronized by [Lock].
 * Has no reference to the graph when it is released ([base] replaced by snapshot)
 */
internal class OuterTriplesIterator(
    @Volatile var base: Iterator<Triple>,
    @Volatile var lock: Lock,
) : NiceIterator<Triple>() {

    @Volatile
    var started = false

    override fun hasNext(): Boolean = lock.withLock {
        started = true
        base.hasNext()
    }

    override fun next(): Triple = lock.withLock {
        started = true
        base.next()
    }

    override fun close() = lock.withLock { close(base) }
}