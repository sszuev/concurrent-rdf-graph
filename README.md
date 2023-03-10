# Concurrent RDF Graph

## Summary

A simple jvm library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for concurrent environment.

Contains two implementations:
- `SynchronizedGraph`, which uses standard java `synchronized` block
- `ReadWriteLockingGraph`, which uses `java.util.concurrent.locks.ReadWriteLock` (by default)

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper` from [Apache Jena](https://github.com/apache/jena) project.

In general, the RW-locking graph implementation is faster and less memory-demanding.