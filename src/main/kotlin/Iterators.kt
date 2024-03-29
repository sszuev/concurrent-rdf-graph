package com.github.sszuev.graphs

import org.apache.jena.util.iterator.ClosableIterator
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.NullIterator
import org.apache.jena.util.iterator.WrappedIterator
import java.util.Queue
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport

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