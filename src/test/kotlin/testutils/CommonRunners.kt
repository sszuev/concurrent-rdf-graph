package com.github.sszuev.graphs.testutils

import org.apache.jena.graph.Graph
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal const val EXECUTE_TIMEOUT_MS = 60 * 1000L

internal fun runTestScenario(
    graph: Graph,
    numberOfThreads: Int,
    innerIterations: Int,
    scenario: (Graph) -> Unit
) {
    runTestScenario(graph, numberOfThreads, innerIterations) { g, _, _ ->
        scenario(g)
    }
}

internal fun runTestScenario(
    graph: Graph,
    numberOfThreads: Int,
    innerIterations: Int,
    scenario: (Graph, tid: Int, iid: Int) -> Unit
) {
    val executor = Executors.newFixedThreadPool(numberOfThreads)
    val barrier = CyclicBarrier(numberOfThreads)
    val tasks = (1..numberOfThreads).map { ti ->
        executor.submit {
            repeat(innerIterations) { ii ->
                barrier.await(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                try {
                    scenario(graph, ti, ii)
                } catch (ex: Exception) {
                    executor.shutdownNow()
                    throw ex
                }
            }
        }
    }
    executor.shutdown()
    executor.awaitTermination(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    val error = AssertionError()
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