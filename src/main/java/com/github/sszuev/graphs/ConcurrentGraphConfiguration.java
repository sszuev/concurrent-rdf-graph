package com.github.sszuev.graphs;

/**
 * {@link #iteratorCacheChunkSize} `Int`, the size of the snapshot fragments;
 * each snapshot is collected iteratively to allow the [OuterTriplesIterator] to move forward a bit
 * between blocked collecting into snapshots;
 * in environments with a high degree of parallelism, this should speed up modification operations
 * {@link #processOldestFirst} if `true`, then collection into snapshots gives priority to those open Iterators
 * that were created earlier than others; in a highly concurrent environment, this speeds up modification operations
 */
public class ConcurrentGraphConfiguration {
    public static final ConcurrentGraphConfiguration DEFAULT = new ConcurrentGraphConfiguration(
            1024,
            false
    );

    private final int iteratorCacheChunkSize;
    private final boolean processOldestFirst;

    public ConcurrentGraphConfiguration(int iteratorCacheChunkSize, boolean processOldestFirst) {
        this.iteratorCacheChunkSize = iteratorCacheChunkSize;
        this.processOldestFirst = processOldestFirst;
    }

    public int getIteratorCacheChunkSize() {
        return iteratorCacheChunkSize;
    }

    public boolean isProcessOldestFirst() {
        return processOldestFirst;
    }
}
