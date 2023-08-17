package com.github.sszuev.graphs.testutils

import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

internal const val NS = "https://test.ex.com#"

internal fun createTriple(subject: Node? = null, predicate: Node? = null, value: Node? = null): Triple {
    return Triple.create(
        subject ?: uri(createRandomUri()),
        predicate ?: uri(createRandomUri()),
        value ?: uri(createRandomUri()),
    )
}

internal fun createRandomUri(): String {
    return NS + UUID.randomUUID()
}

fun <T> Collection<T>.threadLocalRandom(): T {
    if (isEmpty())
        throw NoSuchElementException("Collection is empty.")
    return elementAt(ThreadLocalRandom.current().nextInt(size))
}
