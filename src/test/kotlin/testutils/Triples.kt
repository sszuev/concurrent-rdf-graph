package com.github.sszuev.graphs.testutils

import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
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

fun Model.create(
    subject: String? = null,
    predicate: String,
    node: String? = null,
): Statement {
    val res = createStatement(createResource(subject),
        createProperty(predicate),
        createResource(node))
    add(res)
    return res
}

fun Model.statements(
    subject: String? = null,
    predicate: String? = null,
    node: String? = null,
) = statements(
    subject?.let { createResource(it) },
    predicate?.let { createProperty(it) },
    node?.let { createResource(it) },
)

fun Model.statements(
    subject: Resource? = null,
    predicate: Property? = null,
    node: RDFNode? = null,
) = listStatements(subject, predicate, node).asSequence()