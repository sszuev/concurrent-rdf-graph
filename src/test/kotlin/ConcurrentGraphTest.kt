package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource


internal class ConcurrentGraphTest {

    @Suppress("unused")
    enum class TestGraphFactory {
        SYNCHRONIZED_GRAPH {
            override fun create(graph: Graph): Graph = SynchronizedGraph(graph)
        };

        abstract fun create(graph: Graph): Graph
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test empty graph using threads`(factory: TestGraphFactory) {
        val g = factory.create(GraphFactory.createGraphMem())
        testJavaMultiThreadGraphModification(
            graph = g,
            nThreads = 420,
            limitOfIterations = 42,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test empty graph using coroutines`(factory: TestGraphFactory) {
        val g = factory.create(GraphFactory.createGraphMem())
        testKotlinMultiCoroutinesGraphModification(
            graph = g,
            nCoroutines = 420,
            limitOfIterations = 42,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test non-empty graph using threads`(factory: TestGraphFactory) {
        val g = factory.create(loadGraph("/pizza.ttl"))
        testJavaMultiThreadGraphModification(
            graph = g,
            nThreads = 210,
            limitOfIterations = 84,
        )
    }

    @Timeout(timeoutInMills)
    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test non-empty graph using coroutines`(factory: TestGraphFactory) {
        val g = factory.create(loadGraph("/pizza.ttl"))
        testKotlinMultiCoroutinesGraphModification(
            graph = g,
            nCoroutines = 210,
            limitOfIterations = 84,
        )
    }

}