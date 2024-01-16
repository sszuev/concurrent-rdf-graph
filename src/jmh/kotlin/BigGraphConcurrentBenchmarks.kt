@file:Suppress("FunctionName", "unused")

package com.github.sszuev.graphs

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
open class BigGraphConcurrentBenchmarks {

    @Param(
        "TXN_GRAPH",
        "SYNCHRONIZED_GRAPH_V1",
        "SYNCHRONIZED_GRAPH_V2",
        "RW_LOCKING_GRAPH_V1",
        "RW_LOCKING_GRAPH_V2",
        "WRAPPER_TRANSACTIONAL2_GRAPH",
    )
    var factory: TestGraphs? = null
    private var graph: Graph? = null

    @Setup(Level.Invocation)
    fun setup() {
        this.graph = checkNotNull(factory?.createNew()).also { g ->
            g.transactionWrite {
                bigGraph.find().forEach {
                    g.add(it)
                }
            }
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_F_RW_5x4")
    fun runScenarioF_RW_5x4(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 4,
        ) { g, b, ti, ii ->
            scenarioF_RW(g, b, ti, ii)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_G_R_4x11")
    fun runScenarioG_R_4x11(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 4,
            innerIterations = 11,
        ) { g, b ->
            scenarioG_R(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_D_W_6x21")
    fun runScenarioD_W_6x21(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 6,
            innerIterations = 21,
        ) { g, _ ->
            scenarioD_W(g)
        }
    }
}