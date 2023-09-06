@file:Suppress("unused", "FunctionName")

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
open class PizzaGraphConcurrentBenchmarks {
    @Param
    var factory: TestGraphs? = null
    private var graph: Graph? = null

    @Setup(Level.Invocation)
    fun setup() {
        this.graph = checkNotNull(factory?.createNew()).also { g ->
            pizzaGraph.find().forEach {
                g.add(it)
            }
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_A_RW_8x4")
    fun runScenarioA_RW_8x4(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b, ti, ii ->
            pizzaGraph_scenarioA_RW(g, b, ti, ii)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_A_RW_5x5")
    fun runScenarioA_RW_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g,  b, ti, ii ->
            pizzaGraph_scenarioA_RW(g, b, ti, ii)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_B_R_5x5")
    fun runScenarioB_R_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g,  b ->
            pizzaGraph_scenarioB_R(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_B_R_8x4")
    fun runScenarioB_R_8x4(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b ->
            pizzaGraph_scenarioB_R(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_C_RW_8x14")
    fun runScenarioC_RW_8x14(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b ->
            scenarioC_RW(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_C_RW_2x42")
    fun runScenarioC_RW_2x42(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 2,
            innerIterations = 42,
        ) { g,  b ->
            scenarioC_RW(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_D_W_5x5")
    fun runScenarioD_W_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g, _ ->
            scenarioD_W(g)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_DB_RW_6x14")
    fun runScenarioDB_RW_6x14(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 6,
            innerIterations = 14,
        ) { g,  b, _, ii ->
            if (ii % 2 == 0) {
                scenarioD_W(g)
            } else {
                scenarioC_RW(g, b)
            }
        }
    }
}