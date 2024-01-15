package com.github.sszuev.graphs

import com.github.sszuev.graphs.scenarious.ReadOperationsTestData
import com.github.sszuev.graphs.scenarious.scenarioE_modifyAndRead
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class SingleThreadGraphTest {

    @ParameterizedTest
    @EnumSource(TestGraphs::class)
    fun `test empty GraphMem (acceptance test)`(factory: TestGraphs) {
        scenarioE_modifyAndRead(
            graph = factory.createNew(),
            getTestData = { ReadOperationsTestData(emptyList()) },
            minSize = 0,
            maxSize = 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 42
        )
    }

    @ParameterizedTest
    @EnumSource(TestGraphs::class)
    fun `test non-empty GraphMem (acceptance test)`(factory: TestGraphs) {
        val graph = factory.createFrom(pizzaGraph)
        val min = graph.transactionRead { size() }
        scenarioE_modifyAndRead(
            graph = graph,
            getTestData = { ReadOperationsTestData(graph) },
            minSize = min,
            maxSize = min + 42,
            numTriplesToCreate = 42,
            numTriplesToDelete = 21,
        )
    }

    @ParameterizedTest
    @EnumSource(TestGraphs::class)
    fun `test ConcurrentModificationException is not thrown for concurrent graphs (acceptance test)`(factory: TestGraphs) {
        val graph = factory.createFrom(pizzaGraph)
        val subject = NodeFactory.createURI("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping")
        val it = graph.transactionRead {
            val x = graph.find(Triple.create(subject, Node.ANY, Node.ANY))
            x.next()
            x.next()
            x
        }
        graph.transactionWrite {
            graph.add(Triple.create(NodeFactory.createURI("42"), Node.ANY, Node.ANY))
        }
        graph.transactionRead {
            it.next()
            it.next()
        }
        graph.transactionWrite {
            graph.add(Triple.create(subject, Node.ANY, Node.ANY))
        }
        if (factory == TestGraphs.MEM_GRAPH) {
            Assertions.assertThrows(ConcurrentModificationException::class.java) { it.next() }
        } else {
            it.next()
        }
    }

}