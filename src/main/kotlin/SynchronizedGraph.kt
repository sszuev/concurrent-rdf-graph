package com.github.sszuev.graphs


import org.apache.jena.graph.*
import org.apache.jena.shared.AddDeniedException
import org.apache.jena.shared.DeleteDeniedException
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.sparql.graph.GraphWrapper
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.WrappedIterator
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Synchronized [GraphWrapper].
 *
 * Note that method [GraphWrapper.get] is not synchronized.
 * Also, components [TransactionHandler], [Capabilities], [GraphEventManager] are not synchronized.
 *
 * Complex operation like [org.apache.jena.rdf.model.Model.write] are not thread-safe.
 */
class SynchronizedGraph(graph: Graph) : GraphWrapper(graph), Graph {

    @Synchronized
    @Throws(AddDeniedException::class)
    override fun add(triple: Triple) = get().add(triple)

    @Synchronized
    @Throws(DeleteDeniedException::class)
    override fun delete(triple: Triple) = get().delete(triple)

    @Synchronized
    @Throws(DeleteDeniedException::class)
    override fun remove(s: Node?, p: Node?, o: Node?) = get().remove(s, p, o)

    @Synchronized
    @Throws(DeleteDeniedException::class)
    override fun clear() = get().clear()

    @Synchronized
    override fun find(triple: Triple?): ExtendedIterator<Triple> =
        WrappedIterator.create(get().find(triple).toList().iterator())

    @Synchronized
    override fun find(s: Node?, p: Node?, o: Node?): ExtendedIterator<Triple> =
        WrappedIterator.create(get().find(s, p, o).toList().iterator())

    @Synchronized
    override fun find(): ExtendedIterator<Triple> = WrappedIterator.create(get().find().toList().iterator())

    @Synchronized
    override fun stream(s: Node?, p: Node?, o: Node?): Stream<Triple> =
        get().stream(s, p, o).collect(Collectors.toList()).stream()

    @Synchronized
    override fun stream(): Stream<Triple> = get().stream().collect(Collectors.toList()).stream()

    @Synchronized
    override fun contains(s: Node?, p: Node?, o: Node?): Boolean = get().contains(s, p, o)

    @Synchronized
    override fun contains(t: Triple): Boolean = get().contains(t)

    @Synchronized
    override fun close() = get().close()

    @Synchronized
    override fun isClosed(): Boolean = get().isClosed

    @Synchronized
    override fun isIsomorphicWith(g: Graph?): Boolean = get().isIsomorphicWith(g)

    @Synchronized
    override fun isEmpty(): Boolean = get().isEmpty

    @Synchronized
    override fun size(): Int = get().size()

    @Synchronized
    override fun dependsOn(other: Graph): Boolean = get().dependsOn(other)

    @Synchronized
    override fun getTransactionHandler(): TransactionHandler = get().transactionHandler

    @Synchronized
    override fun getCapabilities(): Capabilities = get().capabilities

    @Synchronized
    override fun getEventManager(): GraphEventManager = get().eventManager

    @Synchronized
    override fun getPrefixMapping(): PrefixMapping = SynchronizedPrefixMapping(this, get().prefixMapping)
}

/**
 * Synchronized [PrefixMapping].
 */
class SynchronizedPrefixMapping(private val lock: Any, private val pm: PrefixMapping) : PrefixMapping {
    override fun setNsPrefix(prefix: String, uri: String): PrefixMapping =
        synchronized(lock) { also { pm.setNsPrefix(prefix, uri) } }

    override fun removeNsPrefix(prefix: String): PrefixMapping =
        synchronized(lock) { also { pm.removeNsPrefix(prefix) } }

    override fun clearNsPrefixMap(): PrefixMapping =
        synchronized(lock) { also { pm.clearNsPrefixMap() } }

    override fun setNsPrefixes(other: PrefixMapping): PrefixMapping =
        synchronized(lock) { also { pm.setNsPrefixes(other) } }

    override fun setNsPrefixes(map: Map<String, String>): PrefixMapping =
        synchronized(lock) { pm.setNsPrefixes(map) }

    override fun withDefaultMappings(map: PrefixMapping): PrefixMapping =
        synchronized(lock) { also { pm.withDefaultMappings(pm) } }

    override fun getNsPrefixURI(prefix: String): String =
        synchronized(lock) { pm.getNsPrefixURI(prefix) }

    override fun getNsURIPrefix(uri: String): String =
        synchronized(lock) { pm.getNsPrefixURI(uri) }

    override fun getNsPrefixMap(): Map<String, String> =
        synchronized(lock) { pm.nsPrefixMap }

    override fun expandPrefix(prefixed: String): String =
        synchronized(lock) { pm.expandPrefix(prefixed) }

    override fun shortForm(uri: String): String =
        synchronized(lock) { pm.shortForm(uri) }

    override fun qnameFor(uri: String): String =
        synchronized(lock) { pm.qnameFor(uri) }

    override fun lock(): PrefixMapping =
        synchronized(lock) { also { pm.lock() } }

    override fun numPrefixes(): Int =
        synchronized(lock) { pm.numPrefixes() }

    override fun samePrefixMappingAs(other: PrefixMapping): Boolean =
        synchronized(lock) { pm.samePrefixMappingAs(other) }
}