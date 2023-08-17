package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.bnode
import com.github.sszuev.graphs.testutils.literal
import com.github.sszuev.graphs.testutils.uri
import org.apache.jena.graph.Graph
import org.apache.jena.graph.GraphMemFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.graph.GraphFactory
import org.apache.jena.sparql.graph.GraphTxn
import java.util.concurrent.locks.ReentrantReadWriteLock

val smallGraph: Graph = GraphFactory.createDefaultGraph().also { g ->
    // s1,p1,[o1,o2,_:b,"x","y"]
    g.add(uri("s1"), uri("p1"), uri("o1"))
    g.add(uri("s1"), uri("p1"), uri("o2"))
    g.add(uri("s1"), uri("p1"), bnode())
    g.add(uri("s1"), uri("p1"), literal("x"))
    g.add(uri("s1"), uri("p1"), literal("y"))
    // [s2,s3,_:b2],p2,o3
    g.add(uri("s2"), uri("p2"), uri("o3"))
    g.add(uri("s3"), uri("p2"), uri("o3"))
    g.add(bnode(), uri("p2"), uri("o3"))
    // [s4,s5],p3,[o4,o5]
    g.add(uri("s4"), uri("p3"), uri("o4"))
    g.add(uri("s5"), uri("p3"), uri("o5"))
    // s6,[p4,p5,p6],o6
    g.add(uri("s6"), uri("p4"), uri("o6"))
    g.add(uri("s6"), uri("p5"), uri("o6"))
    g.add(uri("s6"), uri("p6"), uri("o6"))
    // sn,pn,on
    (7..42).forEach {
        g.add(uri("s$it"), uri("p$it"), uri("o$it"))
    }
    // total triples: 35 + 13 = 48
}

// total triples: 1937
val pizzaGraph: Graph = loadGraph("/pizza.ttl", { GraphMemFactory.createGraphMem2() })

internal fun loadGraph(resource: String, factory: () -> Graph, lang: Lang? = Lang.TURTLE): Graph {
    checkNotNull(
        TestGraphs::class.java.getResourceAsStream(resource)
    ).use {
        val foundLang = lang ?: RDFDataMgr.determineLang(resource, null, null)
        val res = ModelFactory.createModelForGraph(factory())
        RDFDataMgr.read(res, it, foundLang)
        return res.graph
    }

}

@Suppress("unused")
enum class TestGraphs {
    SYNCHRONIZED_GRAPH {
        override fun createNew(): Graph = SynchronizedGraph(GraphMemFactory.createGraphMem2())
    },
    RW_LOCKING_GRAPH {
        override fun createNew(): Graph =
            ReadWriteLockingGraph(GraphMemFactory.createGraphMem2(), ReentrantReadWriteLock())
    },
    TXN_GRAPH {
        override fun createNew(): Graph = GraphTxn()
    },
    MEM_GRAPH {
        override fun createNew(): Graph = GraphMemFactory.createGraphMem2()
    },
    ;

    abstract fun createNew(): Graph

    fun createFrom(source: Graph): Graph {
        val res = createNew()
        source.find().forEach { res.add(it) }
        return res
    }
}