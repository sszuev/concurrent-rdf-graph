package com.github.sszuev.graphs.testutils

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import java.util.stream.Stream

internal const val ns = "https://test.ex.com#"

internal fun <X> Stream<X>.toSet(): Set<X> = this.collect(Collectors.toSet())

internal fun loadGraph(resource: String, factory: () -> Graph, lang: Lang? = Lang.TURTLE): Graph {
    checkNotNull(
        Class.forName("com.github.sszuev.graphs.testutils.MiscTestUtilsKt").getResourceAsStream(resource)
    ).use {
        val foundLang = lang ?: RDFDataMgr.determineLang(resource, null, null)
        val res = ModelFactory.createModelForGraph(factory())
        RDFDataMgr.read(res, it, foundLang)
        return res.graph
    }
}

internal fun createTriple(subject: Node? = null, predicate: Node? = null, value: Node? = null): Triple {
    return Triple.create(
        subject ?: NodeFactory.createURI(createRandomUri()),
        predicate ?: NodeFactory.createURI(createRandomUri()),
        value ?: NodeFactory.createURI(createRandomUri()),
    )
}

internal fun createRandomUri(): String {
    return ns + UUID.randomUUID()
}

fun <T> Collection<T>.threadLocalRandom(): T {
    if (isEmpty())
        throw NoSuchElementException("Collection is empty.")
    return elementAt(ThreadLocalRandom.current().nextInt(size))
}
