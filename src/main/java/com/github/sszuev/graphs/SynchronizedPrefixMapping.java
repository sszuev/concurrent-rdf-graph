package com.github.sszuev.graphs;

import org.apache.jena.shared.PrefixMapping;

import java.util.Map;

/**
 * Synchronized PrefixMapping.
 */
public class SynchronizedPrefixMapping implements PrefixMapping {
    private final PrefixMapping pm;
    private final Object lock;

    public SynchronizedPrefixMapping(PrefixMapping pm, Object lock) {
        this.pm = pm;
        this.lock = lock;
    }

    @Override
    public synchronized PrefixMapping setNsPrefix(String prefix, String uri) {
        synchronized (lock) {
            pm.setNsPrefix(prefix, uri);
            return this;
        }
    }

    @Override
    public synchronized PrefixMapping removeNsPrefix(String prefix) {
        synchronized (lock) {
            pm.removeNsPrefix(prefix);
            return this;
        }
    }

    @Override
    public synchronized PrefixMapping clearNsPrefixMap() {
        synchronized (lock) {
            pm.clearNsPrefixMap();
            return this;
        }
    }

    @Override
    public synchronized PrefixMapping setNsPrefixes(PrefixMapping other) {
        synchronized (lock) {
            pm.setNsPrefixes(other);
            return this;
        }
    }

    @Override
    public synchronized PrefixMapping setNsPrefixes(Map<String, String> map) {
        synchronized (lock) {
            pm.setNsPrefixes(map);
            return this;
        }
    }

    @Override
    public synchronized PrefixMapping withDefaultMappings(PrefixMapping map) {
        synchronized (lock) {
            pm.withDefaultMappings(pm);
            return this;
        }
    }

    @Override
    public synchronized String getNsPrefixURI(String prefix) {
        synchronized (lock) {
            return pm.getNsPrefixURI(prefix);
        }
    }

    @Override
    public synchronized String getNsURIPrefix(String uri) {
        synchronized (lock) {
            return pm.getNsPrefixURI(uri);
        }
    }

    @Override
    public synchronized Map<String, String> getNsPrefixMap() {
        synchronized (lock) {
            return pm.getNsPrefixMap();
        }
    }

    @Override
    public synchronized String expandPrefix(String prefixed) {
        synchronized (lock) {
            return pm.expandPrefix(prefixed);
        }
    }

    @Override
    public synchronized String shortForm(String uri) {
        synchronized (lock) {
            return pm.shortForm(uri);
        }
    }

    @Override
    public synchronized String qnameFor(String uri) {
        synchronized (lock) {
            return pm.qnameFor(uri);
        }
    }

    @Override
    public synchronized PrefixMapping lock() {
        synchronized (lock) {
            pm.lock();
            return this;
        }
    }

    @Override
    public synchronized int numPrefixes() {
        synchronized (lock) {
            return pm.numPrefixes();
        }
    }

    @Override
    public synchronized boolean samePrefixMappingAs(PrefixMapping other) {
        synchronized (lock) {
            return pm.samePrefixMappingAs(other);
        }
    }
}


