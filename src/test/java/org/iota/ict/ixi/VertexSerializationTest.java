package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Pair;
import org.iota.ict.ixi.utils.VertexGenerator;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.Trytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VertexSerializationTest extends GraphTestTemplate {

    @Test
    public void serializeVertexTest() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80);

        String currentTail = graphModule1.getGraph().addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graphModule1.getGraph().addEdge(currentTail, lastEdge);

        List<TransactionBuilder> transactionBuilderList = graphModule1.finalizeVertex(tail);

        Bundle bundle = graphModule1.serialize(new Pair<>(tail, transactionBuilderList));

        Transaction firstTransaction = bundle.getTransactions().get(0);
        Transaction secondTransaction = bundle.getTransactions().get(1);
        Transaction thirdTransaction = bundle.getTransactions().get(2);
        Transaction lastTransaction = bundle.getTransactions().get(3);

        Assert.assertEquals(lastEdge, firstTransaction.signatureFragments().substring(0,81));
        Assert.assertEquals(firstEdge, lastTransaction.signatureFragments().substring(0,81));

        Assert.assertEquals(1, Trytes.toTrits(firstTransaction.tag())[2]); // start flag expected
        Assert.assertEquals(0, Trytes.toTrits(firstTransaction.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(firstTransaction.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(lastTransaction.tag())[2]);
        Assert.assertEquals(1, Trytes.toTrits(lastTransaction.tag())[1]); // end flag expected
        Assert.assertEquals(0, Trytes.toTrits(lastTransaction.tag())[0]);

    }

    @Test
    public void serializeTwoVerticesToSameBundleTest() {

        // create first vertex
        String dataHash1 = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge1 = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash1 = graphModule1.getGraph().startVertex(dataHash1, firstEdge1);
        String[] edges1 = VertexGenerator.generateRandomEdges(80);

        String currentTail1 = graphModule1.getGraph().addEdges(firstTranscationHash1, edges1);

        String lastEdge1 = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail1 = graphModule1.getGraph().addEdge(currentTail1, lastEdge1);

        // create second vertex
        String dataHash2 = "ANOTHER9DATA9HASH9999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge2 = "ANOTHER9FIRST9EDGE999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash2 = graphModule1.getGraph().startVertex(dataHash2, firstEdge2);
        String[] edges2 = VertexGenerator.generateRandomEdges(80);

        String currentTail2 = graphModule1.getGraph().addEdges(firstTranscationHash2, edges2);

        String lastEdge2 = "ANOTHER9LAST9HASH9999999999999999999999999999999999999999999999999999999999999999";
        String tail2 = graphModule1.getGraph().addEdge(currentTail2, lastEdge2);

        List<TransactionBuilder> transactionBuilderList1 = graphModule1.finalizeVertex(tail1);
        List<TransactionBuilder> transactionBuilderList2 = graphModule1.finalizeVertex(tail2);

        Bundle bundle = graphModule1.serialize(new Pair(tail1, transactionBuilderList1), new Pair(tail2, transactionBuilderList2));

        for(Transaction t: bundle.getTransactions()) {

            System.out.println("HASH: " + t.hash);
            System.out.println("ExtraData: " + t.extraDataDigest());
            System.out.println("TRUNK: " + t.trunkHash());
            System.out.println("BRANCH: " + t.branchHash());
            System.out.println("SIGNATURE: " + t.signatureFragments());
            System.out.println();

        }

        Transaction transaction1 = bundle.getTransactions().get(0);
        Transaction transaction2 = bundle.getTransactions().get(1);
        Transaction transaction3 = bundle.getTransactions().get(2);
        Transaction transaction4 = bundle.getTransactions().get(3);

        Transaction transaction5 = bundle.getTransactions().get(4);
        Transaction transaction6 = bundle.getTransactions().get(5);
        Transaction transaction7 = bundle.getTransactions().get(6);
        Transaction transaction8 = bundle.getTransactions().get(7);

        Assert.assertEquals(lastEdge1, transaction1.signatureFragments().substring(0,81));
        Assert.assertEquals(firstEdge1, transaction4.signatureFragments().substring(0,81));

        Assert.assertEquals(lastEdge2, transaction5.signatureFragments().substring(0,81));
        Assert.assertEquals(firstEdge2, transaction8.signatureFragments().substring(0,81));

        // first vertex
        Assert.assertEquals(1, Trytes.toTrits(transaction1.tag())[2]); // start flag expected
        Assert.assertEquals(0, Trytes.toTrits(transaction1.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction1.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction2.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(transaction2.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction2.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction3.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(transaction3.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction3.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction4.tag())[2]);
        Assert.assertEquals(1, Trytes.toTrits(transaction4.tag())[1]); // end flag expected
        Assert.assertEquals(0, Trytes.toTrits(transaction4.tag())[0]);

        // second vertex
        Assert.assertEquals(1, Trytes.toTrits(transaction5.tag())[2]); // start flag expected
        Assert.assertEquals(0, Trytes.toTrits(transaction5.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction5.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction6.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(transaction6.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction6.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction7.tag())[2]);
        Assert.assertEquals(0, Trytes.toTrits(transaction7.tag())[1]);
        Assert.assertEquals(0, Trytes.toTrits(transaction7.tag())[0]);

        Assert.assertEquals(0, Trytes.toTrits(transaction8.tag())[2]);
        Assert.assertEquals(1, Trytes.toTrits(transaction8.tag())[1]); // end flag expected
        Assert.assertEquals(0, Trytes.toTrits(transaction8.tag())[0]);


    }

}
