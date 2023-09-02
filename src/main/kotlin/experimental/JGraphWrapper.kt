package com.github.sszuev.graphs.experimental

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple

internal class JGraphWrapper(private val base: Graph) : KGraph {

    override fun triples(subject: Node?, predicate: Node?, target: Node?): Flow<Triple> {
        val eit = if (subject == null && predicate == null && target == null) {
            base.find()
        } else {
            base.find(Triple.createMatch(subject, predicate, target))
        }
        return eit.asFlow().onCompletion { eit.close() }
    }

    override fun contains(subject: Node?, predicate: Node?, target: Node?): Boolean =
        base.contains(Triple.createMatch(subject, predicate, target))

    override fun count(): Long = base.size().toLong()

    override fun add(subject: Node, predicate: Node, target: Node) = base.add(subject, predicate, target)

    override fun delete(subject: Node, predicate: Node, target: Node) = base.delete(subject, predicate, target)

    override fun remove(subject: Node?, predicate: Node?, target: Node?) = base.remove(subject, predicate, target)

    override fun clear() = base.clear()

    override fun close() = base.close()
}