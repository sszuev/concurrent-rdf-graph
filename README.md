# Concurrent RDF Graph

## Summary

A simple jvm library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for concurrent environment.

Contains several implementations, including

- `SynchronizedGraph`, based straightforward use standard java `synchronized` block
- `ReadWriteLockingGraph`, uses `java.util.concurrent.locks.ReadWriteLock`

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper` from [Apache Jena](https://github.com/apache/jena) project.
The project is equipped with concurrent tests, the local running of which may end with `TimeoutException` if the local
machine is not fast enough.

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

For allowed arguments see help:

```bash
java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar -help
```

Example:

```bash
java -jar build/libs/concurrent-rdf-graph-kotlin-{version}-jmh.jar -wi 3 -i 3 -p factory=RW_LOCKING_GRAPH_V2,SYNCHRONIZED_GRAPH
```

## License

Apache License Version 2.0

## Benchmarks

```txt
Benchmark                                                                  (factory)         Score       Error  Units  Mode   Cnt

FunctionalBenchmarks.MIXED_OPERATIONS                          SYNCHRONIZED_GRAPH_V1    360874,442    6544,873  ops/s  thrpt   25
FunctionalBenchmarks.MIXED_OPERATIONS                          SYNCHRONIZED_GRAPH_V2    159884,792    2240,801  ops/s  thrpt   25
FunctionalBenchmarks.MIXED_OPERATIONS                            RW_LOCKING_GRAPH_V1    160909,886     752,778  ops/s  thrpt   25
FunctionalBenchmarks.MIXED_OPERATIONS                            RW_LOCKING_GRAPH_V2    160852,180     568,482  ops/s  thrpt   25
FunctionalBenchmarks.MIXED_OPERATIONS                                      TXN_GRAPH      4733,355    2276,425  ops/s  thrpt   25
FunctionalBenchmarks.MIXED_OPERATIONS                                      MEM_GRAPH    511464,911    7447,120  ops/s  thrpt   25
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_K_RW_5x5                TXN_GRAPH       861,948      19,946  ops/s  thrpt   25
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_K_RW_5x5    SYNCHRONIZED_GRAPH_V1      2326,383      11,281  ops/s  thrpt   25
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_K_RW_5x5    SYNCHRONIZED_GRAPH_V2      1647,428      13,200  ops/s  thrpt   25
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_K_RW_5x5      RW_LOCKING_GRAPH_V1      1360,096       8,573  ops/s  thrpt   25
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_K_RW_5x5      RW_LOCKING_GRAPH_V2      1364,187      11,645  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x6                TXN_GRAPH        44,571       0,406  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x6    SYNCHRONIZED_GRAPH_V1       388,593       0,572  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x6    SYNCHRONIZED_GRAPH_V2       196,814       2,185  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x6      RW_LOCKING_GRAPH_V1       174,625       3,216  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x6      RW_LOCKING_GRAPH_V2       175,428       6,557  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5                 TXN_GRAPH        10,474       0,041  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5     SYNCHRONIZED_GRAPH_V1       256,124       2,280  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5     SYNCHRONIZED_GRAPH_V2       202,822       1,607  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5       RW_LOCKING_GRAPH_V1       262,363       2,302  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5       RW_LOCKING_GRAPH_V2       259,216       1,256  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14               TXN_GRAPH        17,381       0,488  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14   SYNCHRONIZED_GRAPH_V1       383,599       1,189  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14   SYNCHRONIZED_GRAPH_V2       116,886       1,096  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14     RW_LOCKING_GRAPH_V1        80,700       0,751  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14     RW_LOCKING_GRAPH_V2        80,328       0,380  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DC_RW_6x14              TXN_GRAPH        12,106       0,306  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DC_RW_6x14  SYNCHRONIZED_GRAPH_V1       239,483       0,531  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DC_RW_6x14  SYNCHRONIZED_GRAPH_V2        82,769       1,018  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DC_RW_6x14    RW_LOCKING_GRAPH_V1        58,756       0,197  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DC_RW_6x14    RW_LOCKING_GRAPH_V2        57,630       0,316  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5                 TXN_GRAPH       134,213       2,101  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5     SYNCHRONIZED_GRAPH_V1      1166,114      10,125  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5     SYNCHRONIZED_GRAPH_V2      1140,289       6,863  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5       RW_LOCKING_GRAPH_V1      1125,440      18,303  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5       RW_LOCKING_GRAPH_V2      1110,576       5,923  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_H_RW_4x8                TXN_GRAPH         8,564       0,087  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_H_RW_4x8    SYNCHRONIZED_GRAPH_V1       200,555       1,549  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_H_RW_4x8    SYNCHRONIZED_GRAPH_V2       123,483       1,655  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_H_RW_4x8      RW_LOCKING_GRAPH_V1       128,941       1,356  ops/s  thrpt   25
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_H_RW_4x8      RW_LOCKING_GRAPH_V2       129,192       1,150  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_6x21                  TXN_GRAPH        27,264       0,557  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_6x21      SYNCHRONIZED_GRAPH_V1       243,342       2,209  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_6x21      SYNCHRONIZED_GRAPH_V2       242,921       3,252  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_6x21        RW_LOCKING_GRAPH_V1       247,684       3,352  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_6x21        RW_LOCKING_GRAPH_V2       246,687       1,778  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x4                  TXN_GRAPH         6,203       0,180  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x4      SYNCHRONIZED_GRAPH_V1         4,889       0,276  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x4      SYNCHRONIZED_GRAPH_V2         2,159       0,049  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x4        RW_LOCKING_GRAPH_V1         3,764       0,077  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x4        RW_LOCKING_GRAPH_V2         3,767       0,081  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x11                  TXN_GRAPH         0,887       0,004  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x11      SYNCHRONIZED_GRAPH_V1       215,077       4,978  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x11      SYNCHRONIZED_GRAPH_V2       176,964       8,118  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x11        RW_LOCKING_GRAPH_V1       197,756       6,029  ops/s  thrpt   25
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x11        RW_LOCKING_GRAPH_V2       192,724      15,460  ops/s  thrpt   25

```