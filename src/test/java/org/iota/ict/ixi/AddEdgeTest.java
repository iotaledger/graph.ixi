package org.iota.ict.ixi;

import org.iota.ict.model.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class AddEdgeTest extends GraphTestTemplate {

    @Test
    public void addEdgeWithValidInput() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge = "THIRD9HASH99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String secondTranscationHash = graph.addEdge(firstTranscationHash, secondEdge);
        String thirdTranscationHash = graph.addEdge(secondTranscationHash, thirdEdge);

        Assert.assertEquals(3, graph.getTransactionsByHash().size());

        Transaction thirdTransaction = graph.getTransactionsByHash().get(thirdTranscationHash);
        Assert.assertEquals(thirdEdge, thirdTransaction.branchHash);
        Assert.assertEquals(secondTranscationHash, thirdTransaction.trunkHash);

        Transaction secondTransaction = graph.getTransactionsByHash().get(secondTranscationHash);
        Assert.assertEquals(secondEdge, secondTransaction.branchHash);
        Assert.assertEquals(firstTranscationHash, secondTransaction.trunkHash);

        Transaction firstTransaction = graph.getTransactionsByHash().get(firstTranscationHash);
        Assert.assertEquals(firstEdge, firstTransaction.branchHash);
        Assert.assertEquals(dataHash, firstTransaction.trunkHash);

    }

    @Test
    public void addEdgeWithInvalidInput() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge = "SECOND9HASH";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String secondTranscationHash = graph.addEdge(firstTranscationHash, secondEdge);

        Assert.assertNull(secondTranscationHash);
        Assert.assertEquals(1, graph.getTransactionsByHash().size());

    }

}
