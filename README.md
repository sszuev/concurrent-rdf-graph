# Concurrent RDF Graph

## Summary

A simple jvm library containing [RDF](https://www.w3.org/TR/rdf11-concepts/) graph implementations suitable for concurrent environment.

Contains two implementations:
- `SynchronizedGraph`, which uses standard java `synchronized` block
- `ReadWriteLockingGraph`, which uses `java.util.concurrent.locks.ReadWriteLock` (by default)

Each implementation is an instance of `org.apache.jena.sparql.graph.GraphWrapper` from [Apache Jena](https://github.com/apache/jena) project.

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
Benchmark                                                               (factory) Cnt         Score         Error  Units   Mode

FunctionalBenchmarks.ADD                                       SYNCHRONIZED_GRAPH  25  10327745,424 |  324216,746  ops/s  thrpt
FunctionalBenchmarks.ADD                                         RW_LOCKING_GRAPH  25   9642466,664 |  545900,257  ops/s  thrpt
FunctionalBenchmarks.ADD                                                TXN_GRAPH  25    568284,348 |   14906,968  ops/s  thrpt
FunctionalBenchmarks.ADD                                                MEM_GRAPH  25   9868883,985 |  351432,363  ops/s  thrpt
FunctionalBenchmarks.CONTAINS                                  SYNCHRONIZED_GRAPH  25  17414202,069 |  838847,529  ops/s  thrpt
FunctionalBenchmarks.CONTAINS                                    RW_LOCKING_GRAPH  25  16596013,888 |  360388,978  ops/s  thrpt
FunctionalBenchmarks.CONTAINS                                           TXN_GRAPH  25      2617,453 |     639,489  ops/s  thrpt
FunctionalBenchmarks.CONTAINS                                           MEM_GRAPH  25  17735128,066 |  332657,672  ops/s  thrpt
FunctionalBenchmarks.COUNT                                     SYNCHRONIZED_GRAPH  25  34871132,016 |   54212,935  ops/s  thrpt
FunctionalBenchmarks.COUNT                                       RW_LOCKING_GRAPH  25  30702008,214 |  124360,601  ops/s  thrpt
FunctionalBenchmarks.COUNT                                              TXN_GRAPH  25     12754,607 |   12240,855  ops/s  thrpt
FunctionalBenchmarks.COUNT                                              MEM_GRAPH  25  35705376,831 |   58186,669  ops/s  thrpt
FunctionalBenchmarks.DELETE                                    SYNCHRONIZED_GRAPH  25   6132754,574 |  128806,329  ops/s  thrpt
FunctionalBenchmarks.DELETE                                      RW_LOCKING_GRAPH  25   6006734,462 |  158163,662  ops/s  thrpt
FunctionalBenchmarks.DELETE                                             TXN_GRAPH  25    466415,443 |   34619,335  ops/s  thrpt
FunctionalBenchmarks.DELETE                                             MEM_GRAPH  25   6378279,427 |   74230,896  ops/s  thrpt
FunctionalBenchmarks.FIND_ALL                                  SYNCHRONIZED_GRAPH  25   1995921,417 |  197773,961  ops/s  thrpt
FunctionalBenchmarks.FIND_ALL                                    RW_LOCKING_GRAPH  25    731483,422 |    4945,364  ops/s  thrpt
FunctionalBenchmarks.FIND_ALL                                           TXN_GRAPH  25     27210,697 |   11762,668  ops/s  thrpt
FunctionalBenchmarks.FIND_ALL                                           MEM_GRAPH  25   6250382,955 |  558544,461  ops/s  thrpt
FunctionalBenchmarks.FIND_SOME                                 SYNCHRONIZED_GRAPH  25   9185112,808 |  167003,190  ops/s  thrpt
FunctionalBenchmarks.FIND_SOME                                   RW_LOCKING_GRAPH  25   1891931,627 |   24180,754  ops/s  thrpt
FunctionalBenchmarks.FIND_SOME                                          TXN_GRAPH  25      2585,932 |     405,423  ops/s  thrpt
FunctionalBenchmarks.FIND_SOME                                          MEM_GRAPH  25  19784548,579 | 1761162,017  ops/s  thrpt
FunctionalBenchmarks.MIXED_OPERATIONS                          SYNCHRONIZED_GRAPH  25    371974,804 |    6627,910  ops/s  thrpt
FunctionalBenchmarks.MIXED_OPERATIONS                            RW_LOCKING_GRAPH  25    160969,040 |    1328,173  ops/s  thrpt
FunctionalBenchmarks.MIXED_OPERATIONS                                   TXN_GRAPH  25      1864,759 |     911,797  ops/s  thrpt
FunctionalBenchmarks.MIXED_OPERATIONS                                   MEM_GRAPH  25    511414,126 |    4680,192  ops/s  thrpt

SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5             TXN_GRAPH  25       861,604 |      11,766  ops/s  thrpt
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5    SYNCHRONIZED_GRAPH  25      2317,995 |      14,237  ops/s  thrpt
SmallGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5      RW_LOCKING_GRAPH  25      1361,093 |       9,248  ops/s  thrpt

PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5             TXN_GRAPH  25        53,449 |       0,348  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5    SYNCHRONIZED_GRAPH  25       455,535 |       3,287  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_5x5      RW_LOCKING_GRAPH  25        40,529 |       0,714  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_8x4             TXN_GRAPH  25        42,045 |       0,159  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_8x4    SYNCHRONIZED_GRAPH  25       352,476 |       1,480  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_A_RW_8x4      RW_LOCKING_GRAPH  25        25,387 |       0,759  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5              TXN_GRAPH  25        10,475 |       0,086  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5     SYNCHRONIZED_GRAPH  25       261,004 |       2,216  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_5x5       RW_LOCKING_GRAPH  25       257,982 |       1,757  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_8x4              TXN_GRAPH  25         8,194 |       0,168  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_8x4     SYNCHRONIZED_GRAPH  25       203,590 |       0,816  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_B_R_8x4       RW_LOCKING_GRAPH  25       209,688 |       4,162  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_2x42            TXN_GRAPH  25         8,184 |       0,346  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_2x42   SYNCHRONIZED_GRAPH  25       183,167 |       2,297  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_2x42     RW_LOCKING_GRAPH  25        45,588 |       0,512  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14            TXN_GRAPH  25        17,841 |       0,509  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14   SYNCHRONIZED_GRAPH  25       380,125 |       3,195  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_C_RW_8x14     RW_LOCKING_GRAPH  25        81,118 |       0,655  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DB_RW_6x14           TXN_GRAPH  25        12,359 |       0,188  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DB_RW_6x14  SYNCHRONIZED_GRAPH  25       241,269 |       2,998  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_DB_RW_6x14    RW_LOCKING_GRAPH  25        57,992 |       0,415  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5              TXN_GRAPH  25       134,988 |       5,140  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5     SYNCHRONIZED_GRAPH  25      1165,756 |      15,443  ops/s  thrpt
PizzaGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_5x5       RW_LOCKING_GRAPH  25      1144,433 |       8,823  ops/s  thrpt

BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_2x420              TXN_GRAPH  25         1,446 |       0,041  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_2x420     SYNCHRONIZED_GRAPH  25        18,554 |       0,204  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_D_W_2x420       RW_LOCKING_GRAPH  25        20,452 |       0,315  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x5               TXN_GRAPH  25         4,954 |       0,198  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x5      SYNCHRONIZED_GRAPH  25         3,900 |       0,303  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_F_RW_5x5        RW_LOCKING_GRAPH  25         2,894 |       0,144  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x21               TXN_GRAPH  25         0,459 |       0,005  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x21      SYNCHRONIZED_GRAPH  25       124,310 |       1,384  ops/s  thrpt
BigGraphConcurrentBenchmarks.CONCURRENT_SCENARIO_G_R_4x21        RW_LOCKING_GRAPH  25       120,663 |      11,241  ops/s  thrpt

```