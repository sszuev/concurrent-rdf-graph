package com.github.sszuev.graphs.scenarious

import com.github.sszuev.graphs.testutils.assertEquals
import com.github.sszuev.graphs.testutils.assertFalse
import com.github.sszuev.graphs.testutils.assertSingleOrEmpty
import com.github.sszuev.graphs.testutils.assertTrue
import com.github.sszuev.graphs.testutils.createTriple
import com.github.sszuev.graphs.testutils.threadLocalRandom
import com.github.sszuev.graphs.testutils.toSet
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Assertions
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.toList

private val testSubject = NodeFactory.createURI("https://ex.com#test-subject")
private val testPredicate = NodeFactory.createURI("https://ex.com#test-predicate")
private val testObject = NodeFactory.createURI("https://ex.com#test-object")

internal fun testModifyAndRead(
    graph: Graph,
    getTestData: () -> ReadOperationsTestData,
    minSize: Int,
    maxSize: Int,
    numTriplesToCreate: Int,
    numTriplesToDelete: Int
) {
    val newTriples = (1..numTriplesToCreate).map {
        testRead(graph, getTestData(), minSize, maxSize)
        when (ThreadLocalRandom.current().nextInt(4)) {
            0 -> createTriple()
            1 -> createTriple(subject = testSubject)
            2 -> createTriple(predicate = testPredicate)
            3 -> createTriple(value = testObject)
            else -> throw IllegalStateException()
        }.also {
            graph.add(it)
        }
    }.toMutableList()

    repeat(numTriplesToDelete) {
        testRead(graph, getTestData(), minSize, maxSize)
        val t1 = createTriple()
        graph.remove(t1.subject, t1.predicate, t1.`object`)

        testRead(graph, getTestData(), minSize, maxSize)

        val t2 = newTriples.removeAt(0)
        graph.delete(t2)
    }
    testRead(graph, getTestData(), minSize, maxSize)
}

internal fun testWrite(
    graph: Graph,
    numTriplesToCreate: Int,
    numTriplesToDelete: Int
) {
    val newTriples = (1..numTriplesToCreate).map {
        when (ThreadLocalRandom.current().nextInt(4)) {
            0 -> createTriple()
            1 -> createTriple(subject = testSubject)
            2 -> createTriple(predicate = testPredicate)
            3 -> createTriple(value = testObject)
            else -> throw IllegalStateException()
        }.also {
            graph.add(it)
        }
    }.toMutableList()
    repeat(numTriplesToDelete) {
        val t1 = createTriple()
        graph.remove(t1.subject, t1.predicate, t1.`object`)
        val t2 = newTriples.removeAt(0)
        graph.delete(t2)
    }
}

internal fun testRead(
    graph: Graph,
    testData: ReadOperationsTestData,
    minSize: Int,
    maxSize: Int,
) {

    assertTrue(graph.size() in minSize..maxSize) { "actual graph size: ${graph.size()}, bounds = [$maxSize .. $maxSize]" }
    val findAllToList = graph.find().toList()
    assertTrue(findAllToList.size in minSize..maxSize) { "findAllCount ${findAllToList.size}, bounds = [$maxSize .. $maxSize]" }
    val streamAllToSet = graph.stream().toSet()
    assertTrue(streamAllToSet.size in minSize..maxSize) { "streamAllCount ${streamAllToSet.size}, bounds = [$maxSize .. $maxSize]" }
    if (minSize > 0) {
        assertFalse(graph.isEmpty)
    }

    assertEquals(testData.containsTriple1Expected, graph.contains(testData.containsTriple1Given))

    assertEquals(testData.containsTriple2Expected, graph.contains(testData.containsTriple2Given))

    assertEquals(testData.containsTriple3Expected, graph.contains(testData.containsTriple3Given))

    Assertions.assertEquals(
        testData.findTripleAsSequenceItemExpected,
        graph.find(testData.findTripleAsSequenceItemGiven).asSequence()
            .assertSingleOrEmpty(testData.findTripleAsSequenceItemExpected != null)
    )

    Assertions.assertEquals(0, graph.find(testData.findTripleAsSequenceExpectedEmpty).asSequence().count())

    assertEquals(
        testData.findSPOToListExpected,
        graph.find(
            testData.findSPOToListGiven.subject,
            testData.findSPOToListGiven.predicate,
            testData.findSPOToListGiven.`object`
        ).toList()
    )

    assertEquals(
        testData.findSPOByPredicateToSetExpected,
        graph.find(null, testData.findSPOByPredicateToSetGiven, null).toSet()
    )
    assertEquals(
        testData.findSPOBySubjectToSetExpected,
        graph.find(testData.findSPOBySubjectToSetGiven, null, null).toSet()
    )
    assertEquals(
        testData.findSPOBySubjectAndPredicateToSetExpected,
        graph.find(
            testData.findSPOBySubjectAndPredicateToSetGiven.subject,
            testData.findSPOBySubjectAndPredicateToSetGiven.predicate,
            null
        ).toSet()
    )

    val findSPOByPredicateIteratorExpected = testData.findSPOByPredicateIteratorExpected
    val findSPOByPredicateIterator = graph.find(null, testData.findSPOByPredicateIteratorGiven, null)
    if (findSPOByPredicateIteratorExpected.isEmpty()) {
        assertFalse(findSPOByPredicateIterator.hasNext())
    } else {
        val findSPOByPredicateIteratorActual = mutableSetOf<Triple>()
        while (findSPOByPredicateIterator.hasNext()) {
            findSPOByPredicateIteratorActual.add(findSPOByPredicateIterator.next())
        }
        assertEquals(findSPOByPredicateIteratorExpected, findSPOByPredicateIteratorActual)
    }

    assertEquals(
        testData.findTripleMaskWithSubjectAndObjectToListExpected,
        graph.find(testData.findTripleMaskWithSubjectAndObjectToListGiven).toList()
    )

    assertEquals(
        testData.streamSPOToListExpected,
        graph.stream(
            testData.streamSPOToListGiven.subject,
            testData.streamSPOToListGiven.predicate,
            testData.streamSPOToListGiven.`object`
        ).toList()
    )

    assertEquals(
        testData.streamSPOByPredicateToSetExpected,
        graph.stream(null, testData.streamSPOByPredicateToSetGiven, null).toSet()
    )
    assertEquals(
        testData.streamSPOByObjectToSetExpected,
        graph.stream(null, null, testData.streamSPOByObjectToSetGiven).toSet()
    )
    assertEquals(
        testData.streamSPOByPredicateAndObjectToSetExpected,
        graph.stream(
            null,
            testData.streamSPOByPredicateAndObjectToSetGiven.predicate,
            testData.streamSPOByPredicateAndObjectToSetGiven.`object`
        ).toSet()
    )

    assertEquals(
        testData.countSPOAnonymousOWLClassesExpected,
        graph.find(null, RDF.type.asNode(), OWL.Class.asNode()).filterKeep { it.subject.isBlank }.mapWith { 1 }
            .toList().size,
    )
    assertEquals(
        testData.countSPOAnonymousOWLClassesExpected,
        graph.stream(null, RDF.type.asNode(), OWL.Class.asNode()).filter { it.subject.isBlank }.map { it.subject }
            .count()
            .toInt(),
    )

    val findTripleMaskWithTestSubjectIterator = graph.find(Triple.create(testSubject, Node.ANY, Node.ANY))
    while (findTripleMaskWithTestSubjectIterator.hasNext()) {
        findTripleMaskWithTestSubjectIterator.next()
    }
    val findTripleMaskWithTestPredicateIterator = graph.find(Triple.create(Node.ANY, testPredicate, Node.ANY))
    while (findTripleMaskWithTestPredicateIterator.hasNext()) {
        findTripleMaskWithTestPredicateIterator.next()
    }
    val findSPUByTestObjectIterator = graph.find(null, null, testObject)
    while (findSPUByTestObjectIterator.hasNext()) {
        findSPUByTestObjectIterator.next()
    }
    val findAllIterator = graph.find()
    var findAllIteratorCount = 0
    while (findAllIterator.hasNext()) {
        findAllIterator.next()
        findAllIteratorCount++
    }
    assertTrue(findAllIteratorCount in minSize..maxSize) { "findAllIterator ${findAllIteratorCount}, bounds = [$maxSize .. $maxSize]" }
}

internal data class ReadOperationsTestData(val triples: List<Triple>) {

    constructor(graph: Graph) : this(graph.find().toList())

    val containsTriple1Given: Triple = createTriple()
    val containsTriple1Expected = false

    val containsTriple2Given: Triple = getOrFindRandomTriple()
    val containsTriple2Expected = triples.isNotEmpty()

    val containsTriple3Given: Triple =
        Triple.create(
            NodeFactory.createBlankNode(),
            getOrFindRandomTriple().predicate,
            getOrFindRandomTriple().`object`
        )
    val containsTriple3Expected = false

    val findTripleAsSequenceItemGiven: Triple = getOrFindRandomTriple()
    val findTripleAsSequenceItemExpected: Triple? = if (triples.isEmpty()) null else findTripleAsSequenceItemGiven

    val findTripleAsSequenceExpectedEmpty = createTriple()

    val findSPOToListGiven: Triple = getOrFindRandomTriple()
    val findSPOToListExpected = if (triples.isEmpty()) emptyList() else listOf(findSPOToListGiven)

    val findSPOByPredicateToSetGiven: Node = getOrFindRandomTriple().predicate
    val findSPOByPredicateToSetExpected =
        triples.filter { findSPOByPredicateToSetGiven == it.predicate }.toSet()

    val findSPOBySubjectToSetGiven: Node = getOrFindRandomTriple().subject
    val findSPOBySubjectToSetExpected = triples.filter { findSPOBySubjectToSetGiven == it.subject }.toSet()

    val findSPOBySubjectAndPredicateToSetGiven: Triple =
        getOrFindRandomTriple().let { Triple.create(it.subject, it.predicate, Node.ANY) }
    val findSPOBySubjectAndPredicateToSetExpected = triples.filter {
        findSPOBySubjectAndPredicateToSetGiven.subject == it.subject && findSPOBySubjectAndPredicateToSetGiven.predicate == it.predicate
    }.toSet()

    val findSPOByPredicateIteratorGiven: Node = getOrFindRandomTriple().predicate
    val findSPOByPredicateIteratorExpected =
        triples.filter { findSPOByPredicateIteratorGiven == it.predicate }.toSet()

    val findTripleMaskWithSubjectAndObjectToListGiven: Triple =
        getOrFindRandomTriple().let { Triple.create(it.subject, Node.ANY, it.`object`) }
    val findTripleMaskWithSubjectAndObjectToListExpected = triples.filter {
        findTripleMaskWithSubjectAndObjectToListGiven.subject == it.subject && findTripleMaskWithSubjectAndObjectToListGiven.`object` == it.`object`
    }.toList()

    val streamSPOToListGiven: Triple = getOrFindRandomTriple()
    val streamSPOToListExpected = if (triples.isEmpty()) emptyList() else listOf(streamSPOToListGiven)

    val streamSPOByPredicateToSetGiven: Node = getOrFindRandomTriple().predicate
    val streamSPOByPredicateToSetExpected =
        triples.filter { streamSPOByPredicateToSetGiven == it.predicate }.toSet()

    val streamSPOByObjectToSetGiven: Node = getOrFindRandomTriple().`object`
    val streamSPOByObjectToSetExpected =
        triples.filter { streamSPOByObjectToSetGiven == it.`object` }.toSet()

    val streamSPOByPredicateAndObjectToSetGiven: Triple =
        getOrFindRandomTriple().let { Triple.create(Node.ANY, it.predicate, it.`object`) }
    val streamSPOByPredicateAndObjectToSetExpected = triples.filter {
        streamSPOByPredicateAndObjectToSetGiven.predicate == it.predicate && streamSPOByPredicateAndObjectToSetGiven.`object` == it.`object`
    }.toSet()

    val countSPOAnonymousOWLClassesExpected =
        triples.count { it.predicate == RDF.type.asNode() && it.`object` == OWL.Class.asNode() && it.subject.isBlank }

    private fun getOrFindRandomTriple(): Triple {
        return if (triples.isEmpty()) createTriple() else triples.threadLocalRandom()
    }
}