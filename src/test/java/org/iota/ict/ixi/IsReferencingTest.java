package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

public class IsReferencingTest extends GraphTestTemplate {

    @Test
    public void isDescendantTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);

        String secondEdge = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String secondTranscationHash = graph.addEdge(firstTranscationHash, secondEdge);

        Assert.assertEquals(false, graph.isReferencing(firstTranscationHash,firstTranscationHash));
        Assert.assertEquals(false, graph.isReferencing(firstTranscationHash,secondTranscationHash));
        Assert.assertEquals(true, graph.isReferencing(secondTranscationHash,firstTranscationHash));
        Assert.assertEquals(false, graph.isReferencing(secondTranscationHash,secondTranscationHash));

    }

    @Test
    public void isDescendantInBigBundleTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80);

        String currentTail = graph.addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graph.addEdge(currentTail, lastEdge);

        Assert.assertEquals(true, graph.isReferencing(tail, firstTranscationHash));

    }

}
