package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.junit.jupiter.api.Assertions
import java.util.UUID
import kotlin.streams.toList

internal const val ns = "https://ex.com#"

internal fun testModifyAndRead(
    graph: Graph,
    expected: List<Triple>,
    minSize: Int,
    maxSize: Int,
    numTriplesToCreate: Int,
    numTriplesToDelete: Int
) {
    val newTriples = (1..numTriplesToCreate).map {
        testFindSome(graph, expected)
        testFindAll(graph, minSize, maxSize)
        createRandom().also {
            graph.add(it)
        }
    }.toMutableList()

    repeat(numTriplesToDelete) {
        testFindSome(graph, expected)
        testFindAll(graph, minSize, maxSize)
        val t1 = createRandom()
        graph.remove(t1.subject, t1.predicate, t1.`object`)

        testFindSome(graph, expected)
        testFindAll(graph, minSize, maxSize)

        val t2 = newTriples.removeAt(0)
        graph.delete(t2)
    }
    testFindSome(graph, expected)
    testFindAll(graph, minSize, maxSize)
}

internal fun testFindAll(
    graph: Graph,
    minSize: Int,
    maxSize: Int,
) {
    Assertions.assertTrue(graph.size() in minSize..maxSize) {
        "actual graph size: ${graph.size()}, bounds = [$maxSize .. $maxSize]"
    }
    val findAll = graph.find().toList()
    Assertions.assertTrue(findAll.size in minSize..maxSize) {
        "findAllCount ${findAll.size}, bounds = [$maxSize .. $maxSize]"
    }
    val streamAll = graph.stream().toSet()
    Assertions.assertTrue(streamAll.size in minSize..maxSize) {
        "streamAllCount ${streamAll.size}, bounds = [$maxSize .. $maxSize]"
    }
    if (minSize > 0) {
        Assertions.assertFalse(graph.isEmpty)
    }
}

internal fun testFindSome(
    actual: Graph,
    expected: List<Triple>,
) {
    val t1 = createRandom()
    Assertions.assertFalse(actual.contains(t1))
    if (expected.isEmpty()) {
        return
    }

    val t2 = expected.random()
    Assertions.assertTrue(actual.contains(t2))

    val t3 =
        Triple.create(NodeFactory.createBlankNode(), expected.random().predicate, expected.random().`object`)
    Assertions.assertFalse(actual.contains(t3))

    val t4 = expected.random()
    assertFail { actual.find(t4).asSequence().single() }

    val t5 = expected.random()
    Assertions.assertEquals(listOf(t5), actual.find(t5.subject, t5.predicate, t5.`object`).toList())

    val t6 = expected.random()
    val s6 = t6.subject
    val p6 = t6.predicate
    Assertions.assertEquals(expected.filter { p6 == it.predicate }.toSet(), actual.find(null, p6, null).toSet())
    Assertions.assertEquals(expected.filter { s6 == it.subject }.toSet(), actual.find(s6, null, null).toSet())
    Assertions.assertEquals(
        expected.filter { s6 == it.subject && p6 == it.predicate }.toSet(),
        actual.find(s6, p6, null).toSet()
    )

    val t7 = expected.random()
    Assertions.assertEquals(listOf(t7), actual.stream(t7.subject, t7.predicate, t7.`object`).toList())

    val t8 = expected.random()
    val p8 = t8.predicate
    val o8 = t8.`object`
    Assertions.assertEquals(
        expected.filter { p8 == it.predicate }.toSet(),
        actual.stream(null, p8, null).toSet()
    )
    Assertions.assertEquals(
        expected.filter { o8 == it.`object` }.toSet(),
        actual.find(null, null, o8).toSet()
    )
    Assertions.assertEquals(
        expected.filter { p8 == it.predicate && o8 == it.`object` }.toSet(),
        actual.find(null, p8, o8).toSet()
    )

}

internal fun createRandom(): Triple {
    return Triple.create(
        NodeFactory.createURI(createRandomUri()),
        NodeFactory.createURI(createRandomUri()),
        NodeFactory.createURI(createRandomUri()),
    )
}

internal fun createRandomUri(): String {
    return ns + UUID.randomUUID()
}

internal fun assertFail(block: () -> Unit) {
    try {
        block()
    } catch (ex: AssertionError) {
        throw ex
    } catch (ex: Exception) {
        Assertions.fail<Unit>(ex)
    }
}
