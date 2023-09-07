@file:Suppress("FunctionName", "DuplicatedCode")

package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.any
import com.github.sszuev.graphs.testutils.bnode
import com.github.sszuev.graphs.testutils.count
import com.github.sszuev.graphs.testutils.create
import com.github.sszuev.graphs.testutils.first
import com.github.sszuev.graphs.testutils.literal
import com.github.sszuev.graphs.testutils.statements
import com.github.sszuev.graphs.testutils.toSet
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
internal fun smallGraph_scenarioK_RW(
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

    val res4 = graph.find().count()
    check(48L <= res4)
    b.consume(res4)

    graph.delete(t1)
    graph.delete(t2)
    b.consume(t1)
    b.consume(t2)

    val res5 = graph.find(uri("s6"), any(), uri("o6")).toList()
    check(3 == res5.size)
    b.consume(res5)

    val res6 = graph.find(uri("s6"), uri("p6"), uri("o6")).toSet()
    check(1 == res6.size)
    b.consume(res6)
}

internal fun pizzaGraph_scenarioA_RW(
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

internal fun pizzaGraph_scenarioB_R(
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

internal fun scenarioC_RW(
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

fun scenarioD_W(
    graph: Graph,
) {
    smallGraph.find().forEach {
        graph.add(it)
    }
    smallGraph.find().forEach {
        graph.delete(it)
    }
}

fun scenarioF_RW(
    graph: Graph,
    b: Blackhole,
    ti: Int,
    ii: Int,
) {
    val ns = "http://ex#"
    val suffix = "[$ti,$ii]"
    val model = ModelFactory.createModelForGraph(graph)
    val triple = model.create(ns + "s7", ns + "p13", ns + "${suffix}o")
    val x1 = model.graph.stream().use { it.limit(4242).filter { t -> t.`object`.uri == ns + "${suffix}o" }.toSet() }
    model.remove(triple)
    b.consume(x1)
    b.consume(triple)
    repeat(2) {
        repeat(4) { i ->
            val x2 = model.statements(ns + "s$i", null, null)
                .filter { it.predicate.uri == ns + "p7" }.count() // 42
            b.consume(x2)
        }
        val triples = (1..2).map { s ->
            (1..2).map { p ->
                (1..2).map { o ->
                    model.create(ns + "s$s$suffix", ns + "y$p", ns + "f$o")
                }
            }.flatten()
        }.flatten()
        repeat(4) { i ->
            val x3 = model.statements(null, ns + "p$i", null).map { it.subject }.count() // 17640
            b.consume(x3)
        }
        repeat(4) { i ->
            val x4 = model.statements(null, null, ns + "v$i").map { it.subject }.count() // 17640
            b.consume(x4)
        }
        model.remove(triples)
        repeat(2) { s ->
            repeat(2) { p ->
                repeat(2) { o ->
                    val t = model.create(ns + "s$s", ns + "y$p$suffix", ns + "f$o")
                    val x5 = model.statements(ns + "s$s").toList() // 1756
                    b.consume(x5)
                    model.remove(t)
                }
            }
        }
        b.consume(triples)
    }
}

fun scenarioG_R(
    graph: Graph,
    b: Blackhole,
) {
    val ns = "http://ex#"
    val x1 = graph.stream(uri("${ns}s21"), any(), any())
        .flatMap { graph.stream(any(), any(), it.`object`) }
        .skip(4200)
        .limit(4200).use { it.count() }
    b.consume(x1)

    val x2 = graph.stream(any(), uri("${ns}p21"), uri("${ns}o21")).filter { it.predicate.uri.endsWith("21") }.toSet()
    b.consume(x2)

    val x3 = graph.size()
    b.consume(x3)

    val x4 = graph.contains(any(), uri("${ns}p21"), any())
    b.consume(x4)
}

internal fun pizzaGraph_scenarioH_RW(
    graph: Graph,
    b: Blackhole,
) {
    val ns = "http://www.co-ode.org/ontologies/pizza/pizza.owl#"

    val m = ModelFactory.createModelForGraph(graph)

    val u = m.listStatements(null, OWL2.someValuesFrom, null as RDFNode?)
        .filterKeep { it.subject.isAnon }
        .filterKeep { it.subject.hasProperty(RDF.type, OWL2.Restriction) }
        .toList()
    b.consume(u)

    check(m.contains(m.createResource(ns + "Siciliana"), null, null as RDFNode?))

    val r = m.listStatements(null, RDFS.subClassOf, null as RDFNode?).toList()
    b.consume(r)

    checkNotNull(m.listStatements(null, RDF.type, OWL2.Class).first())

    b.consume(m.createResource(ns + "GorgonzolaTopping").getProperty(RDF.type))

    val g = m.listStatements(null, OWL2.differentFrom, null as RDFNode?)
        .asSequence()
        .onEach {
            b.consume(it)
        }
        .associateBy { it.subject }
    b.consume(g)

    m.getResource(ns + "CajunSpiceTopping").addProperty(RDFS.comment, "XXX")

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
    b.consume(y)

    val f = m.listStatements(null, OWL2.disjointWith, null as RDFNode?)
        .filterKeep { it.`object`.isURIResource }
        .filterKeep { it.resource.hasProperty(RDF.type, OWL2.Class) }
        .toList()
    b.consume(f)

    m.getResource(ns + "CajunSpiceTopping").removeAll(RDFS.comment)

    val x = m.listStatements(null, null, null as RDFNode?)
        .filterKeep { it.`object`.isLiteral }
        .filterKeep { it.literal.language == "pt" }
        .toList()
    b.consume(x)

    m.getResource(ns + "Capricciosa").addProperty(RDFS.subClassOf, OWL2.Thing)

    val w = m.listStatements(null, RDF.type, OWL2.Class)
        .filterKeep { it.subject.isURIResource }
        .filterKeep { it.subject.hasProperty(RDFS.label) }
        .toSet()
    b.consume(w)

    check(!m.contains(m.createResource("X"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    check(m.contains(m.createResource(ns + "JalapenoPepperTopping"), RDFS.subClassOf, null as RDFNode?))

    check(m.contains(m.createResource(ns + "LeekTopping"), RDFS.subClassOf, m.createResource(ns + "VegetableTopping")))

    m.listStatements(null, null, OWL2.Restriction).forEach {
        b.consume(it)
    }

    m.remove(m.getResource(ns + "Capricciosa"), RDFS.subClassOf, OWL2.Thing)

    b.consume(m)
}
