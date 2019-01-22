package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GetReferencingVerticesTest extends GraphTestTemplate {

    @Test
    public void getReferencingVertexTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "VERTEX9TO9FIND9999999999999999999999999999999999999999999999999999999999999999999";
        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);

        String tail = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(80));

        List<String> referencingVertices = graph.getReferencingVertices(firstEdge);

        Assert.assertEquals(1, referencingVertices.size());
        Assert.assertEquals(tail, referencingVertices.get(0));

    }

    @Test
    public void getReferencingVerticesTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "VERTEX9TO9FIND9999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail1 = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(80));

        String secondTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail2 = graph.addEdges(secondTranscationHash, VertexGenerator.generateRandomEdges(80));

        List<String> referencingVertices = graph.getReferencingVertices(firstEdge);

        Assert.assertEquals(2, referencingVertices.size());
        Assert.assertEquals(tail1, referencingVertices.get(0));
        Assert.assertEquals(tail2, referencingVertices.get(1));

    }

    @Test
    public void getReferencingVertexTestWithVerticesSomewhereInTheMiddle() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String vertexToFind = "VERTEX9TO9FIND9999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(80));
        tail = graph.addEdge(tail, vertexToFind);
        tail = graph.addEdges(tail, VertexGenerator.generateRandomEdges(80));

        List<String> referencingVertices = graph.getReferencingVertices(vertexToFind);

        Assert.assertEquals(1, referencingVertices.size());
        Assert.assertEquals(tail, referencingVertices.get(0));

    }

    @Test
    public void getReferencingVerticesTestWithVerticesSomewhereInTheMiddle() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String vertexToFind = "VERTEX9TO9FIND9999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail1 = graph.addEdges(firstTranscationHash, VertexGenerator.generateRandomEdges(80));
        tail1 = graph.addEdge(tail1, vertexToFind);
        tail1 = graph.addEdges(tail1, VertexGenerator.generateRandomEdges(80));

        String secondTranscationHash = graph.startVertex(dataHash, firstEdge);
        String tail2 = graph.addEdges(secondTranscationHash, VertexGenerator.generateRandomEdges(80));
        tail2 = graph.addEdge(tail2, vertexToFind);
        tail2 = graph.addEdges(tail2, VertexGenerator.generateRandomEdges(80));

        List<String> referencingVertices = graph.getReferencingVertices(vertexToFind);

        Assert.assertEquals(2, referencingVertices.size());
        Assert.assertEquals(tail1, referencingVertices.get(0));
        Assert.assertEquals(tail2, referencingVertices.get(1));

    }

}
