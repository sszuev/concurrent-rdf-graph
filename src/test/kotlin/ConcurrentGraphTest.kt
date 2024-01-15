package com.github.sszuev.graphs

import com.github.sszuev.graphs.scenarious.scenarioC_modifyAndRead
import com.github.sszuev.graphs.scenarious.scenarioF_modifyAndRead
import com.github.sszuev.graphs.scenarious.testJavaMultiThreadEveryTaskModifications
import com.github.sszuev.graphs.scenarious.testJavaMultiThreadSeparateReadWrite
import com.github.sszuev.graphs.scenarious.testKotlinMultiCoroutineEveryTaskModification
import com.github.sszuev.graphs.testutils.EXECUTE_TIMEOUT_MS
import com.github.sszuev.graphs.testutils.assertThrows
import com.github.sszuev.graphs.testutils.runTestScenario
import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource


internal class ConcurrentGraphTest {

    @Timeout(EXECUTE_TIMEOUT_MS)
    @Test
    fun `test rw-operations for non thread-safe graph-mem, negative acceptance test`() {
        // to be sure we have working tests
        val empty = GraphFactory.createGraphMem()
        assertThrows(Exception::class.java, AssertionError::class.java) {
            testJavaMultiThreadEveryTaskModifications(
                graph = empty,
                nTasks = 42,
                limitOfIterations = 420,
            )
        }
        val nonEmpty = loadGraph("/pizza.ttl", { GraphFactory.createGraphMem() })
        assertThrows(Exception::class.java, AssertionError::class.java) {
            testJavaMultiThreadEveryTaskModifications(
                graph = nonEmpty,
                nTasks = 42,
                limitOfIterations = 420,
            )
        }
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createNew()
        testJavaMultiThreadEveryTaskModifications(
            graph = g,
            nTasks = 210,
            limitOfIterations = 42,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for empty graph in coroutines`(factory: TestGraphs) {
        val g = factory.createNew()
        testKotlinMultiCoroutineEveryTaskModification(
            graph = g,
            nTasks = 210,
            limitOfIterations = 42,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for non-empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createFrom(pizzaGraph)
        testJavaMultiThreadEveryTaskModifications(
            graph = g,
            nTasks = 126,
            limitOfIterations = 84,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for non-empty graph in coroutines`(factory: TestGraphs) {
        val g = factory.createFrom(pizzaGraph)
        testKotlinMultiCoroutineEveryTaskModification(
            graph = g,
            nTasks = 126,
            limitOfIterations = 84,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read one write for empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createNew()
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 10,
            nWriteThreads = 1,
            limitOfIterations = 4200,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read one write for non-empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createFrom(pizzaGraph)
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 10,
            nWriteThreads = 1,
            limitOfIterations = 4200,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read few write for empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createNew()
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 17,
            nWriteThreads = 5,
            limitOfIterations = 4200,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read few write for non-empty graph in multithreading`(factory: TestGraphs) {
        val g = factory.createFrom(pizzaGraph)
        testJavaMultiThreadSeparateReadWrite(
            graph = g,
            nReadThreads = 18,
            nWriteThreads = 5,
            limitOfIterations = 4200,
        )
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for pizza graph in multithreading, scenarioC`(factory: TestGraphs) {
        val pizza = factory.createFrom(pizzaGraph)
        runTestScenario(
            graph = pizza,
            numberOfThreads = 2,
            innerIterations = 4242,
        ) { g -> scenarioC_modifyAndRead(g) }
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(
        names = [
            "SYNCHRONIZED_GRAPH_V1",
            "SYNCHRONIZED_GRAPH_V2",
            "RW_LOCKING_GRAPH_V1",
            "RW_LOCKING_GRAPH_V2",
            "TXN_GRAPH",
        ]
    )
    fun `test many read many write for big graph in multithreading, scenarioF`(factory: TestGraphs) {
        val pizza = factory.createFrom(bigGraph)
        runTestScenario(
            graph = pizza,
            numberOfThreads = 8,
            innerIterations = 13,
        ) { g -> scenarioF_modifyAndRead(g) }
    }
}
