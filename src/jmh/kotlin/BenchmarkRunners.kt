package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal const val EXECUTE_TIMEOUT_MS = 60 * 1000L

internal fun runBenchmarkScenario(
    graph: Graph,
    blackhole: Blackhole,
    numberOfThreads: Int,
    innerIterations: Int,
    scenario: (Graph, Blackhole) -> Unit
) {
    runBenchmarkScenario(graph, blackhole, numberOfThreads, innerIterations) { g, b, _, _ ->
        scenario(g, b)
    }
}

internal fun runBenchmarkScenario(
    graph: Graph,
    blackhole: Blackhole,
    numberOfThreads: Int,
    innerIterations: Int,
    scenario: (Graph, Blackhole, tid: Int, iid: Int) -> Unit
) {
    val executor = Executors.newFixedThreadPool(numberOfThreads)
    val barrier = CyclicBarrier(numberOfThreads)
    val tasks = (1..numberOfThreads).map { ti ->
        executor.submit {
            repeat(innerIterations) { ii ->
                barrier.await(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                try {
                    scenario(graph, blackhole, ti, ii)
                } catch (ex: Exception) {
                    executor.shutdownNow()
                    throw ex
                }
                blackhole.consume(graph)
            }
        }
    }
    executor.shutdown()
    executor.awaitTermination(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    val error = IllegalStateException()
    tasks.forEach {
        try {
            it.get(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (ex: Exception) {
            error.addSuppressed(ex)
        }
    }
    if (error.suppressed.isNotEmpty()) {
        throw error
    }
}