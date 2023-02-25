package com.github.sszuev.graphs

import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.api.Test

internal class SingleThreadGraphTest {

    @Test
    fun `test empty GraphMem (acceptance test)`() {
        testModifyAndRead(
            graph = GraphFactory.createGraphMem(),
            expected = emptyList(),
            minSize = 0,
            maxSize = 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 42
        )
    }

    @Test
    fun `test non-empty GraphMem (acceptance test)`() {
        val graph = loadGraph("/pizza.ttl")
        val min = graph.size()
        testModifyAndRead(
            graph = graph,
            expected = emptyList(),
            minSize = min,
            maxSize = min + 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 21,
        )
    }
}