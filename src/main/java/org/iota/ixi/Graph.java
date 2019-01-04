package org.iota.ixi;

import org.iota.ict.ixi.DefaultIxiModule;
import org.iota.ict.ixi.IctProxy;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.BundleBuilder;
import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;
import org.iota.ict.utils.Trytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph extends DefaultIxiModule {

    public Graph(IctProxy ict) {
        super(ict);
    }

    @Override
    public void run() {

        String hash = createVertex("DATAHASH9999999999999999999999999999999999999999999999999999999999999999999999999", new String[] { "FIRST9999999999999999999999999999999999999999999999999999999999999999999999999999", "SECOND999999999999999999999999999999999999999999999999999999999999999999999999999" } );

        List<String> e = getEdges(hash);

        System.out.println("VERTEX HASH: " + hash);
        System.out.println("DATA: "+ getData(hash));
        System.out.println("EDGE1: " + e.get(0));
        System.out.println("EDGE2: " + e.get(1));

    }

    // returns all outgoing edges for the current vertex hash
    public List<String> getEdges(String vertex) {

        List<String> ret = new ArrayList<>();

        while(true) {

            Transaction t = findTransactionByHash(vertex);

            for(String edge: t.signatureFragments.split("(?<=\\G.{81})"))
                if(!edge.equals(Trytes.NULL_HASH))
                    ret.add(edge);

            if(Trytes.toTrits(t.tag)[0] == 1 || t.trunkHash.equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                break;

            vertex = t.trunkHash;

        }

        return ret;

    }

    // returns the hash of the data bundle fragment tail
    public String getData(String vertex) {
        Transaction t = findTransactionByHash(vertex);
        return t.extraDataDigest;
    }

    // creates a vertex bundle fragment, returns the tail of it
    public String createVertex(String data, String[] edges) {

        BundleBuilder bundleBuilder = new BundleBuilder();
        List<TransactionBuilder> transactions = new ArrayList<>();

        TransactionBuilder t = new TransactionBuilder();
        t.extraDataDigest = data;
        t.signatureFragments = "";

        for(String edge: edges) {

            if(t.signatureFragments.length() < 27 * 81)
                t.signatureFragments += edge;
            else {
                transactions.add(t);
                t = new TransactionBuilder();
            }

        }

        // fill signature fragment
        t.signatureFragments = Trytes.padRight(t.signatureFragments, Transaction.Field.SIGNATURE_FRAGMENTS.tryteLength);
        transactions.add(t);

        Collections.reverse(transactions);
        bundleBuilder.append(transactions);
        Bundle vertexBundle = bundleBuilder.build();

        for(Transaction tx: vertexBundle.getTransactions())
            submit(tx);

        return vertexBundle.getTransactions().get(0).hash;

    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        System.out.println("vertex received");
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        System.out.println("vertex submitted");
    }

    @Override
    public void onIctShutdown() {
        super.onIctShutdown();
    }

}
