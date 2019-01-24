package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GetCompoundVerticesTest extends GraphTestTemplate {

    @Test
    public void getCompoundVertexTest() {

        String dataHash = "DATA9TO9FIND999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);

        String tail = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(80));

        List<String> compoundVertex = graph.getCompoundVertex(dataHash);

        Assert.assertEquals(1, compoundVertex.size());
        Assert.assertEquals(tail, compoundVertex.get(0));

    }

    @Test
    public void getCompoundVerticesTest() {

        String dataHash = "DATA9TO9FIND999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail1 = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(50));

        String secondTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail2 = graph.addEdges(secondTranscationHash, VertexGenerator.generateRandomEdges(50));

        List<String> compoundVertices = graph.getCompoundVertex(dataHash);

        Assert.assertEquals(2, compoundVertices.size());
        Assert.assertEquals(tail1, compoundVertices.get(0));
        Assert.assertEquals(tail2, compoundVertices.get(1));

    }

}
