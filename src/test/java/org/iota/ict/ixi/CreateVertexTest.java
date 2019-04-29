package org.iota.ict.ixi;

import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.utils.Trytes;
import org.junit.Assert;
import org.junit.Test;

public class CreateVertexTest extends GraphTestTemplate {

    @org.junit.Test
    public void createVertexWithValidInput() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String headHash = graphModule1.getGraph().createVertex(dataHash, new String[] { firstEdge });
        Transaction head = graphModule1.getGraph().getTransactionsByHash().get(headHash);

        Assert.assertEquals(dataHash, head.trunkHash());
        Assert.assertEquals(firstEdge, head.branchHash());
        Assert.assertEquals(1, Trytes.toTrits(head.tag())[1]);

    }

    @org.junit.Test
    public void createVertexWithInvalidDataHash() {

        String dataHash = null;
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String headHash = graphModule1.getGraph().createVertex(dataHash, new String[] { firstEdge });
        Assert.assertNull(headHash);

        dataHash = "DATA9HASH";

        headHash = graphModule1.getGraph().createVertex(dataHash, new String[] { firstEdge });
        Assert.assertNull(headHash);

        Assert.assertEquals(0, graphModule1.getGraph().getTransactionsByHash().size());

    }

    @Test
    public void createVertexWithInvalidFirstEdge() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = null;

        String headHash = graphModule1.getGraph().createVertex(dataHash, new String[] { firstEdge });
        Assert.assertNull(headHash);

        firstEdge = "FIRST9EDGE";

        headHash = graphModule1.getGraph().createVertex(dataHash, new String[] { firstEdge });
        Assert.assertNull(headHash);

        Assert.assertEquals(0, graphModule1.getGraph().getTransactionsByHash().size());

    }

}
