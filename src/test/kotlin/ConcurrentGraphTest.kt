package com.github.sszuev.graphs

import com.github.sszuev.graphs.scenarious.testJavaMultiThreadEveryTaskModifications
import com.github.sszuev.graphs.scenarious.testJavaMultiThreadSeparateReadWrite
import com.github.sszuev.graphs.scenarious.testKotlinMultiCoroutineEveryTaskModification
import com.github.sszuev.graphs.scenarious.timeoutInMills
import com.github.sszuev.graphs.testutils.assertThrows
import com.github.sszuev.graphs.testutils.loadGraph
import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource


internal class ConcurrentGraphTest {

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test many read many write for empty graph in multithreading`(factory: TestGraphFactory) {
        val g = factory.create(GraphFactory.createGraphMem())
        testJavaMultiThreadEveryTaskModifications(
            graph = g,
            nTasks = 210,
            limitOfIterations = 42,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test many read many write for empty graph in coroutines`(factory: TestGraphFactory) {
        val g = factory.create(GraphFactory.createGraphMem())
        testKotlinMultiCoroutineEveryTaskModification(
            graph = g,
            nTasks = 210,
            limitOfIterations = 42,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test many read many write for non-empty graph in multithreading`(factory: TestGraphFactory) {
        val g = factory.create(loadGraph("/pizza.ttl"))
        testJavaMultiThreadEveryTaskModifications(
            graph = g,
            nTasks = 126,
            limitOfIterations = 84,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test many read many write for non-empty graph in coroutines`(factory: TestGraphFactory) {
        val g = factory.create(loadGraph("/pizza.ttl"))
        testKotlinMultiCoroutineEveryTaskModification(
            graph = g,
            nTasks = 126,
            limitOfIterations = 84,
        )
    }

    @Timeout(timeoutInMills)
    @Test
    fun `test non thread-safe rw-cycle`() {
        // to be sure we have working tests
        val empty = GraphFactory.createGraphMem()
        assertThrows(Exception::class.java, AssertionError::class.java) {
            testJavaMultiThreadEveryTaskModifications(
                graph = empty,
                nTasks = 42,
                limitOfIterations = 420,
            )
        }
        val nonEmpty = loadGraph("/pizza.ttl")
        assertThrows(Exception::class.java, AssertionError::class.java) {
            testJavaMultiThreadEveryTaskModifications(
                graph = nonEmpty,
                nTasks = 42,
                limitOfIterations = 420,
            )
        }
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test few write many read for empty graph in multithreading`(factory: TestGraphFactory) {
        val g = factory.create(GraphFactory.createGraphMem())
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 10,
            nWriteThreads = 1,
            limitOfIterations = 4200,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test few write many read for non-empty graph in multithreading`(factory: TestGraphFactory) {
        val g = factory.create(loadGraph("/pizza.ttl"))
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 10,
            nWriteThreads = 1,
            limitOfIterations = 4200,
        )
    }
}
