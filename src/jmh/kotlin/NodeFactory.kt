package com.github.sszuev.graphs

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory

internal fun uri(uri: String) = NodeFactory.createURI(uri)
internal fun bnode() = NodeFactory.createBlankNode()
internal fun literal(value: String) = NodeFactory.createLiteral(value)
internal fun any() = Node.ANY