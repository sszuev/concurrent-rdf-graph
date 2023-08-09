@file:Suppress("SameParameterValue", "unused")

package com.github.sszuev.graphs

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Triple
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Group
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
open class ConcurrentBenchmarks {

    @Param("TXN_GRAPH", "SYNCHRONIZED_GRAPH", "RW_LOCKING_GRAPH")
    var factory: BenchmarkGraphFactory? = null
    private var graph: Graph? = null

    @Setup(Level.Invocation)
    fun setup() {
        this.graph = checkNotNull(factory?.newGraph()).also { g ->
            // s1,p1,[o1,o2,_:b,"x","y"]
            g.add(uri("s1"), uri("p1"), uri("o1"))
            g.add(uri("s1"), uri("p1"), uri("o2"))
            g.add(uri("s1"), uri("p1"), bnode())
            g.add(uri("s1"), uri("p1"), literal("x"))
            g.add(uri("s1"), uri("p1"), literal("y"))
            // [s2,s3,_:b2],p2,o3
            g.add(uri("s2"), uri("p2"), uri("o3"))
            g.add(uri("s3"), uri("p2"), uri("o3"))
            g.add(bnode(), uri("p2"), uri("o3"))
            // [s4,s5],p3,[o4,o5]
            g.add(uri("s4"), uri("p3"), uri("o4"))
            g.add(uri("s5"), uri("p3"), uri("o5"))
            // s6,[p4,p5,p6],o6
            g.add(uri("s6"), uri("p4"), uri("o6"))
            g.add(uri("s6"), uri("p5"), uri("o6"))
            g.add(uri("s6"), uri("p6"), uri("o6"))
            // sn,pn,on
            (7..42).forEach {
                g.add(uri("s$it"), uri("p$it"), uri("o$it"))
            }
            // total triples: 35 + 13 = 48
        }
    }

    /**
     * 5 threads
     * 5 scenario iterations per thread
     * 6 find operations in iteration
     * 4 modification operations in iteration (2 add + 2 delete
     */
    @Benchmark
    @Group("CONCURRENT_SCENARIO_1_5x5_6F_4M")
    fun runScenario1(blackhole: Blackhole) {
        runScenario(
            graph = checkNotNull(this.graph),
            blackhole = blackhole,
            numberOfThreads = 5,
            innerIterations = 5,
        ) { g, ti, ii, b ->
            scenario1(g, b, ti, ii)
        }
    }

    private fun runScenario(
        graph: Graph,
        blackhole: Blackhole,
        numberOfThreads: Int,
        innerIterations: Int,
        scenario: (Graph, tid: Int, iid: Int, Blackhole) -> Unit
    ) {
        val executorService = Executors.newFixedThreadPool(numberOfThreads)
        val barrier = CountDownLatch(numberOfThreads)
        repeat(numberOfThreads) { ti ->
            executorService.submit {
                barrier.countDown()
                barrier.await()
                repeat(innerIterations) { ii ->
                    scenario(graph, ti, ii, blackhole)
                    blackhole.consume(graph)
                }
            }
        }
        executorService.shutdown()
    }

    companion object {

        /**
         * 6 find operations, 4 modification operations (2 add + 2 delete)
         */
        private fun scenario1(
            graph: Graph,
            b: Blackhole,
            ti: Int,
            ii: Int,
        ) {
            val res1 = graph.find(uri("s1"), uri("p1"), any()).toList()
            check(5 == res1.size)
            b.consume(res1)

            val res2 = graph.find(any(), uri("p2"), any()).toList()
            check(3 == res2.size)
            b.consume(res2)

            val t1 = Triple.create(uri("s-$ti-$ii"), uri("p-$ti"), literal("x"))
            graph.add(t1)

            val res3 = graph.find(any(), uri("p3"), any()).toList()
            check(2 == res3.size)
            b.consume(res3)

            val t2 = Triple.create(bnode(), uri("p-$ti"), bnode())
            graph.add(t2)

            val res4 = graph.find().toList()
            check(48 <= res4.size)
            b.consume(res4)

            graph.delete(t1)
            graph.delete(t2)
            b.consume(t1)
            b.consume(t2)

            val res5 = graph.find(uri("s6"), any(), uri("o6")).toList()
            check(3 == res5.size)
            b.consume(res5)

            val res6 = graph.find(uri("s6"), uri("p6"), uri("o6")).toList()
            check(1 == res6.size)
            b.consume(res6)
        }
    }
}