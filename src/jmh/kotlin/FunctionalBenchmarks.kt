@file:Suppress("unused")

package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.any
import com.github.sszuev.graphs.testutils.count
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
        this.graph = checkNotNull(factory?.createNew()).also { g ->
            g.transactionWrite {
                smallGraph.find().forEach {
                    g.add(it)
                }
            }
        }
    }

    @Benchmark
    @Group("ADD")
    fun runAdd(eraser: Blackhole) {
        graph!!.transactionWrite { add(uri("s42"), uri("p24"), uri("o42")) }
        eraser.consume(graph)
    }

    @Benchmark
    @Group("DELETE")
    fun runDelete(eraser: Blackhole) {
        graph!!.transactionWrite { delete(uri("s1"), uri("p1"), uri("o1")) }
        eraser.consume(graph)
    }

    @Benchmark
    @Group("FIND_ALL")
    fun runFindAll(eraser: Blackhole) {
        check(graph!!.transactionRead { find().count() } == 49L)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("FIND_SOME")
    fun runFindByPredicate(eraser: Blackhole) {
        check(graph!!.transactionRead { find(any(), uri("p4"), any()).count() } == 1L)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("CONTAINS")
    fun runContains(eraser: Blackhole) {
        check(graph!!.transactionRead { contains(uri("s42"), uri("p42"), uri("o42")) })
        eraser.consume(graph)
    }

    @Benchmark
    @Group("COUNT")
    fun runSize(eraser: Blackhole) {
        check(graph!!.transactionRead { size() } == 49)
        eraser.consume(graph)
    }

    @Benchmark
    @Group("MIXED_OPERATIONS")
    fun runMixedOperations(eraser: Blackhole) {
        smallGraph_scenarioK_RW(graph!!, eraser, 42_42_42, 42_42)
    }
}

