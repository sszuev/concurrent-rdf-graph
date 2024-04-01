package com.github.sszuev.graphs;

import org.apache.jena.shared.PrefixMapping;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * A PrefixMapping with Read/Write Locks
 */
public class ReadWriteLockingPrefixMapping implements PrefixMapping {
    private final PrefixMapping pm;
    private final Lock readLock;
    private final Lock writeLock;

    public ReadWriteLockingPrefixMapping(PrefixMapping pm, Lock readLock, Lock writeLock) {
        this.pm = pm;
        this.readLock = readLock;
        this.writeLock = writeLock;
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        writeLock.lock();
        try {
            pm.setNsPrefix(prefix, uri);
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        writeLock.lock();
        try {
            pm.removeNsPrefix(prefix);
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PrefixMapping clearNsPrefixMap() {
        writeLock.lock();
        try {
            pm.clearNsPrefixMap();
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        writeLock.lock();
        try {
            pm.setNsPrefixes(other);
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        writeLock.lock();
        try {
            pm.setNsPrefixes(map);
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        writeLock.lock();
        try {
            pm.withDefaultMappings(pm);
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        readLock.lock();
        try {
            return pm.getNsPrefixURI(prefix);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getNsURIPrefix(String uri) {
        readLock.lock();
        try {
            return pm.getNsPrefixURI(uri);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        readLock.lock();
        try {
            return pm.getNsPrefixMap();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String expandPrefix(String prefixed) {
        readLock.lock();
        try {
            return pm.expandPrefix(prefixed);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String shortForm(String uri) {
        readLock.lock();
        try {
            return pm.shortForm(uri);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String qnameFor(String uri) {
        readLock.lock();
        try {
            return pm.qnameFor(uri);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public PrefixMapping lock() {
        writeLock.lock();
        try {
            pm.lock();
            return this;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int numPrefixes() {
        readLock.lock();
        try {
            return pm.numPrefixes();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        readLock.lock();
        try {
            return pm.samePrefixMappingAs(other);
        } finally {
            readLock.unlock();
        }
    }
}
