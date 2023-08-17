package com.github.sszuev.graphs.testutils

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory

fun uri(uri: String): Node = NodeFactory.createURI(uri)

fun bnode(): Node = NodeFactory.createBlankNode()

fun literal(value: String): Node = NodeFactory.createLiteral(value)

fun any(): Node = Node.ANY