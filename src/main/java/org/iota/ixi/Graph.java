package org.iota.ixi;

import org.iota.ict.ixi.IctProxy;
import org.iota.ict.ixi.IxiModule;
import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.*;

public class Graph extends IxiModule {

    private Map<String, Transaction> transactionsByHash = new HashMap<>();
    private Map<String, List<String>> verticesByDataHash = new HashMap<>();
    private Map<String, List<String>> referencingVertices = new HashMap<>();

    public Graph(IctProxy ict) {
        super(ict);
        new Thread(this).start();
    }

    @Override
    public void run() {

        // List<TransactionBuilder> transactions = createVertex("DATAHASH9999999999999999999999999999999999999999999999999999999999999999999999999", new String[] { "FIRST9999999999999999999999999999999999999999999999999999999999999999999999999999", "SECOND999999999999999999999999999999999999999999999999999999999999999999999999999" } );

    }


    // returns the hash of the created head, trunk pointing to data, branch pointing to first edge
    public String startVertex(String data, String edge) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = data;
        transactionBuilder.branchHash = edge;
        transactionBuilder.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 1, 0, 0 }), Transaction.Field.TAG.tryteLength);
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        return transaction.hash;
    }

    // adds a transaction to the bundle started in startVertex, branch pointing to the edge, returns transaction hash
    public String addEdge(String midVertexHash, String edge) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = midVertexHash;
        transactionBuilder.branchHash = edge;
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        return transaction.hash;
    }

    // create the bundle fragment ready to attach to the tangle, or to put into a bundle, return the fragment tail
    public List<TransactionBuilder> finalizeVertex(String reflectedTail) {

        List<String> edges = new LinkedList<>();

        String current = reflectedTail;
        while(true) {

            Transaction t = transactionsByHash.get(current);
            String edge = t.branchHash;
            edges.add(edge);

            if(Trytes.toTrits(t.tag)[0] == 1 || t.trunkHash.equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                break;

            current = t.trunkHash;

        }

        Transaction head = transactionsByHash.get(current);
        String data = head.trunkHash;
        String firstEdge = head.branchHash;
        edges.add(firstEdge);

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

        return transactions;
    }

    // returns all vertices for data hash
    public List<String> getCompoundVertex(String dataHash) {
        return verticesByDataHash.get(dataHash);
    }

    // returns all vertices with edges incoming to given vertex.
    public List<String> getReferencingVertices(String vertex) {
        return referencingVertices.get(vertex);
    }

    // returns all outgoing edges for the current vertex hash
    public List<String> getEdges(String vertex) {

        List<String> ret = new ArrayList<>();

        while(true) {

            Transaction t = transactionsByHash.get(vertex);

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
        Transaction t = transactionsByHash.get(vertex);
        return t.extraDataDigest;
    }

    // creates a vertex bundle fragment, returns the tail of it
    public List<TransactionBuilder> createVertex(String data, String[] edges) {

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

        return transactions;

    }

    public String getNextCompoundVertex(String data, String previousVertex) {
        List<String> vertices = getCompoundVertex(data);
        for(int i = 0; i < vertices.size(); i++)
            if(previousVertex.equals(vertices.get(i)))
                if(i + 1 < vertices.size())
                    return vertices.get(i + 1);
        return null;
    }

    public String getNextReferencingVertex(String vertex, String previousVertex) {
        List<String> vertices = getReferencingVertices(vertex);
        for(int i = 0; i < vertices.size(); i++)
            if(previousVertex.equals(vertices.get(i)))
                if(i + 1 < vertices.size())
                    return vertices.get(i + 1);
         return null;
    }

    public String getNextEdge(String vertex, String previousEdge) {
        List<String> edges = getEdges(vertex);
        for(int i = 0; i < edges.size(); i++)
            if(previousEdge.equals(edges.get(i)))
                if(i + 1 < edges.size())
                    return edges.get(i + 1);
        return null;
    }

}
