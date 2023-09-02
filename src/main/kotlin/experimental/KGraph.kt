package com.github.sszuev.graphs.experimental

import kotlinx.coroutines.flow.Flow
import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple

interface KGraph {

    fun triples(subject: Node?, predicate: Node?, target: Node?): Flow<Triple>

    fun contains(subject: Node?, predicate: Node?, target: Node?): Boolean

    fun count(): Long

    fun add(subject: Node, predicate: Node, target: Node)

    fun delete(subject: Node, predicate: Node, target: Node)

    fun remove(subject: Node?, predicate: Node?, target: Node?)

    fun clear()

    fun close()

    fun add(triple: Triple) = add(triple.subject, triple.predicate, triple.`object`)

    fun delete(triple: Triple) = delete(triple.subject, triple.predicate, triple.`object`)

    fun contains(triple: Triple): Boolean = contains(triple.subject, triple.predicate, triple.`object`)

    fun triples(search: Triple): Flow<Triple> = triples(search.subject, search.predicate, search.`object`)

    fun isEmpty(): Boolean = !contains(Triple.ANY)
}