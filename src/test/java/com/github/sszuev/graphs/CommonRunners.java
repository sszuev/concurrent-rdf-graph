package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CommonRunners {
    public static final long EXECUTE_TIMEOUT_MS = 60 * 1000L;

    public static void runTestScenario(
            Graph graph,
            int numberOfThreads,
            int innerIterations,
            GraphScenario scenario) {
        runTestScenario(graph, numberOfThreads, innerIterations, (g, tid, iid) -> scenario.run(g));
    }

    public static void runTestScenario(
            Graph graph,
            int numberOfThreads,
            int innerIterations,
            DetailedGraphScenario scenario) {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        List<Future<Void>> tasks = new ArrayList<>();
        for (int ti = 1; ti <= numberOfThreads; ti++) {
            final int tid = ti;
            tasks.add(executor.submit(() -> {
                for (int ii = 0; ii < innerIterations; ii++) {
                    try {
                        barrier.await(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        scenario.run(graph, tid, ii);
                    } catch (Exception ex) {
                        executor.shutdownNow();
                        throw ex;
                    }
                }
                return null;
            }));
        }
        executor.shutdown();
        try {
            Assertions.assertTrue(executor.awaitTermination(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        AssertionError error = new AssertionError();
        tasks.forEach(task -> {
            try {
                task.get(EXECUTE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                error.addSuppressed(ex);
            }
        });
        if (error.getSuppressed().length > 0) {
            throw error;
        }
    }

    public interface GraphScenario {
        void run(Graph graph);
    }

    public interface DetailedGraphScenario {
        void run(Graph graph, int tid, int iid) throws Exception;
    }
}


