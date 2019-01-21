package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GetEdgesTest extends GraphTestTemplate {

    @Test
    public void getLastEdgeTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80).stream().toArray(String[]::new);
        String tail = graph.addEdges(firstTranscationHash, edges);

        List<String> e = graph.getEdges(tail);

        Assert.assertEquals(firstEdge, e.get(e.size()-1));

    }


}
