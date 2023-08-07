@file:Suppress("unused")

package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Group
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
open class FunctionalBenchmarks {
    @Param
    var factory: BenchmarkGraphFactory? = null
    private var graph: Graph? = null

    @Setup(Level.Invocation)
    fun setup() {
        val graph = checkNotNull(factory?.newGraph())
        repeat(42) {
            graph.add(NodeFactory.createURI("s${it}"), NodeFactory.createURI("p${it}"), NodeFactory.createURI("o${it}"))
        }
        this.graph = graph
    }

    @Benchmark
    @Group("ADD")
    fun runAdd(eraser: Blackhole) {
        graph!!.add(NodeFactory.createURI("s42"), NodeFactory.createURI("p42"), NodeFactory.createURI("o42"))
        eraser.consume(graph)
    }

    @Benchmark
    @Group("DELETE")
    fun runDelete(eraser: Blackhole) {
        graph!!.delete(NodeFactory.createURI("s1"), NodeFactory.createURI("p1"), NodeFactory.createURI("o1"))
        eraser.consume(graph)
    }

    @Benchmark
    @Group("FIND_ALL")
    fun runFindAll(eraser: Blackhole) {
        check(graph!!.find().toList().size == 42)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("FIND_SOME")
    fun runFindByPredicate(eraser: Blackhole) {
        check(graph!!.find(Node.ANY, NodeFactory.createURI("p4"), Node.ANY).toList().size == 1)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("CONTAINS")
    fun runContains(eraser: Blackhole) {
        check(
            graph!!.contains(
                NodeFactory.createURI("s4"),
                NodeFactory.createURI("p4"),
                NodeFactory.createURI("o4")
            )
        )
        eraser.consume(graph)
    }


    @Benchmark
    @Group("COUNT")
    fun runSize(eraser: Blackhole) {
        check(graph!!.size() == 42)
        eraser.consume(graph)
    }
}

