package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.github.sszuev.graphs.TestData.pizzaGraph;

public class SingleThreadGraphTest {

    @ParameterizedTest
    @EnumSource(names = {"SYNCHRONIZED_GRAPH_V1", "SYNCHRONIZED_GRAPH_V2", "RW_LOCKING_GRAPH_V1", "RW_LOCKING_GRAPH_V2", "TXN_GRAPH"})
    void testScenarioC(TestGraphs factory) {
        Graph pizza = factory.createFrom(pizzaGraph);
        CommonScenarios.scenarioC_modifyAndRead(pizza);
        Assertions.assertEquals(1937, pizza.size());
    }
}
