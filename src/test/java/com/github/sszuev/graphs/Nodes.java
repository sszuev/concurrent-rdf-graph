package com.github.sszuev.graphs;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

class Nodes {
    public static Node uri(String uri) {
        return NodeFactory.createURI(uri);
    }

    public static Node bnode() {
        return NodeFactory.createBlankNode();
    }

    public static Node literal(String value) {
        return NodeFactory.createLiteral(value);
    }
}
