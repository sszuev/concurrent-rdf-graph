package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import java.util.stream.Collectors
import java.util.stream.Stream


internal fun <X> Stream<X>.toSet(): Set<X> = this.collect(Collectors.toSet())

internal fun loadGraph(resource: String, lang: Lang? = Lang.TURTLE): Graph {
    checkNotNull(Class.forName("com.github.sszuev.graphs.TestUtilsKt").getResourceAsStream(resource)).use {
        val foundLang = lang ?: RDFDataMgr.determineLang(resource, null, null)
        val res = ModelFactory.createDefaultModel()
        RDFDataMgr.read(res, it, foundLang)
        return res.graph
    }
}