package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Pair;
import org.iota.ict.ixi.utils.VertexGenerator;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VertexEventTest extends GraphTestTemplate {

    @Test
    public void receiveSingleVertexTest() {

        // create vertex
        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graphModule2.getGraph().startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(135);

        String currentTail = graphModule2.getGraph().addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graphModule2.getGraph().addEdge(currentTail, lastEdge);

        List<TransactionBuilder> transactionBuilderList = graphModule2.finalizeVertex(tail);
        Bundle bundle = graphModule2.serialize(new Pair<>(tail, transactionBuilderList));

        // send vertex from Ict2 to Ict1
        for(Transaction transaction: bundle.getTransactions())
            ict2.submit(transaction);

        // wait few seconds to avoid premature termination of this test
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Transaction> vertices = new ArrayList<>(graphModule1.getGraph().getTransactionsByHash().values());

        Assert.assertEquals(137, vertices.size());
        Assert.assertEquals(dataHash, vertices.get(0).trunkHash());
        Assert.assertEquals(firstEdge, vertices.get(0).branchHash());
        Assert.assertEquals(lastEdge, vertices.get(136).branchHash());

    }

    @Test
    public void receiveMultipleVerticesFromSameBundleTest() {

        // create first vertex
        String dataHash1 = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge1 = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash1 = graphModule2.getGraph().startVertex(dataHash1, firstEdge1);
        String[] edges1 = VertexGenerator.generateRandomEdges(79);

        String currentTail1 = graphModule2.getGraph().addEdges(firstTranscationHash1, edges1);

        String lastEdge1 = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail1 = graphModule2.getGraph().addEdge(currentTail1, lastEdge1);

        // create second vertex
        String dataHash2 = "ANOTHER9DATA9HASH9999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge2 = "ANOTHER9FIRST9EDGE999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash2 = graphModule2.getGraph().startVertex(dataHash2, firstEdge2);
        String[] edges2 = VertexGenerator.generateRandomEdges(79);

        String currentTail2 = graphModule2.getGraph().addEdges(firstTranscationHash2, edges2);

        String lastEdge2 = "ANOTHER9LAST9HASH9999999999999999999999999999999999999999999999999999999999999999";
        String tail2 = graphModule2.getGraph().addEdge(currentTail2, lastEdge2);

        List<TransactionBuilder> transactionBuilderList1 = graphModule2.finalizeVertex(tail1);
        List<TransactionBuilder> transactionBuilderList2 = graphModule2.finalizeVertex(tail2);

        Bundle bundle = graphModule2.serialize(new Pair(tail1, transactionBuilderList1), new Pair(tail2, transactionBuilderList2));

        // send vertex from Ict2 to Ict1
        for(Transaction transaction: bundle.getTransactions())
            ict2.submit(transaction);

        // wait few seconds to avoid premature termination of this test
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Transaction> vertices = new ArrayList<>(graphModule1.getGraph().getTransactionsByHash().values());

        Assert.assertEquals(162, vertices.size());

        // check if vertices got added properly to the module graph

        if(vertices.get(0).trunkHash().equals(dataHash1)) {

            Assert.assertEquals(firstEdge1, vertices.get(0).branchHash());
            Assert.assertEquals(lastEdge1, vertices.get(80).branchHash());
            Assert.assertEquals(true, graphModule1.getGraph().isReferencing(vertices.get(80).hash, dataHash1));

            Assert.assertEquals(dataHash2, vertices.get(81).trunkHash());
            Assert.assertEquals(firstEdge2, vertices.get(81).branchHash());
            Assert.assertEquals(lastEdge2, vertices.get(161).branchHash());
            Assert.assertEquals(true, graphModule1.getGraph().isReferencing(vertices.get(161).hash, dataHash2));

            Assert.assertEquals(false, graphModule1.getGraph().isReferencing(vertices.get(161).hash, dataHash1));

        } else {

            Assert.assertEquals(dataHash2, vertices.get(0).trunkHash());
            Assert.assertEquals(firstEdge2, vertices.get(0).branchHash());
            Assert.assertEquals(lastEdge2, vertices.get(80).branchHash());
            Assert.assertEquals(true, graphModule1.getGraph().isReferencing(vertices.get(80).hash, dataHash2));

            Assert.assertEquals(dataHash1, vertices.get(81).trunkHash());
            Assert.assertEquals(firstEdge1, vertices.get(81).branchHash());
            Assert.assertEquals(lastEdge1, vertices.get(161).branchHash());
            Assert.assertEquals(true, graphModule1.getGraph().isReferencing(vertices.get(161).hash, dataHash1));

            Assert.assertEquals(false, graphModule1.getGraph().isReferencing(vertices.get(161).hash, dataHash2));

        }

    }

}
