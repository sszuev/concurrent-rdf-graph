package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import static com.github.sszuev.graphs.TestData.smallGraph;

public class CommonScenarios {
    public static void scenarioC_modifyAndRead(Graph graph) {
        smallGraph.find().forEach(it -> {
            graph.add(it);
            graph.find(it.getSubject(), Node.ANY, Node.ANY);
            graph.find(Node.ANY, it.getPredicate(), Node.ANY);
            graph.find(Node.ANY, it.getPredicate(), it.getObject());
        });
        smallGraph.find().forEach(graph::delete);
        smallGraph.find().forEach(it -> {
            graph.find(it.getSubject(), Node.ANY, it.getObject());
            graph.find(it.getSubject(), it.getPredicate(), Node.ANY);
            graph.find(Node.ANY, Node.ANY, it.getObject());
        });
    }
}
