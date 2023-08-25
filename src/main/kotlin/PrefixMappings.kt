package com.github.sszuev.graphs

import org.apache.jena.shared.PrefixMapping
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock


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

/**
 * A [PrefixMapping] with Read/Write [Lock]s
 */
class ReadWriteLockingPrefixMapping(
    private val pm: PrefixMapping,
    private val readLock: Lock,
    private val writeLock: Lock,
) : PrefixMapping {

    override fun setNsPrefix(prefix: String, uri: String): PrefixMapping = writeLock.withLock {
        also { pm.setNsPrefix(prefix, uri) }
    }

    override fun removeNsPrefix(prefix: String): PrefixMapping = writeLock.withLock {
        also { pm.removeNsPrefix(prefix) }
    }

    override fun clearNsPrefixMap(): PrefixMapping = writeLock.withLock {
        also { pm.clearNsPrefixMap() }
    }

    override fun setNsPrefixes(other: PrefixMapping): PrefixMapping = writeLock.withLock {
        also { pm.setNsPrefixes(other) }
    }

    override fun setNsPrefixes(map: Map<String, String>): PrefixMapping = writeLock.withLock {
        pm.setNsPrefixes(map)
    }

    override fun withDefaultMappings(map: PrefixMapping): PrefixMapping = writeLock.withLock {
        also { pm.withDefaultMappings(pm) }
    }

    override fun getNsPrefixURI(prefix: String): String = readLock.withLock {
        pm.getNsPrefixURI(prefix)
    }

    override fun getNsURIPrefix(uri: String): String = readLock.withLock {
        pm.getNsPrefixURI(uri)
    }

    override fun getNsPrefixMap(): Map<String, String> = readLock.withLock {
        pm.nsPrefixMap
    }

    override fun expandPrefix(prefixed: String): String = readLock.withLock {
        pm.expandPrefix(prefixed)
    }

    override fun shortForm(uri: String): String = readLock.withLock {
        pm.shortForm(uri)
    }

    override fun qnameFor(uri: String): String = readLock.withLock {
        pm.qnameFor(uri)
    }

    override fun lock(): PrefixMapping = writeLock.withLock {
        also { pm.lock() }
    }

    override fun numPrefixes(): Int = readLock.withLock {
        pm.numPrefixes()
    }

    override fun samePrefixMappingAs(other: PrefixMapping): Boolean = readLock.withLock {
        pm.samePrefixMappingAs(other)
    }
}