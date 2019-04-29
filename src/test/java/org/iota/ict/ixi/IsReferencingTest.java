package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

public class IsReferencingTest extends GraphTestTemplate {

    @Test
    public void isDescendantTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);

        String secondEdge = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String secondTranscationHash = graphModule1.getGraph().addEdge(firstTranscationHash, secondEdge);

        Assert.assertEquals(false, graphModule1.getGraph().isReferencing(firstTranscationHash,firstTranscationHash));
        Assert.assertEquals(false, graphModule1.getGraph().isReferencing(firstTranscationHash,secondTranscationHash));
        Assert.assertEquals(true, graphModule1.getGraph().isReferencing(secondTranscationHash,firstTranscationHash));
        Assert.assertEquals(false, graphModule1.getGraph().isReferencing(secondTranscationHash,secondTranscationHash));

    }

    @Test
    public void isDescendantInBigBundleTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80);

        String currentTail = graphModule1.getGraph().addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graphModule1.getGraph().addEdge(currentTail, lastEdge);

        Assert.assertEquals(true, graphModule1.getGraph().isReferencing(tail, firstTranscationHash));

    }

}
