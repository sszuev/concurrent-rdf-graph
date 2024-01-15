package com.github.sszuev.graphs.scenarious

import com.github.sszuev.graphs.testutils.runAll
import com.github.sszuev.graphs.testutils.threadLocalRandom
import com.github.sszuev.graphs.transactionRead
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.jena.graph.Graph
import org.junit.jupiter.api.Assertions
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.random.nextInt

internal fun testJavaMultiThreadEveryTaskModifications(
    graph: Graph,
    nTasks: Int,
    limitOfIterations: Int,
    executorService: ExecutorService = Executors.newFixedThreadPool(nTasks),
) {
    testEveryTaskModifyAndRead(graph, nTasks, limitOfIterations) {
        executorService.runAll(it)
    }
}

internal fun testKotlinMultiCoroutineEveryTaskModification(
    graph: Graph,
    nTasks: Int,
    limitOfIterations: Int,
    executorService: ExecutorService = Executors.newFixedThreadPool(nTasks),
) {
    testEveryTaskModifyAndRead(graph, nTasks, limitOfIterations) {
        executorService.asCoroutineDispatcher().runAll(it)
    }
}

internal fun testJavaMultiThreadSeparateReadWrite(
    graph: Graph,
    nReadThreads: Int,
    nWriteThreads: Int,
    limitOfIterations: Int,
    executorService: ExecutorService = Executors.newFixedThreadPool(nReadThreads + nWriteThreads)
) {
    testReadAndWriteTasks(
        graph = graph,
        numberOfReadTasks = nReadThreads,
        numberOfWriteTasks = nWriteThreads,
        limitOfWriteIterations = limitOfIterations,
    ) { executorService.runAll(it) }
}

private fun testEveryTaskModifyAndRead(
    graph: Graph,
    numberTasks: Int,
    limitOfIterations: Int,
    runAndWait: (Collection<Callable<Unit>>) -> Unit,
) {
    val barrier = CountDownLatch(numberTasks)
    val addCounts = mutableListOf<Int>()
    val removeCounts = mutableListOf<Int>()
    val testData = mutableListOf<ReadOperationsTestData>()
    generateSequence { Random.Default.nextInt(2..limitOfIterations) }.take(numberTasks).forEach { createCount ->
        val deleteCount = Random.Default.nextInt(1..createCount)
        addCounts.add(createCount)
        removeCounts.add(deleteCount)
        testData.add(ReadOperationsTestData(graph))
    }
    val minSize = graph.transactionRead { size() }
    val maxSize = addCounts.sum() + minSize
    val expectedFinalSize = addCounts.sum() - removeCounts.sum() + graph.transactionRead { size() }
    val testDataProvider = { testData.threadLocalRandom() }

    runAndWait(
        (0 until numberTasks).map { index ->
            Callable {
                val numTriplesToAdd = addCounts[index]
                val numTriplesToRemove = removeCounts[index]
                barrier.countDown()
                barrier.await()
                println(":::S=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
                scenarioE_modifyAndRead(
                    graph = graph,
                    getTestData = testDataProvider,
                    minSize = minSize,
                    maxSize = maxSize,
                    numTriplesToCreate = numTriplesToAdd,
                    numTriplesToDelete = numTriplesToRemove,
                )
                println(":::E=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
            }
        }.toList()
    )
    Assertions.assertEquals(expectedFinalSize, graph.transactionRead { size() })
}

private fun testReadAndWriteTasks(
    graph: Graph,
    numberOfReadTasks: Int,
    numberOfWriteTasks: Int,
    limitOfWriteIterations: Int,
    runAndWait: (Collection<Callable<Unit>>) -> Unit,
) {
    val barrier = CountDownLatch(numberOfReadTasks + numberOfWriteTasks)
    val addCounts = mutableListOf<Int>()
    val removeCounts = mutableListOf<Int>()
    val testData = mutableListOf<ReadOperationsTestData>()
    generateSequence { Random.Default.nextInt(2..limitOfWriteIterations) }.take(numberOfWriteTasks)
        .forEach { createCount ->
            val deleteCount = Random.Default.nextInt(1..createCount)
            addCounts.add(createCount)
            removeCounts.add(deleteCount)
            testData.add(ReadOperationsTestData(graph))
        }
    val minSize = graph.transactionRead { size() }
    val maxSize = addCounts.sum() + minSize
    val expectedFinalSize = addCounts.sum() - removeCounts.sum() + graph.transactionRead { size() }

    val numberOfFinishedWriteTasks = AtomicLong()
    val hasError = AtomicBoolean(false)
    val writeTasks = (0 until numberOfWriteTasks).map { index ->
        Callable {
            try {
                barrier.countDown()
                barrier.await()
                val numTriplesToAdd = addCounts[index]
                val numTriplesToRemove = removeCounts[index]
                println(":::[W]S=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
                scenarioE_modify(
                    graph = graph,
                    numTriplesToCreate = numTriplesToAdd,
                    numTriplesToDelete = numTriplesToRemove,
                )
                println(":::[W]E=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
            } catch (ex: Exception) {
                hasError.set(true)
                throw ex
            } finally {
                numberOfFinishedWriteTasks.incrementAndGet()
            }
        }
    }.toList()

    val readTasks = (0 until numberOfReadTasks).map {
        Callable {
            barrier.countDown()
            barrier.await()
            println(":::[R]S=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
            while (!hasError.get() && numberOfFinishedWriteTasks.get() < numberOfWriteTasks) {
                scenarioE_read(
                    graph,
                    testData = testData.threadLocalRandom(),
                    minSize = minSize,
                    maxSize = maxSize,
                )
            }
            println(":::[R]E=[${Thread.currentThread().id}]::${Thread.currentThread().name}")
        }
    }
    runAndWait(writeTasks + readTasks)
    graph.transactionRead {
        Assertions.assertEquals(expectedFinalSize, size())
    }
}