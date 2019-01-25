package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Graph;
import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DefaultGraphModule extends IxiModule {

    private Graph graph = new Graph();

    public DefaultGraphModule(Ixi ixi) {

        super(ixi);

        ixi.addGossipListener(event -> {

            Transaction transaction = event.getTransaction();

            if(InputValidator.hasVertexStartFlagSet(transaction)) {

                // Check if graph contains already serialized vertex, if not add vertex to the graph
                if(graph.getReferencingVertices(transaction.hash).size() == 0) {

                    List<Transaction> vertex = completeVertex(transaction);
                    graph.deserializeAndStore(vertex);

                }

            }
        });

    }

    @Override
    public void run() { ; }

    public void submit(Bundle bundle) {
        for(Transaction transaction: bundle.getTransactions())
            ixi.submit(transaction);
    }

    private List<Transaction> completeVertex(Transaction tail) {

        List<Transaction> ret = new ArrayList<>();
        ret.add(tail);

        while(true) {

            Transaction next = ixi.findTransactionByHash(tail.hash);

            if(next == null)
                return null;

            ret.add(next);

            if(InputValidator.hasVertexEndFlagSet(next))
                return ret;

            tail = next;

        }

    }

}