package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Graph;
import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.Transaction;

import java.util.*;

public class DefaultGraphModule extends IxiModule {

    private Graph graph = new Graph();
    private Map<String, Transaction> receivedTransactionsByHash = new HashMap<>();

    public DefaultGraphModule(Ixi ixi) {

        super(ixi);

        ixi.addGossipListener(event -> {

            receivedTransactionsByHash.put(event.getTransaction().hash, event.getTransaction());

            for(Transaction transaction: new ArrayList<>(receivedTransactionsByHash.values())) {

                if(InputValidator.hasVertexStartFlagSet(transaction) || InputValidator.hasVertexStartAndEndFlagSet(transaction)) {
                    List<Transaction> vertex = completeVertex(transaction.hash);
                    graph.deserializeAndStore(vertex);
                    for(Transaction t: vertex)
                        receivedTransactionsByHash.remove(t);
                }

            }

        });

    }

    @Override
    public void run() { ; }

    public Graph getGraph() {
        return graph;
    }

    public void submit(Bundle bundle) {
        for(Transaction transaction: bundle.getTransactions())
            ixi.submit(transaction);
    }

    private List<Transaction> completeVertex(String tail) {

        List<Transaction> ret = new ArrayList<>();

        while(true) {

            Transaction next = receivedTransactionsByHash.get(tail);

            if(next == null)
                return new ArrayList<>();

            ret.add(next);

            if(InputValidator.hasVertexEndFlagSet(next))
                return ret;

            tail = next.trunkHash();

        }

    }

}