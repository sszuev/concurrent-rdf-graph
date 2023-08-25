package com.github.sszuev.graphs.testutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.jena.graph.Graph
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
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

internal fun ExecutorService.runAll(tasks: Collection<Callable<Unit>>) {
    val futures = invokeAll(tasks)
    shutdown()
    awaitTermination(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    val error = AssertionError("ExecutorService.runAll::fail")
    futures.forEach {
        try {
            it.get()
        } catch (e: Exception) {
            shutdownNow()
            error.addSuppressed(e)
        }
    }
    if (error.suppressed.isNotEmpty()) {
        if (error.suppressed.size == 1) {
            throw error.suppressed[0]
        } else {
            throw error
        }
    }
}

internal fun CoroutineDispatcher.runAll(tasks: Collection<Callable<Unit>>) {
    runBlocking(this) {
        withTimeout(EXECUTE_TIMEOUT_MS) {
            val jobs = tasks.map {
                launch { it.call() }
            }
            jobs.joinAll()
        }
    }
}