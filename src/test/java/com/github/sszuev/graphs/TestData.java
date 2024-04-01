package com.github.sszuev.graphs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import static com.github.sszuev.graphs.Nodes.bnode;
import static com.github.sszuev.graphs.Nodes.literal;
import static com.github.sszuev.graphs.Nodes.uri;

public class TestData {

    public static Graph smallGraph = GraphFactory.createDefaultGraph();

    static {
        // s1,p1,[o1,o2,_:b,"x","y"]
        smallGraph.add(uri("s1"), uri("p1"), uri("o1"));
        smallGraph.add(uri("s1"), uri("p1"), uri("o2"));
        smallGraph.add(uri("s1"), uri("p1"), bnode());
        smallGraph.add(uri("s1"), uri("p1"), literal("x"));
        smallGraph.add(uri("s1"), uri("p1"), literal("y"));
        // [s2,s3,_:b2],p2,o3
        smallGraph.add(uri("s2"), uri("p2"), uri("o3"));
        smallGraph.add(uri("s3"), uri("p2"), uri("o3"));
        smallGraph.add(bnode(), uri("p2"), uri("o3"));
        // [s4,s5],p3,[o4,o5]
        smallGraph.add(uri("s4"), uri("p3"), uri("o4"));
        smallGraph.add(uri("s5"), uri("p3"), uri("o5"));
        // s6,[p4,p5,p6],o6
        smallGraph.add(uri("s6"), uri("p4"), uri("o6"));
        smallGraph.add(uri("s6"), uri("p5"), uri("o6"));
        smallGraph.add(uri("s6"), uri("p6"), uri("o6"));
        // sn,pn,on
        for (int i = 7; i <= 42; i++) {
            smallGraph.add(uri("s" + i), uri("p" + i), uri("o" + i));
        }
        // total triples: 36 + 13 = 49
    }

    // total triples: 1937
    public static Graph pizzaGraph = loadGraph("/pizza.ttl", GraphMemFactory::createGraphMem2, Lang.TURTLE);

    @SuppressWarnings("SameParameterValue")
    private static Graph loadGraph(String resource, Supplier<Graph> factory, Lang lang) {
        try (InputStream in = TestData.class.getResourceAsStream(resource)) {
            if (in == null) throw new AssertionError();
            Lang foundLang = lang != null ? lang : RDFDataMgr.determineLang(resource, null, null);
            Graph res = factory.get();
            RDFDataMgr.read(ModelFactory.createModelForGraph(res), in, foundLang);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
