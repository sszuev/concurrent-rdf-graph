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
class SynchronizedGraph(graph: Graph, lock: Any? = null) : GraphWrapper(graph), Graph {

    private val lock: Any

    init {
        this.lock = lock ?: this
    }

    @Throws(AddDeniedException::class)
    override fun add(triple: Triple) = synchronized(lock) { get().add(triple) }

    @Throws(DeleteDeniedException::class)
    override fun delete(triple: Triple) = synchronized(lock) { get().delete(triple) }

    @Throws(DeleteDeniedException::class)
    override fun remove(s: Node?, p: Node?, o: Node?) = synchronized(lock) { get().remove(s, p, o) }

    @Throws(DeleteDeniedException::class)
    override fun clear() = synchronized(lock) { get().clear() }

    override fun find(triple: Triple): ExtendedIterator<Triple> = synchronized(lock) {
        WrappedIterator.create(get().find(triple).toList().iterator())
    }

    override fun find(s: Node?, p: Node?, o: Node?): ExtendedIterator<Triple> = synchronized(lock) {
        WrappedIterator.create(get().find(s, p, o).toList().iterator())
    }

    override fun find(): ExtendedIterator<Triple> = synchronized(lock) {
        WrappedIterator.create(get().find().toList().iterator())
    }

    override fun stream(s: Node?, p: Node?, o: Node?): Stream<Triple> = synchronized(lock) {
        get().stream(s, p, o).collect(Collectors.toList()).stream()
    }

    override fun stream(): Stream<Triple> = synchronized(lock) { get().stream().collect(Collectors.toList()).stream() }

    override fun contains(s: Node?, p: Node?, o: Node?): Boolean = synchronized(lock) { get().contains(s, p, o) }

    override fun contains(t: Triple): Boolean = synchronized(lock) { get().contains(t) }

    override fun close() = synchronized(lock) { get().close() }

    override fun isClosed(): Boolean = synchronized(lock) { get().isClosed }

    override fun isIsomorphicWith(g: Graph?): Boolean = synchronized(lock) { get().isIsomorphicWith(g) }

    override fun isEmpty(): Boolean = synchronized(lock) { get().isEmpty }

    override fun size(): Int = synchronized(lock) { get().size() }

    override fun dependsOn(other: Graph): Boolean = synchronized(lock) { get().dependsOn(other) }

    override fun getTransactionHandler(): TransactionHandler = synchronized(lock) { get().transactionHandler }

    override fun getCapabilities(): Capabilities = synchronized(lock) { get().capabilities }

    override fun getEventManager(): GraphEventManager = synchronized(lock) { get().eventManager }

    override fun getPrefixMapping(): PrefixMapping = SynchronizedPrefixMapping(get().prefixMapping, lock)
}

/**
 * Synchronized [PrefixMapping].
 */
class SynchronizedPrefixMapping(private val pm: PrefixMapping, private val lock: Any) : PrefixMapping {
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