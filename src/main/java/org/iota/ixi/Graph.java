package org.iota.ixi;

import org.iota.ict.ixi.IctProxy;
import org.iota.ict.ixi.IxiModule;
import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    // returns the hash of the head created, trunk pointing to data, branch pointing to first edge
    public Transaction startVertex(String data, String edge) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = data;
        transactionBuilder.branchHash = edge;
        return transactionBuilder.build();
    }

    // adds a transaction to the bundle started in startVertex, branch pointing to the edge, returns the new transaction hash
    public Transaction addEdge(String midVertexHash, String edge, boolean last) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = midVertexHash;
        transactionBuilder.branchHash = edge;
        if(last)
            transactionBuilder.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 1, 0, 0 }), Transaction.Field.TAG.tryteLength);
        return transactionBuilder.build();
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
