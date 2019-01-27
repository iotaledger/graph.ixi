package org.iota.ict.ixi;

import org.iota.ict.Ict;
import org.iota.ict.ixi.utils.VertexGenerator;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.utils.properties.EditableProperties;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class VertexEventTest extends GraphTestTemplate {

    @Test
    public void receiveSingleVertexTest() {

        EditableProperties properties1 = new EditableProperties().host("localhost").port(1337).minForwardDelay(0).maxForwardDelay(10).guiEnabled(false);
        Ict ict1 = new Ict(properties1.toFinal());

        EditableProperties properties2 = new EditableProperties().host("localhost").port(1338).minForwardDelay(0).maxForwardDelay(10).guiEnabled(false);
        Ict ict2 = new Ict(properties2.toFinal());

        addNeighborToIct(ict1,ict2);
        addNeighborToIct(ict2,ict1);

        // register graph module to Ict1
        DefaultGraphModule graphModule = new DefaultGraphModule(ict1);

        // create vertex
        String dataHash = "DATA9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String firstEdge = "FIRST9EDGE99999999999999999999999999999999999999999999999999999999999999999999999";

        String firstTranscationHash = graph.startVertex(dataHash, firstEdge);
        String[] edges = VertexGenerator.generateRandomEdges(135);

        String currentTail = graph.addEdges(firstTranscationHash, edges);

        String lastEdge = "LAST9HASH999999999999999999999999999999999999999999999999999999999999999999999999";
        String tail = graph.addEdge(currentTail, lastEdge);

        List<TransactionBuilder> transactionBuilderList = graph.finalizeVertex(tail);
        Bundle bundle = graph.serialize(transactionBuilderList);

        // send vertex from Ict2 to Ict1
        for(Transaction transaction: bundle.getTransactions())
            ict2.submit(transaction);

        // wait few seconds to avoid premature termination of this test
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Transaction> vertices = new ArrayList<>(graphModule.getGraph().getTransactionsByHash().values());

        Assert.assertEquals(137, vertices.size());
        Assert.assertEquals(dataHash, vertices.get(0).trunkHash());
        Assert.assertEquals(firstEdge, vertices.get(0).branchHash());
        Assert.assertEquals(lastEdge, vertices.get(136).branchHash());

        ict2.terminate();
        ict1.terminate();

    }

    private static void addNeighborToIct(Ict ict, Ict neighbor) {
        EditableProperties properties = ict.getProperties().toEditable();
        List<InetSocketAddress> neighbors = properties.neighbors();
        neighbors.add(neighbor.getAddress());
        properties.neighbors(neighbors);
        ict.updateProperties(properties.toFinal());
    }

}
