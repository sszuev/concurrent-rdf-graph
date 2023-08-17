@file:Suppress("FunctionName", "DuplicatedCode")

package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.any
import com.github.sszuev.graphs.testutils.bnode
import com.github.sszuev.graphs.testutils.first
import com.github.sszuev.graphs.testutils.literal
import com.github.sszuev.graphs.testutils.uri
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.vocabulary.OWL2
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import org.openjdk.jmh.infra.Blackhole

/**
 * 6 find operations, 4 modification operations (2 add + 2 delete)
 */
internal fun smallGraph_scenarioA(
    graph: Graph,
    b: Blackhole,
    ti: Int,
    ii: Int,
) {
    val res1 = graph.find(uri("s1"), uri("p1"), any()).toList()
    check(5 == res1.size)
    b.consume(res1)

    val res2 = graph.find(any(), uri("p2"), any()).toList()
    check(3 == res2.size)
    b.consume(res2)

    val t1 = Triple.create(uri("s-$ti-$ii"), uri("p-$ti"), literal("x"))
    graph.add(t1)

    val res3 = graph.find(any(), uri("p3"), any()).toList()
    check(2 == res3.size)
    b.consume(res3)

    val t2 = Triple.create(bnode(), uri("p-$ti"), bnode())
    graph.add(t2)

    val res4 = graph.find().toList()
    check(48 <= res4.size)
    b.consume(res4)

    graph.delete(t1)
    graph.delete(t2)
    b.consume(t1)
    b.consume(t2)

    val res5 = graph.find(uri("s6"), any(), uri("o6")).toList()
    check(3 == res5.size)
    b.consume(res5)

    val res6 = graph.find(uri("s6"), uri("p6"), uri("o6")).toList()
    check(1 == res6.size)
    b.consume(res6)
}

fun pizzaGraph_scenarioA(
    graph: Graph,
    b: Blackhole,
    ti: Int,
    ii: Int,
) {
    val ns = "http://www.co-ode.org/ontologies/pizza/pizza.owl#"

    val m = ModelFactory.createModelForGraph(graph)

    val x = m.listStatements(null, null, null as RDFNode?).filterKeep { it.`object`.isLiteral }
        .filterKeep { it.literal.language == "pt" }
        .toList()
    check(x.size >= 95)
    b.consume(x)

    b.consume(m.createResource(ns + "GorgonzolaTopping").getProperty(RDF.type))

    checkNotNull(
        m.listStatements(
            m.createResource(ns + "RocketTopping"),
            RDFS.subClassOf,
            m.createResource(ns + "VegetableTopping")
        ).first()
    )

    val y = m.listStatements(null, RDFS.subClassOf, null as RDFNode?)
        .filterKeep { it.`object`.isURIResource }
        .mapWith { m.listStatements(it.resource, null, null as RDFNode?) }
        .toList()
    check(y.size >= 84)
    b.consume(y)

    check(m.contains(m.createResource(ns + "Siciliana"), null, null as RDFNode?))
    check(!m.contains(m.createResource("X"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    val w = m.listStatements(null, RDF.type, OWL2.Class).filterKeep { it.subject.isURIResource }.toList()
    check(w.size >= 100)
    b.consume(w)

    check(m.contains(m.createResource(ns + "JalapenoPepperTopping"), RDFS.subClassOf, null as RDFNode?))

    val j = m.createResource(ns + "MushroomTopping").listProperties().toList()
    val k = m.createResource("${ns}MushroomTopping-${ti}-${ii}")
    j.forEach {
        k.addProperty(it.predicate, it.`object`)
    }
    b.consume(j)
    b.consume(k)

    val f = m.listStatements(null, OWL2.disjointWith, null as RDFNode?).toList()
    check(f.size >= 398)
    b.consume(f)

    val g = m.listStatements(null, OWL2.differentFrom, null as RDFNode?)
        .toList()
    check(g.size == 0)
    b.consume(g)

    m.remove(k.listProperties())

    check(m.contains(m.createResource(ns + "LeekTopping"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    val h = m.listStatements(null, null, OWL2.Restriction).toList()
    check(h.size == 188)
    b.consume(h)

    val u = m.listStatements(null, OWL2.someValuesFrom, null as RDFNode?).toList()
    check(u.size == 155)
    b.consume(u)

    val r = m.listStatements(null, RDFS.subClassOf, null as RDFNode?).toList()
    check(r.size >= 259)
    b.consume(r)

    checkNotNull(m.listStatements(null, RDF.type, OWL2.Class).first())

    b.consume(m)
}

internal fun pizzaGraph_scenarioB(
    graph: Graph,
    b: Blackhole,
) {
    val ns = "http://www.co-ode.org/ontologies/pizza/pizza.owl#"

    val m = ModelFactory.createModelForGraph(graph)

    val u = m.listStatements(null, OWL2.someValuesFrom, null as RDFNode?)
        .filterKeep { it.subject.isAnon }
        .filterKeep { it.subject.hasProperty(RDF.type, OWL2.Restriction) }
        .toList()
    check(u.size == 155)
    b.consume(u)

    val r = m.listStatements(null, RDFS.subClassOf, null as RDFNode?).toList()
    check(r.size == 259)
    b.consume(r)

    checkNotNull(m.listStatements(null, RDF.type, OWL2.Class).first())

    b.consume(m.createResource(ns + "GorgonzolaTopping").getProperty(RDF.type))

    val g = m.listStatements(null, OWL2.differentFrom, null as RDFNode?)
        .toList()
    check(g.size == 0)
    b.consume(g)

    checkNotNull(
        m.listStatements(
            m.createResource(ns + "RocketTopping"),
            RDFS.subClassOf,
            m.createResource(ns + "VegetableTopping")
        ).first()
    )

    val y = m.listStatements(null, RDFS.subClassOf, null as RDFNode?)
        .filterKeep { it.`object`.isURIResource }
        .mapWith { m.listStatements(it.resource, null, null as RDFNode?) }
        .toList()
    check(y.size == 84)
    b.consume(y)

    val f = m.listStatements(null, OWL2.disjointWith, null as RDFNode?)
        .filterKeep { it.`object`.isURIResource }
        .filterKeep { it.resource.hasProperty(RDF.type, OWL2.Class) }
        .toList()
    check(f.size == 398)
    b.consume(f)

    val x = m.listStatements(null, null, null as RDFNode?).filterKeep { it.`object`.isLiteral }
        .filterKeep { it.literal.language == "pt" }
        .toList()
    check(x.size == 95)
    b.consume(x)

    val w = m.listStatements(null, RDF.type, OWL2.Class)
        .filterKeep { it.subject.isURIResource }
        .filterKeep { it.subject.hasProperty(RDFS.label) }
        .toList()
    check(w.size == 96)
    b.consume(w)

    check(m.contains(m.createResource(ns + "Siciliana"), null, null as RDFNode?))
    check(!m.contains(m.createResource("X"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    check(m.contains(m.createResource(ns + "JalapenoPepperTopping"), RDFS.subClassOf, null as RDFNode?))

    check(m.contains(m.createResource(ns + "LeekTopping"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    val h = m.listStatements(null, null, OWL2.Restriction).toList()
    check(h.size == 188)
    b.consume(h)

    b.consume(m)
}

internal fun scenarioC(
    graph: Graph,
    b: Blackhole,
) {
    smallGraph.find().forEach {
        graph.add(it)
        b.consume(
            graph.find(it.subject, Node.ANY, Node.ANY).toSet()
        )
        b.consume(
            graph.find(Node.ANY, it.predicate, Node.ANY).toSet()
        )
        b.consume(
            graph.find(Node.ANY, it.predicate, it.`object`).toSet()
        )
    }
    smallGraph.find().forEach {
        graph.delete(it)
    }
    smallGraph.find().forEach {
        b.consume(
            graph.find(it.subject, Node.ANY, it.`object`).toSet()
        )
        b.consume(
            graph.find(it.subject, it.predicate, Node.ANY).toSet()
        )
        b.consume(
            graph.find(Node.ANY, Node.ANY, it.`object`).toSet()
        )
    }
}

fun scenarioD(
    graph: Graph,
) {
    smallGraph.find().forEach {
        graph.add(it)
    }
    smallGraph.find().forEach {
        graph.delete(it)
    }
}