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
class PizzaGraphConcurrentBenchmarks {
    @Param("TXN_GRAPH", "SYNCHRONIZED_GRAPH", "RW_LOCKING_GRAPH")
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
    @Group("CONCURRENT_SCENARIO_A_8x4")
    fun runScenarioA_8x4(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b, ti, ii ->
            pizzaGraph_scenarioA(g, b, ti, ii)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_A_5x5")
    fun runScenarioA_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g,  b, ti, ii ->
            pizzaGraph_scenarioA(g, b, ti, ii)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_B_5x5")
    fun runScenarioB_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g,  b ->
            pizzaGraph_scenarioB(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_B_8x4")
    fun runScenarioB_8x4(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b ->
            pizzaGraph_scenarioB(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_C_8x14")
    fun runScenarioC_8x14(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 8,
            innerIterations = 4,
        ) { g,  b ->
            scenarioC(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_C_2x42")
    fun runScenarioC_2x42(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 2,
            innerIterations = 42,
        ) { g,  b ->
            scenarioC(g, b)
        }
    }

    @Benchmark
    @Group("CONCURRENT_SCENARIO_D_5x5")
    fun runScenarioD_5x5(blackhole: Blackhole) {
        runBenchmarkScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g,  b ->
            scenarioD(g)
        }
    }
}