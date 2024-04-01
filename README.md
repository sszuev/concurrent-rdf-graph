# Concurrent RDF Graph

## Summary

A simple java library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for
concurrent environment.

Contains several implementations, including

- `SynchronizedGraph` - straightforward implementation which uses the standard `synchronized` java block
- `ExtendedSynchronizedGraph` - advanced implementation which uses the standard java `synchronized` block
- `ReadWriteLockingGraph` - it uses `java.util.concurrent.locks.ReadWriteLock`

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper`
from [Apache Jena](https://github.com/apache/jena).

## Available via [jitpack](https://jitpack.io/#sszuev/concurrent-rdf-graph)

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
        ...
<dependency>
<groupId>com.github.sszuev</groupId>
<artifactId>concurrent-rdf-graph</artifactId>
<version>1.1.0-java</version>
</dependency>
```

## License

Apache License Version 2.0