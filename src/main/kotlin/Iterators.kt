package com.github.sszuev.graphs

import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.WrappedIterator
import java.util.LinkedList
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport

internal fun <X> Stream<X>.asExtendedIterator(): ExtendedIterator<X> = object : WrappedIterator<X>(this.iterator()) {
    override fun close() {
        this@asExtendedIterator.close()
    }
}

internal fun <X> ExtendedIterator<X>.asStream(): Stream<X> =
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.IMMUTABLE), false)
        .onClose { this.close() }

internal inline fun <T, R : MutableCollection<T>> Iterator<T>.collect(create: () -> R): R {
    val res = create()
    while (hasNext()) {
        res.add(next())
    }
    return res
}

fun <X> LinkedList<X>.erasingIterator(): Iterator<X> = object : Iterator<X> {
    override fun hasNext(): Boolean = this@erasingIterator.isNotEmpty()

    override fun next(): X = this@erasingIterator.removeFirst()
}