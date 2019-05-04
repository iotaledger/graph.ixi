package org.iota.ict.ixi;

import org.iota.ict.eee.call.EEEFunction;
import org.iota.ict.eee.call.FunctionEnvironment;
import org.iota.ict.ixi.model.Graph;
import org.iota.ict.ixi.model.Pair;
import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.bundle.BundleBuilder;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.network.gossip.GossipEvent;
import org.iota.ict.network.gossip.GossipListener;
import org.iota.ict.utils.Trytes;

import java.util.*;

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
    private final EEEFunction serializeAndSubmit = new EEEFunction(new FunctionEnvironment("Graph.ixi", "serializeAndSubmit"));
    private final EEEFunction serializeAndSubmitToCustomTips = new EEEFunction(new FunctionEnvironment("Graph.ixi", "serializeAndSubmitToCustomTips"));
    private final EEEFunction getSerializedTail = new EEEFunction(new FunctionEnvironment("Graph.ixi", "getSerializedTail"));

    public GraphModule(Ixi ixi) {

        super(ixi);

        ixi.addListener(new GossipListener.Implementation() {
            @Override
            public void onReceive(GossipEvent effect) {
                receivedTransactionsByHash.put(effect.getTransaction().hash, effect.getTransaction());
                List<Transaction> vertex = completeVertex();
                deserializeAndStore(vertex);
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
        ixi.addListener(serializeAndSubmit);
        ixi.addListener(getSerializedTail);
        ixi.addListener(serializeAndSubmitToCustomTips);

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

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processSerializeAndSubmit(serializeAndSubmit.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processSerializeAndSubmitToCustomTips(serializeAndSubmitToCustomTips.requestQueue.take());
                } catch (InterruptedException e) {
                    if(isRunning()) throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (isRunning()) {
                try {
                    processGetSerializedTail(getSerializedTail.requestQueue.take());
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

    public void processSerializeAndSubmit(EEEFunction.Request request) {
        String virtualTail = request.argument;
        List<TransactionBuilder> transactionBuilders = finalizeVertex(virtualTail);
        Bundle bundle = serialize(new Pair(virtualTail, transactionBuilders));
        submit(bundle);
        request.submitReturn(ixi, bundle.getHead().hash);
    }

    public void processSerializeAndSubmitToCustomTips(EEEFunction.Request request) {

        String[] arguments = request.argument.split(";");
        String virtualTail = arguments[0];
        String trunkTip = arguments[1];
        String branchTip = arguments[2];

        List<TransactionBuilder> transactionBuilders = finalizeVertex(virtualTail);
        Collections.reverse(transactionBuilders);

        for(int i = 0; i < transactionBuilders.size(); i++) {

            TransactionBuilder builder = transactionBuilders.get(i);

            if(i != transactionBuilders.size() - 1)
                builder.branchHash = trunkTip;
            else {
                builder.branchHash = branchTip;
                builder.trunkHash = trunkTip;
            }

        }

        Collections.reverse(transactionBuilders);

        Bundle bundle = serialize(new Pair(virtualTail, transactionBuilders));
        submit(bundle);
        request.submitReturn(ixi, bundle.getHead().hash);
    }


    public void processGetSerializedTail(EEEFunction.Request request) {
        String virtualTail = request.argument;
        String ret = graph.getSerializedTail(virtualTail);
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
     * Finalizes a vertex ready to put into a bundle.
     * @param currentVertexTail the vertex tail to be finalized
     * @return the bundle fragment ready to put into a bundle
     */
    public List<TransactionBuilder> finalizeVertex(String currentVertexTail) {

        if(!InputValidator.isValidHash(currentVertexTail))
            return null;

        List<String> edges = new LinkedList<>();

        while(true) {

            Transaction t = graph.getTransactionsByHash().get(currentVertexTail);

            if(t == null)
                break;

            if(Trytes.toTrits(t.tag())[1] == 1)
                break;

            String edge = t.branchHash();
            edges.add(edge);

            currentVertexTail = t.trunkHash();

        }

        Transaction head = graph.getTransactionsByHash().get(currentVertexTail);
        String data = head.trunkHash();
        String firstEdge = head.branchHash();
        edges.add(firstEdge);

        List<TransactionBuilder> transactions = new ArrayList<>();

        TransactionBuilder t = new TransactionBuilder();
        t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 0, 1 }), Transaction.Field.TAG.tryteLength);
        t.extraDataDigest = data;
        t.signatureFragments = "";

        for(String edge: edges) {

            if(t.signatureFragments.length() < 27 * 81)
                t.signatureFragments += edge;
            else {
                transactions.add(t);
                t = new TransactionBuilder();
                t.signatureFragments = edge;
            }

        }

        // fill last signature fragment
        t.signatureFragments = Trytes.padRight(t.signatureFragments, Transaction.Field.SIGNATURE_FRAGMENTS.tryteLength);

        // if first transaction == last transaction
        if(Trytes.toTrits(t.tag)[2] == 1)
            t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 1, 1 }), Transaction.Field.TAG.tryteLength);
        else
            t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 1, 0 }), Transaction.Field.TAG.tryteLength);

        transactions.add(t);

        for(TransactionBuilder transactionBuilder: transactions) {
            transactionBuilder.attachmentTimestampLowerBound = System.currentTimeMillis();
            transactionBuilder.attachmentTimestampUpperBound = System.currentTimeMillis();
        }

        return transactions;
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

    /**
     * Serializes bundle fragments ready to attach to the Tangle.
     * @param finalizedVertexPair the bundle fragments mapped with the virtual vertex tail to serialize
     * @return the bundle ready to attach to the Tangle
     */
    public Bundle serialize(Pair<String, List<TransactionBuilder>>... finalizedVertexPair) {

        // put all transactions in a collection
        List<TransactionBuilder> collection = new ArrayList<>();
        for (Pair<String, List<TransactionBuilder>> pair : finalizedVertexPair)
            collection.addAll(pair.value);

        // sort transactions
        Collections.reverse(collection);

        // build bundle
        BundleBuilder bundleBuilder = new BundleBuilder();
        bundleBuilder.append(collection);
        Bundle bundle = bundleBuilder.build();

        // get bundle transactions
        List<Transaction> bundleTransactions = bundle.getTransactions();

        // map serialized vertex tail with virtual vertex tail

        for (Pair<String, List<TransactionBuilder>> pair : finalizedVertexPair) {

            // get virtual vertex tail
            String virtualTail = pair.key;

            // get transaction count
            int count = pair.value.size();

            // get serialized vertex tail
            String serializedTail = bundleTransactions.get(0).hash;

            // map serialized vertex tail with virtual vertex tail
            graph.addSerializedTail(pair.key, serializedTail);

            // skip to next vertex
            bundleTransactions = bundleTransactions.subList(0, count);

        }

        return bundle;

    }


    /**
     * Deserializes and adds all vertices of a bundle to the graph.
     * @param bundle the bundle to deserialize
     * @return to tails of the added vertices
     */
    public String[] deserializeAndStore(Bundle bundle) {
        return deserializeAndStore(bundle.getTransactions());
    }

    /**
     * Deserializes and adds all vertices of a bundle fragment to the graph.
     * @param transactions the bundle fragment to deserialize
     * @return to tails of the added vertices
     */
    public String[] deserializeAndStore(List<Transaction> transactions) {

        // Dissociate vertices from bundle
        List<List<Transaction>> vertices = new ArrayList<>();

        boolean vertexFragmentStart = false;
        List<Transaction> vertexFragment = new ArrayList<>();
        for (Transaction t : transactions) {

            if (InputValidator.hasVertexStartFlagSet(t))
                vertexFragmentStart = true;

            vertexFragment.add(t);

            if (InputValidator.hasVertexEndFlagSet(t))
                if (vertexFragmentStart) {
                    vertices.add(vertexFragment);
                    vertexFragmentStart = false;
                    vertexFragment = new ArrayList<>();
                }

        }

        List<String> tails = new ArrayList<>();

        // Get all edges of found vertices
        for (List<Transaction> vertex : vertices) {

            List<String> edges = new ArrayList<>();
            for (Transaction t : vertex) {
                for (String edge : t.signatureFragments().split("(?<=\\G.{81})"))
                    if (!edge.equals(Trytes.NULL_HASH))
                        edges.add(edge);
            }

            String data = vertex.get(0).extraDataDigest();
            Collections.reverse(edges);

            String tail = graph.createVertex(data, edges.toArray(new String[edges.size()]));
            tails.add(tail);
            graph.addSerializedTail(tail, vertex.get(0).hash);

        }

        return tails.toArray(new String[tails.size()]);

    }

}