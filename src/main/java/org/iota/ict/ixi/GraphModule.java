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

    private final EEEFunction createVertex = new EEEFunction(new FunctionEnvironment("Graph.ixi", "createVertex"));
    private final EEEFunction startVertex = new EEEFunction(new FunctionEnvironment("Graph.ixi", "startVertex"));
    private final EEEFunction addEdge = new EEEFunction(new FunctionEnvironment("Graph.ixi", "addEdge"));
    private final EEEFunction addEdges = new EEEFunction(new FunctionEnvironment("Graph.ixi", "addEdges"));
    private final EEEFunction getData = new EEEFunction(new FunctionEnvironment("Graph.ixi", "getData"));
    private final EEEFunction getEdges = new EEEFunction(new FunctionEnvironment("Graph.ixi", "getEdges"));
    private final EEEFunction getNextEdge = new EEEFunction(new FunctionEnvironment("Graph.ixi", "getNextEdge"));
    private final EEEFunction getReferencingVertices = new EEEFunction(new FunctionEnvironment("Graph.ixi", "getReferencingVertices"));
    private final EEEFunction isReferencing = new EEEFunction(new FunctionEnvironment("Graph.ixi", "isReferencing"));

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

        ixi.addListener(createVertex);
        ixi.addListener(startVertex);
        ixi.addListener(addEdge);
        ixi.addListener(addEdges);
        ixi.addListener(getData);
        ixi.addListener(getEdges);
        ixi.addListener(getNextEdge);
        ixi.addListener(getReferencingVertices);
        ixi.addListener(isReferencing);

    }

    /**
     * This method will be executed once graph.ixi got injected into Ict.
     */
    @Override
    public void run() {

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processCreateVertexRequest(createVertex.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

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

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processAddEdgesRequest(addEdges.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processGetData(getData.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processGetEdges(getEdges.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processGetNextEdge(getNextEdge.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processGetReferencingVertices(getReferencingVertices.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processIsReferencing(isReferencing.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        System.out.println("Graph.ixi loaded!");

    }

    public void processCreateVertexRequest(EEEFunction.Request request) {
        String argument = request.argument;
        String data = argument.split(";")[0];
        String[] edges = argument.substring(argument.indexOf(";")+1).split(";");
        String ret = graph.createVertex(data, edges);
        request.submitReturn(ixi, ret);
    }

    public void processStartVertexRequest(EEEFunction.Request request) {
        String argument = request.argument;
        String data = argument.split(";")[0];
        String edge = argument.split(";")[1];
        String ret = graph.startVertex(data, edge);
        request.submitReturn(ixi, ret);
    }

    public void processAddEdgeRequest(EEEFunction.Request request) {
        String argument = request.argument;
        String midVertexHash = argument.split(";")[0];
        String edge = argument.split(";")[1];
        String ret = graph.addEdge(midVertexHash, edge);
        request.submitReturn(ixi, ret);
    }

    public void processAddEdgesRequest(EEEFunction.Request request) {
        String argument = request.argument;
        String midVertexHash = argument.split(";")[0];
        String[] edges = argument.substring(argument.indexOf(";")+1).split(";");
        String ret = graph.addEdges(midVertexHash, edges);
        request.submitReturn(ixi, ret);
    }

    public void processGetData(EEEFunction.Request request) {
        String vertex = request.argument;
        String ret = graph.getData(vertex);
        request.submitReturn(ixi, ret);
    }

    public void processGetEdges(EEEFunction.Request request) {
        String vertex = request.argument;
        List<String> edges = graph.getEdges(vertex);
        String ret = "";
        for(int i = 0; i < edges.size(); i++) {
            ret += edges.get(i);
            if(i < edges.size() - 1)
                ret += ";";
        }
        request.submitReturn(ixi, ret);
    }

    public void processGetNextEdge(EEEFunction.Request request) {
        String argument = request.argument;
        String vertex = argument.split(";")[0];
        String previousEdge = argument.split(";")[1];
        String ret = graph.getNextEdge(vertex, previousEdge);
        request.submitReturn(ixi, ret);
    }

    public void processGetReferencingVertices(EEEFunction.Request request) {
        String vertex = request.argument;
        List<String> vertices = graph.getReferencingVertices(vertex);
        String ret = "";
        for(int i = 0; i < vertices.size(); i++) {
            ret += vertices.get(i);
            if(i < vertices.size() - 1)
                ret += ";";
        }
        request.submitReturn(ixi, ret);
    }

    public void processIsReferencing(EEEFunction.Request request) {
        String argument = request.argument;
        String vertex = argument.split(";")[0];
        String neighbor = argument.split(";")[1];
        boolean isReferencing = graph.isReferencing(vertex, neighbor);
        String ret = isReferencing + "";
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