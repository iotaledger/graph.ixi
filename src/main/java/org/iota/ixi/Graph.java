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
    }

    @Override
    public void run() { ; }

    /**
     * This method starts a reflected vertex with trunk pointing to the data and branch pointing to the first reflected vertex tail that is to be referenced.
     * @param data the hash of the data bundle fragment tail
     * @param edge the hash of the outgoing reflected vertex tail
     * @return the hash of the reflected vertex head
     */
    public String startVertex(String data, String edge) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = data;
        transactionBuilder.branchHash = edge;
        transactionBuilder.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 1, 0, 0 }), Transaction.Field.TAG.tryteLength);
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        return transaction.hash;
    }

    /**
     * This method continues a reflected vertex with branch pointing to the next reflected vertex tail that is to be referenced.
     * @param midVertexHash the hash of the reflected vertex tail to be continued
     * @param edge the hash of the outgoing reflected vertex tail
     * @return the hash of the new reflected vertex tail
     */
    public String addEdge(String midVertexHash, String edge) {
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = midVertexHash;
        transactionBuilder.branchHash = edge;
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        return transaction.hash;
    }

    /**
     * This method serializes a reflected vertex into a bundle fragment ready to put into a bundle.
     * @param reflectedTail the hash of the reflected vertex tail to be finalized
     * @return the bundle fragment ready to put into a bundle
     */
    public List<TransactionBuilder> finalizeVertex(String reflectedTail) {

        List<String> edges = new LinkedList<>();

        while(true) {

            Transaction t = transactionsByHash.get(reflectedTail);
            String edge = t.branchHash;
            edges.add(edge);

            if(Trytes.toTrits(t.tag)[0] == 1 || t.trunkHash.equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                break;

            reflectedTail = t.trunkHash;

        }

        Transaction head = transactionsByHash.get(reflectedTail);
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

        // fill last signature fragment
        t.signatureFragments = Trytes.padRight(t.signatureFragments, Transaction.Field.SIGNATURE_FRAGMENTS.tryteLength);
        t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 1, 0, 0 }), Transaction.Field.TAG.tryteLength);
        transactions.add(t);

        return transactions;
    }

    /**
     * This method returns the hash of the data bundle fragment tail.
     * @param vertex the hash of the reflected vertex tail
     * @return the hash of the data bundle fragment tail
     */
    public String getData(String vertex) {
        Transaction transaction = null;
        while(true) {
            transaction = transactionsByHash.get(vertex);
            if(Trytes.toTrits(transaction.tag)[0] == 1)
                break;
            if(transaction.trunkHash.equals(Trytes.NULL_HASH))
                return null;
            vertex = transaction.trunkHash;
        }
        return transaction.trunkHash;
    }

    /**
     * This method returns all outgoing edges for the reflected vertex tail
     * @param vertex the hash of the current reflected vertex tail
     * @return all outgoing edges for the current reflected vertex tail
     */
    public List<String> getEdges(String vertex) {
        List<String> ret = new ArrayList<>();
        while(true) {
            Transaction t = transactionsByHash.get(vertex);
            if(t == null)
                return ret;
            ret.add(t.branchHash);
            if(Trytes.toTrits(t.tag)[0] == 1 || t.trunkHash.equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                return ret;
            vertex = t.trunkHash;
        }
    }

    /**
     * This method returns the next outgoing edge for the current reflected vertex tail
     * @param vertex the hash of the current reflected vertex tail
     * @param previousEdge the previous edge
     * @return the next outgoing edge for the current reflected vertex tail
     */
    public String getNextEdge(String vertex, String previousEdge) {
        List<String> edges = getEdges(vertex);
        for(int i = 0; i < edges.size(); i++)
            if(previousEdge.equals(edges.get(i)))
                if(i + 1 < edges.size())
                    return edges.get(i + 1);
        return null;
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

    // returns all vertices for data hash
    public List<String> getCompoundVertex(String dataHash) {
        return verticesByDataHash.get(dataHash);
    }

    // returns all vertices with edges incoming to given vertex.
    public List<String> getReferencingVertices(String vertex) {
        return referencingVertices.get(vertex);
    }

}
