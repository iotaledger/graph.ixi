package org.iota.ict.ixi;

import org.iota.ict.ixi.utils.VertexGenerator;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.Trytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FinalizeVertexTest extends GraphTestTemplate {

    @Test
    public void finalizeVertex() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";
        String secondEdge = "SECOND9HASH9999999999999999999999999999999999999999999999999999999999999999999999";
        String thirdEdge = "THIRD9HASH99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String secondTranscationHash = graphModule1.getGraph().addEdge(firstTranscationHash, secondEdge);
        String thirdTranscationHash = graphModule1.getGraph().addEdge(secondTranscationHash, thirdEdge);

        List<TransactionBuilder> transactionBuilderList = graphModule1.finalizeVertex(thirdTranscationHash);
        TransactionBuilder b = transactionBuilderList.get(0);

        Assert.assertEquals(b.extraDataDigest, dataHash);
        Assert.assertEquals(b.signatureFragments, Trytes.padRight(thirdEdge+secondEdge+firstEdge, Transaction.Field.SIGNATURE_FRAGMENTS.tryteLength));
        Assert.assertEquals(1, Trytes.toTrits(b.tag)[2]); // start flag expected
        Assert.assertEquals(1, Trytes.toTrits(b.tag)[1]); // end  flag expected

    }

    @Test
    public void finalizeVertexWithGivenFirstAndLastEdge() {

        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule1.getGraph().startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(80);

        String currentTail = graphModule1.getGraph().addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graphModule1.getGraph().addEdge(currentTail, lastEdge);

        List<TransactionBuilder> transactionBuilderList = graphModule1.finalizeVertex(tail);

        TransactionBuilder firstTransaction = transactionBuilderList.get(0);
        TransactionBuilder secondTransaction = transactionBuilderList.get(1);
        TransactionBuilder thirdTransaction = transactionBuilderList.get(2);
        TransactionBuilder lastTransaction = transactionBuilderList.get(3);

        Assert.assertEquals(lastEdge, firstTransaction.signatureFragments.substring(0,81));
        Assert.assertEquals(firstEdge, lastTransaction.signatureFragments.substring(0,81));

        Assert.assertEquals(1, Trytes.toTrits(firstTransaction.tag)[2]); // start flag expected
        Assert.assertEquals(0, Trytes.toTrits(firstTransaction.tag)[1]);
        Assert.assertEquals(0, Trytes.toTrits(firstTransaction.tag)[0]);

        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag)[2]);
        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag)[1]);
        Assert.assertEquals(0, Trytes.toTrits(secondTransaction.tag)[0]);

        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag)[2]);
        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag)[1]);
        Assert.assertEquals(0, Trytes.toTrits(thirdTransaction.tag)[0]);

        Assert.assertEquals(0, Trytes.toTrits(lastTransaction.tag)[2]);
        Assert.assertEquals(1, Trytes.toTrits(lastTransaction.tag)[1]); // end flag expected
        Assert.assertEquals(0, Trytes.toTrits(lastTransaction.tag)[0]);

    }

}
