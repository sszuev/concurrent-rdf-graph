@file:Suppress("SameParameterValue", "unused", "FunctionName")

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
open class SmallGraphConcurrentBenchmarks {

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
                smallGraph.find().forEach {
                    g.add(it)
                }
            }
        }
    }

    /**
     * 5 threads
     * 5 scenario iterations per thread
     * 6 find operations in iteration
     * 4 modification operations in iteration (2 add + 2 delete)
     */
    @Benchmark
    @Group("CONCURRENT_SCENARIO_K_RW_5x5")
    fun runScenarioK_RW_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g, b, ti, ii ->
            smallGraph_scenarioK_RW(g, b, ti, ii)
        }
    }
}