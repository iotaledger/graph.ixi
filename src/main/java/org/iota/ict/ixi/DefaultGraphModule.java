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

            System.out.println("OTHERS");

            {
                Transaction t = transaction;

                System.out.println("HASH: " + t.hash);
                System.out.println("ExtraData: " + t.extraDataDigest());
                System.out.println("TRUNK: " + t.trunkHash());
                System.out.println("BRANCH: " + t.branchHash());
                System.out.println("SIGNATURE: " + t.signatureFragments());
                System.out.println();
            }

            if(InputValidator.hasVertexStartFlagSet(transaction) || InputValidator.hasVertexStartAndEndFlagSet(transaction)) {

                List<Transaction> vertex = completeVertex(transaction.hash);

                System.out.println("FOUND");
                {
                    Transaction t = transaction;

                    System.out.println("HASH: " + t.hash);
                    System.out.println("ExtraData: " + t.extraDataDigest());
                    System.out.println("TRUNK: " + t.trunkHash());
                    System.out.println("BRANCH: " + t.branchHash());
                    System.out.println("SIGNATURE: " + t.signatureFragments());
                    System.out.println();
                }

                System.out.println("SIZE: "+ vertex.size());

                graph.deserializeAndStore(vertex);

            }
        });

    }

    @Override
    public void run() { ; }

    public void submit(Bundle bundle) {
        for(Transaction transaction: bundle.getTransactions())
            ixi.submit(transaction);
    }

    private List<Transaction> completeVertex(String tail) {

        List<Transaction> ret = new ArrayList<>();

        while(true) {

            Transaction next = ixi.findTransactionByHash(tail);

            if(next == null)
                return ret;

            ret.add(next);

            if(InputValidator.hasVertexEndFlagSet(next))
                return ret;

            tail = next.trunkHash();

        }

    }

    public Graph getGraph() {
        return graph;
    }

}