package org.iota.ict.ixi;

import org.iota.ict.model.transaction.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class AddEdgeTest extends GraphTestTemplate {

    @Test
    public void addEdgeWithValidInput() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge = "THIRD9HASH99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String secondTranscationHash = graphModule1.getGraph().addEdge(firstTranscationHash, secondEdge);
        String thirdTranscationHash = graphModule1.getGraph().addEdge(secondTranscationHash, thirdEdge);

        Assert.assertEquals(3, graphModule1.getGraph().getTransactionsByHash().size());

        Transaction thirdTransaction = graphModule1.getGraph().getTransactionsByHash().get(thirdTranscationHash);
        Assert.assertEquals(thirdEdge, thirdTransaction.branchHash());
        Assert.assertEquals(secondTranscationHash, thirdTransaction.trunkHash());

        Transaction secondTransaction = graphModule1.getGraph().getTransactionsByHash().get(secondTranscationHash);
        Assert.assertEquals(secondEdge, secondTransaction.branchHash());
        Assert.assertEquals(firstTranscationHash, secondTransaction.trunkHash());

        Transaction firstTransaction = graphModule1.getGraph().getTransactionsByHash().get(firstTranscationHash);
        Assert.assertEquals(firstEdge, firstTransaction.branchHash());
        Assert.assertEquals(dataHash, firstTransaction.trunkHash());

    }

    @Test
    public void addEdgeWithInvalidInput() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge = "SECOND9HASH";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String secondTranscationHash = graphModule1.getGraph().addEdge(firstTranscationHash, secondEdge);

        Assert.assertNull(secondTranscationHash);
        Assert.assertEquals(1, graphModule1.getGraph().getTransactionsByHash().size());

    }

}
