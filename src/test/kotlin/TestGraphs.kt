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
    // total triples: 36 + 13 = 49
}

// total triples: 1937
val pizzaGraph: Graph = loadGraph("/pizza.ttl", { GraphMemFactory.createGraphMem2() })

// total triples: 740880
val bigGraph: Graph = GraphMemFactory.createGraphMem2().let { graph ->
    val ns = "http://ex#"
    repeat(420) { si ->
        val s = uri("${ns}s$si")
        repeat(42) { pi ->
            val p = uri("${ns}p$pi")
            repeat(42) { oi ->
                val o = uri("${ns}v$oi")
                graph.add(s, p, o)
            }
        }
    }
    graph.prefixMapping.setNsPrefix("", ns)
    graph
}

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
    SYNCHRONIZED_GRAPH_V1 {
        override fun createNew(): Graph = SimpleSynchronizedGraph(GraphMemFactory.createGraphMem2())
    },
    SYNCHRONIZED_GRAPH_V2 {
        override fun createNew(): Graph = ExtendedSynchronizedGraph(
            base = GraphMemFactory.createGraphMem2(),
            config = ConcurrentGraphConfiguration(
                iteratorCacheChunkSize = 1024,
                processOldestFirst = false,
            ),
        )
    },
    RW_LOCKING_GRAPH_V1 {
        override fun createNew(): Graph =
            ReadWriteLockingGraph(
                graph = GraphMemFactory.createGraphMem2(),
                lock = ReentrantReadWriteLock(),
                config = ConcurrentGraphConfiguration(
                    iteratorCacheChunkSize = 1024,
                    processOldestFirst = false,
                ),
            )
    },
    RW_LOCKING_GRAPH_V2 {
        override fun createNew(): Graph =
            ReadWriteLockingGraph(
                graph = GraphMemFactory.createGraphMem2(),
                lock = ReentrantReadWriteLock(),
                config = ConcurrentGraphConfiguration(
                    iteratorCacheChunkSize = 1024,
                    processOldestFirst = true
                ),
            )
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