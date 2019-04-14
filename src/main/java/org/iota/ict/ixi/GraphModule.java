package org.iota.ict.ixi;

import org.iota.ict.eee.call.EEEFunction;
import org.iota.ict.eee.call.FunctionEnvironment;
import org.iota.ict.ixi.model.Graph;
import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.network.gossip.GossipEvent;
import org.iota.ict.network.gossip.GossipListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphModule extends IxiModule {

    private Graph graph = new Graph();
    private Map<String, Transaction> receivedTransactionsByHash = new HashMap<>();

    private final EEEFunction startVertex = new EEEFunction(new FunctionEnvironment("Graph.ixi", "startVertex"));
    private final EEEFunction addEdge = new EEEFunction(new FunctionEnvironment("Graph.ixi", "addEdge"));

    public GraphModule(Ixi ixi) {

        super(ixi);

        ixi.addListener(new GossipListener.Implementation() {
            @Override
            public void onReceive(GossipEvent effect) {
                receivedTransactionsByHash.put(effect.getTransaction().hash, effect.getTransaction());
                List<Transaction> vertex = completeVertex();
                graph.deserializeAndStore(vertex);
            }
        });

    }

    /**
     * This method will be executed once graph.ixi got injected into Ict.
     */
    @Override
    public void run() {

        System.out.println("Graph.ixi loaded!");

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processStartVertexRequest(startVertex.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processAddEdgeRequest(addEdge.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

    }

    public void processStartVertexRequest(EEEFunction.Request request) {
        String data = request.argument.split(";")[0];
        String edge = request.argument.split(";")[1];
        String ret = graph.startVertex(data, edge);
        request.submitReturn(ixi, ret);
    }

    public void processAddEdgeRequest(EEEFunction.Request request) {
        String midVertexHash = request.argument.split(";")[0];
        String edge = request.argument.split(";")[1];
        String ret = graph.addEdge(midVertexHash, edge);
        request.submitReturn(ixi, ret);
    }

    /**
     * Returns the module graph.
     * @return the graph connected to this module
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Sends a bundle to the neighbors.
     * @param bundle the bundle that is to be sent
     */
    public void submit(Bundle bundle) {
        for(Transaction transaction: bundle.getTransactions())
            ixi.submit(transaction);
    }

    /**
     * Tries to complete received vertex by its tail transaction.
     * @return the completed vertex bundle fragment.
     */
    private List<Transaction> completeVertex() {

        for(Transaction tail: new ArrayList<>(receivedTransactionsByHash.values())) {

            if(InputValidator.hasVertexStartFlagSet(tail) || InputValidator.hasVertexStartAndEndFlagSet(tail)) {

                List<Transaction> ret = new ArrayList<>();
                ret.add(tail);
                String currentTail = tail.trunkHash();

                while(true) {

                    Transaction next = receivedTransactionsByHash.get(currentTail);

                    if(next == null)
                        return new ArrayList<>();

                    ret.add(next);

                    if(InputValidator.hasVertexEndFlagSet(next)) {
                        for(Transaction t: ret)
                            receivedTransactionsByHash.remove(t.hash);
                        return ret;
                    }

                    currentTail = next.trunkHash();

                }
            }
        }

        return new ArrayList<>();

    }

}