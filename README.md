# Concurrent RDF Graph

## Summary

A simple jvm library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for concurrent environment.

Contains several implementations, including

- `SynchronizedGraph` - straightforward implementation which uses the standard `synchronized` java block
- `ExtendedSynchronizedGraph` - advanced implementation which uses the standard java `synchronized` block
- `ReadWriteLockingGraph` - it uses `java.util.concurrent.locks.ReadWriteLock`

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper`
from [Apache Jena](https://github.com/apache/jena).
The project is equipped with concurrent tests, the local running of which may end with `TimeoutException` if the local
machine is not fast enough.
Also, there are benchmarks tests.

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
    <version>1.1.0-kotlin</version>
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

For allowed arguments, see help:

```bash
java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar -help
```

Example:

```bash
java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar -wi 3 -i 3 -p factory=RW_LOCKING_GRAPH_V2,SYNCHRONIZED_GRAPH
```

## License

Apache License Version 2.0