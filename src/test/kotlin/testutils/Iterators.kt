package com.github.sszuev.graphs.testutils

import org.apache.jena.util.iterator.ExtendedIterator
import java.util.stream.Collectors
import java.util.stream.Stream

fun <X> Stream<X>.toSet(): Set<X> = this.collect(Collectors.toSet())

fun <X> ExtendedIterator<X>.first(): X? {
    return try {
        if (hasNext()) next() else null
    } finally {
        close()
    }
}