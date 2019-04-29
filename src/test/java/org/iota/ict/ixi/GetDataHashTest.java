package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.junit.Assert;
import org.junit.Test;

public class GetDataHashTest extends GraphTestTemplate{

    @Test
    public void getDataHashTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80);
        String tail = graphModule1.getGraph().addEdges(firstTranscationHash, edges);

        Assert.assertEquals(dataHash, graphModule1.getGraph().getData(tail));

    }

}
