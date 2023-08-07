# Concurrent RDF Graph

## Summary

A simple jvm library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for concurrent environment.

Contains two implementations:
- `SynchronizedGraph`, which uses standard java `synchronized` block
- `ReadWriteLockingGraph`, which uses `java.util.concurrent.locks.ReadWriteLock` (by default)

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper` from [Apache Jena](https://github.com/apache/jena) project.

In general, the RW-locking graph implementation is faster and less memory-demanding.

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
    <version>1.0.0</version>
</dependency>
```

## Run benchmarks
```bash
gradlew clean jmh
```
or 
```bash
gradlew clean jmhJar
java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar 
```
For allowed arguments see help (`java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar -help`)

## License

Apache License Version 2.0