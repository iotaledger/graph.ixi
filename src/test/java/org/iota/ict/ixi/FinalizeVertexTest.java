package org.iota.ict.ixi;

import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
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

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String secondTranscationHash = graph.addEdge(firstTranscationHash, secondEdge);
        String thirdTranscationHash = graph.addEdge(secondTranscationHash, thirdEdge);

        List<TransactionBuilder> transactionBuilderList = graph.finalizeVertex(thirdTranscationHash);
        TransactionBuilder b = transactionBuilderList.get(0);

        Assert.assertEquals(b.extraDataDigest, dataHash);
        Assert.assertEquals(b.signatureFragments, Trytes.padRight(firstEdge+secondEdge+thirdEdge, Transaction.Field.SIGNATURE_FRAGMENTS.tryteLength));
        Assert.assertEquals(1, Trytes.toTrits(b.tag)[2]); // start flag expected
        Assert.assertEquals(1, Trytes.toTrits(b.tag)[1]); // end  flag expected

    }


}
