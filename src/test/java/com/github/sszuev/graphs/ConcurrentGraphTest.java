package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.github.sszuev.graphs.CommonRunners.EXECUTE_TIMEOUT_MS;
import static com.github.sszuev.graphs.CommonRunners.runTestScenario;
import static com.github.sszuev.graphs.TestData.pizzaGraph;

public class ConcurrentGraphTest {
    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(names = {"SYNCHRONIZED_GRAPH_V1", "SYNCHRONIZED_GRAPH_V2", "RW_LOCKING_GRAPH_V1", "RW_LOCKING_GRAPH_V2", "TXN_GRAPH"})
    public void testManyReadManyWriteForPizzaGraph2ThreadsScenarioC(TestGraphs factory) {
        Graph pizza = factory.createFrom(pizzaGraph);
        runTestScenario(
                pizza,
                2,
                4242,
                CommonScenarios::scenarioC_modifyAndRead
        );
    }

    @Timeout(EXECUTE_TIMEOUT_MS)
    @ParameterizedTest
    @EnumSource(names = {"SYNCHRONIZED_GRAPH_V1", "SYNCHRONIZED_GRAPH_V2", "RW_LOCKING_GRAPH_V1", "RW_LOCKING_GRAPH_V2", "TXN_GRAPH"})
    public void testManyReadManyWriteForPizzaGraph8ThreadsScenarioC(TestGraphs factory) {
        Graph pizza = factory.createFrom(pizzaGraph);
        runTestScenario(
                pizza,
                8,
                424,
                CommonScenarios::scenarioC_modifyAndRead
        );
    }

}
