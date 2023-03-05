package com.github.sszuev.graphs

import com.github.sszuev.graphs.scenarious.ReadOperationsTestData
import com.github.sszuev.graphs.scenarious.testModifyAndRead
import com.github.sszuev.graphs.testutils.loadGraph
import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class SingleThreadGraphTest {

    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test empty GraphMem (acceptance test)`(factory: TestGraphFactory) {
        testModifyAndRead(
            graph = factory.create(GraphFactory.createGraphMem()),
            getTestData = { ReadOperationsTestData(emptyList()) },
            minSize = 0,
            maxSize = 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 42
        )
    }

    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test non-empty GraphMem (acceptance test)`(factory: TestGraphFactory) {
        val graph = loadGraph("/pizza.ttl")
        val min = graph.size()
        testModifyAndRead(
            graph = factory.create(graph),
            getTestData = { ReadOperationsTestData(graph) },
            minSize = min,
            maxSize = min + 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 21,
        )
    }
}