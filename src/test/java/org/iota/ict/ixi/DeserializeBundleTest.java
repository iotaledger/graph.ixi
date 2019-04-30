package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Pair;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.Trytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeserializeBundleTest extends GraphTestTemplate {

    @Test
    public void deserializeSingleVertexFromOneBundleTest() {

        // create first vertex

        String dataHash1 = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge1 = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge1 = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge1 = "THIRD9HASH99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash1 = graphModule1.getGraph().startVertex(dataHash1, firstEdge1);
        String secondTranscationHash1 = graphModule1.getGraph().addEdge(firstTranscationHash1, secondEdge1);
        String thirdTranscationHash1 = graphModule1.getGraph().addEdge(secondTranscationHash1, thirdEdge1);

        List<TransactionBuilder> transactionBuilderList1 = graphModule1.finalizeVertex(thirdTranscationHash1);

        // generating bundle

        Bundle bundle = graphModule1.serialize(new Pair<>(thirdTranscationHash1, transactionBuilderList1));

        Assert.assertEquals(1, bundle.getTransactions().size());

        Transaction vertex1 = bundle.getTransactions().get(0);

        Assert.assertEquals(0, Trytes.toTrits(vertex1.tag())[0]);
        Assert.assertEquals(1, Trytes.toTrits(vertex1.tag())[1]);
        Assert.assertEquals(1, Trytes.toTrits(vertex1.tag())[2]);

        graphModule1.getGraph().getTransactionsByHash().clear();
        graphModule1.deserializeAndStore(bundle);
        Assert.assertEquals(3, graphModule1.getGraph().getTransactionsByHash().size());

        List<Transaction> list = new ArrayList(graphModule1.getGraph().getTransactionsByHash().values());

        Transaction transaction1 = list.get(0);
        Transaction transaction2 = list.get(1);
        Transaction transaction3 = list.get(2);

        Assert.assertEquals(dataHash1, transaction1.trunkHash());
        Assert.assertEquals(firstEdge1, transaction1.branchHash());

        Assert.assertEquals(transaction1.hash, transaction2.trunkHash());
        Assert.assertEquals(secondEdge1, transaction2.branchHash());

        Assert.assertEquals(transaction2.hash, transaction3.trunkHash());
        Assert.assertEquals(thirdEdge1, transaction3.branchHash());

    }

    @Test
    public void deserializeMultipleVerticesFromOneBundleTest() {

        // create first vertex

        String dataHash1 = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge1 = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge1 = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge1 = "THIRD9HASH99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash1 = graphModule1.getGraph().startVertex(dataHash1, firstEdge1);
        String secondTranscationHash1 = graphModule1.getGraph().addEdge(firstTranscationHash1, secondEdge1);
        String thirdTranscationHash1 = graphModule1.getGraph().addEdge(secondTranscationHash1, thirdEdge1);

        List<TransactionBuilder> transactionBuilderList1 = graphModule1.finalizeVertex(thirdTranscationHash1);

        // create second vertex

        String dataHash2 = "ANOTHER9DATA9HASH9999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge2 = "ANOTHER9FIRST9EDGE999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge2 = "ANOTHER9SECOND9HASH99999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge2 = "ANOTHER9THIRD9HASH999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash2 = graphModule1.getGraph().startVertex(dataHash2, firstEdge2);
        String secondTranscationHash2 = graphModule1.getGraph().addEdge(firstTranscationHash2, secondEdge2);
        String thirdTranscationHash2 = graphModule1.getGraph().addEdge(secondTranscationHash2, thirdEdge2);

        List<TransactionBuilder> transactionBuilderList2 = graphModule1.finalizeVertex(thirdTranscationHash2);

        // generating bundle from both vertices

        Bundle bundle = graphModule1.serialize(new Pair(thirdTranscationHash1, transactionBuilderList1), new Pair(thirdTranscationHash2, transactionBuilderList2));

        Assert.assertEquals(2, bundle.getTransactions().size());

        Transaction vertex1 = bundle.getTransactions().get(0);
        Transaction vertex2 = bundle.getTransactions().get(1);

        Assert.assertEquals(0, Trytes.toTrits(vertex1.tag())[0]);
        Assert.assertEquals(1, Trytes.toTrits(vertex1.tag())[1]);
        Assert.assertEquals(1, Trytes.toTrits(vertex1.tag())[2]);

        Assert.assertEquals(0, Trytes.toTrits(vertex2.tag())[0]);
        Assert.assertEquals(1, Trytes.toTrits(vertex2.tag())[1]);
        Assert.assertEquals(1, Trytes.toTrits(vertex2.tag())[2]);

        graphModule1.getGraph().getTransactionsByHash().clear();
        graphModule1.deserializeAndStore(bundle);
        Assert.assertEquals(6, graphModule1.getGraph().getTransactionsByHash().size());

        List<Transaction> list = new ArrayList<>(graphModule1.getGraph().getTransactionsByHash().values());

        Transaction transaction1 = list.get(0);
        Transaction transaction2 = list.get(1);
        Transaction transaction3 = list.get(2);
        Transaction transaction4 = list.get(3);
        Transaction transaction5 = list.get(4);
        Transaction transaction6 = list.get(5);

        // check vertex 1

        Assert.assertEquals(dataHash1, transaction1.trunkHash());
        Assert.assertEquals(firstEdge1, transaction1.branchHash());

        Assert.assertEquals(transaction1.hash, transaction2.trunkHash());
        Assert.assertEquals(secondEdge1, transaction2.branchHash());

        Assert.assertEquals(transaction2.hash, transaction3.trunkHash());
        Assert.assertEquals(thirdEdge1, transaction3.branchHash());

        // check vertex 2

        Assert.assertEquals(dataHash2, transaction4.trunkHash());
        Assert.assertEquals(firstEdge2, transaction4.branchHash());

        Assert.assertEquals(transaction4.hash, transaction5.trunkHash());
        Assert.assertEquals(secondEdge2, transaction5.branchHash());

        Assert.assertEquals(transaction5.hash, transaction6.trunkHash());
        Assert.assertEquals(thirdEdge2, transaction6.branchHash());

    }

}
