@file:Suppress("unused")

package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.any
import com.github.sszuev.graphs.testutils.uri
import org.apache.jena.graph.Graph
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
    var factory: TestGraphs? = null
    private var graph: Graph? = null

    @Setup(Level.Invocation)
    fun setup() {
        val graph = checkNotNull(factory?.createNew())
        repeat(42) {
            graph.add(uri("s${it}"), uri("p${it}"), uri("o${it}"))
        }
        this.graph = graph
    }

    @Benchmark
    @Group("ADD")
    fun runAdd(eraser: Blackhole) {
        graph!!.add(uri("s42"), uri("p42"), uri("o42"))
        eraser.consume(graph)
    }

    @Benchmark
    @Group("DELETE")
    fun runDelete(eraser: Blackhole) {
        graph!!.delete(uri("s1"), uri("p1"), uri("o1"))
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
        check(graph!!.find(any(), uri("p4"), any()).toList().size == 1)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("CONTAINS")
    fun runContains(eraser: Blackhole) {
        check(
            graph!!.contains(
                uri("s4"),
                uri("p4"),
                uri("o4")
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

    @Benchmark
    @Group("MIXED_OPERATIONS")
    fun runMixedOperations(eraser: Blackhole) {
        smallGraph_scenarioA(graph!!, eraser, 42_42_42, 42_42)
    }
}

