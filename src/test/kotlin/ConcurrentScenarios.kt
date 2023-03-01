package com.github.sszuev.graphs

import com.github.sszuev.graphs.testutils.Barrier
import com.github.sszuev.graphs.testutils.CoroutineBarrier
import com.github.sszuev.graphs.testutils.ThreadBarrier
import com.github.sszuev.graphs.testutils.assertSafe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.jena.graph.Graph
import org.junit.jupiter.api.Assertions
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

internal const val timeoutInMills = 2 * 60 * 1000L

internal fun testJavaMultiThreadGraphModification(
    graph: Graph,
    nThreads: Int,
    limitOfIterations: Int,
    executorService: ExecutorService = Executors.newFixedThreadPool(nThreads)
) {
    testGraphModification(graph, nThreads, limitOfIterations, { ThreadBarrier(it) }) { tasks ->
        val futures = executorService.invokeAll(tasks)
        executorService.shutdown()
        executorService.awaitTermination(timeoutInMills, TimeUnit.MILLISECONDS)
        futures.forEach {
            assertSafe {
                it.get()
            }
        }
    }
}

internal fun testKotlinMultiCoroutinesGraphModification(
    graph: Graph,
    nCoroutines: Int,
    limitOfIterations: Int,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    testGraphModification(graph, nCoroutines, limitOfIterations, { CoroutineBarrier(it) }) { tasks ->
        runBlocking(coroutineDispatcher) {
            withTimeout(timeoutInMills) {
                val jobs = tasks.map {
                    launch { it.call() }
                }
                jobs.joinAll()
            }
        }
    }
}

private fun testGraphModification(
    graph: Graph,
    numberTasks: Int,
    limitOfIterations: Int,
    createBarrier: (Int) -> Barrier,
    runAndWait: (Collection<Callable<Unit>>) -> Unit,
) {
    val barrier = createBarrier(numberTasks)
    val addCounts = mutableListOf<Int>()
    val removeCounts = mutableListOf<Int>()
    generateSequence { Random.Default.nextInt(2..limitOfIterations) }.take(numberTasks).forEach {
        addCounts.add(it)
        removeCounts.add(Random.Default.nextInt(1..it))
    }
    val triples = graph.find().toList()
    val minSize = graph.size()
    val maxSize = addCounts.sum() + minSize
    val expectedFinalSize = addCounts.sum() - removeCounts.sum() + graph.size()

    runAndWait(
        (0 until numberTasks).map { index ->
            Callable {
                val numTriplesToAdd = addCounts[index]
                val numTriplesToRemove = removeCounts[index]
                barrier.await()
                println(":::S=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
                testModifyAndRead(
                    graph = graph,
                    expected = triples,
                    minSize = minSize,
                    maxSize = maxSize,
                    numTriplesToCreate = numTriplesToAdd,
                    numTriplesToDelete = numTriplesToRemove,
                )
                println(":::E=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
            }
        }.toList()
    )
    Assertions.assertEquals(expectedFinalSize, graph.size())
}