package com.github.sszuev.graphs

import com.github.sszuev.graphs.scenarious.ReadOperationsTestData
import com.github.sszuev.graphs.scenarious.testModifyAndRead
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class SingleThreadGraphTest {

    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test empty GraphMem (acceptance test)`(factory: TestGraphFactory) {
        testModifyAndRead(
            graph = factory.create(),
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
        val graph = factory.load("/pizza.ttl")
        val min = graph.size()
        testModifyAndRead(
            graph = graph,
            getTestData = { ReadOperationsTestData(graph) },
            minSize = min,
            maxSize = min + 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 21,
        )
    }

    @ParameterizedTest
    @EnumSource(TestGraphFactory::class)
    fun `test ConcurrentModificationException is not thrown for concurrent graphs (acceptance test)`(factory: TestGraphFactory) {
        val graph = factory.load("/pizza.ttl")
        val subject = NodeFactory.createURI("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping")
        val it = graph.find(Triple.create(subject, Node.ANY, Node.ANY))
        it.next()
        it.next()
        graph.add(Triple.create(NodeFactory.createURI("42"), Node.ANY, Node.ANY))
        it.next()
        it.next()
        graph.add(Triple.create(subject, Node.ANY, Node.ANY))
        if (factory == TestGraphFactory.MEM_GRAPH) {
            Assertions.assertThrows(ConcurrentModificationException::class.java) { it.next() }
        } else {
            it.next()
        }
    }

}