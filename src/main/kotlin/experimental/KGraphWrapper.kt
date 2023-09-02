package com.github.sszuev.graphs.experimental

import kotlinx.coroutines.Dispatchers
import org.apache.jena.graph.Capabilities
import org.apache.jena.graph.Graph
import org.apache.jena.graph.GraphEventManager
import org.apache.jena.graph.Node
import org.apache.jena.graph.TransactionHandler
import org.apache.jena.graph.Triple
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.util.iterator.ExtendedIterator
import kotlin.coroutines.CoroutineContext

class KGraphWrapper(
    private val base: KGraph,
    private val coroutineContext: CoroutineContext = Dispatchers.Default
) : Graph {

    override fun dependsOn(other: Graph?): Boolean = unsupported()

    override fun getTransactionHandler(): TransactionHandler = unsupported()

    override fun getCapabilities(): Capabilities = unsupported()

    override fun getEventManager(): GraphEventManager = unsupported()

    override fun getPrefixMapping(): PrefixMapping = unsupported()

    override fun add(t: Triple) = base.add(t)

    override fun delete(t: Triple) = base.delete(t)

    override fun find(m: Triple): ExtendedIterator<Triple> = base.triples(m).asExtendedIterator(coroutineContext)

    override fun find(s: Node?, p: Node?, o: Node?): ExtendedIterator<Triple> =
        base.triples(s, p, o).asExtendedIterator(coroutineContext)

    override fun isIsomorphicWith(g: Graph?): Boolean = unsupported()

    override fun contains(s: Node?, p: Node?, o: Node?): Boolean = base.contains(s, p, o)

    override fun contains(t: Triple): Boolean = base.contains(t)

    override fun clear() = base.clear()

    override fun remove(s: Node?, p: Node?, o: Node?) = base.remove(s, p, o)

    override fun close() = base.close()

    override fun isEmpty(): Boolean = base.isEmpty()

    override fun size(): Int = base.count().toInt()

    override fun isClosed(): Boolean = unsupported()
}

private fun <X> unsupported(): X {
    throw UnsupportedOperationException()
}