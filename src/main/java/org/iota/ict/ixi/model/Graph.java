package org.iota.ict.ixi.model;

import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.*;

public class Graph {

    private Map<String, Transaction> transactionsByHash = new LinkedHashMap();
    private Set<String> virtualTails = new LinkedHashSet<>();
    private Map<String, String> serializedTailsByVirtualTails = new LinkedHashMap<>();

    /**
     * Creates a vertex with trunk pointing to the data and branch pointing to the outgoing vertex tails that are to be referenced.
     * @param data the hash of the data bundle fragment tail that is to be referenced
     * @param edges the hashes of all outgoing vertex tails that are to be referenced
     * @return the hash of the created vertex tail
     */
    public String createVertex(String data, String[] edges) {
        if(!InputValidator.isValidHash(data) || !InputValidator.areValidHashes(edges))
            return null;
        String vertex = startVertex(data, edges[0]);
        for(int i = 1; i < edges.length; i++)
            vertex = addEdge(vertex, edges[i]);
        return vertex;
    }

    /**
     * Starts a new vertex with trunk pointing to the data and branch pointing to the first vertex that is to be referenced.
     * @param data the data bundle fragment tail that is to be referenced
     * @param edge the first vertex tail that is to be referenced
     * @return the created vertex head
     */
    public String startVertex(String data, String edge) {
        if(!InputValidator.isValidHash(data) || !InputValidator.isValidHash(edge))
            return null;
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = data;
        transactionBuilder.branchHash = edge;
        transactionBuilder.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 1, 0 }), Transaction.Field.TAG.tryteLength);
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        virtualTails.add(transaction.hash);
        return transaction.hash;
    }

    /**
     * Continues a vertex with branch pointing to the next vertex that is to be referenced.
     * @param currentVertexTail the vertex tail to be continued
     * @param edge the vertex tail that is to be referenced
     * @return the new vertex tail
     */
    public String addEdge(String currentVertexTail, String edge) {
        if(!InputValidator.isValidHash(currentVertexTail) || !InputValidator.isValidHash(edge))
            return null;
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = currentVertexTail;
        transactionBuilder.branchHash = edge;
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        virtualTails.remove(currentVertexTail);
        virtualTails.add(transaction.hash);
        return transaction.hash;
    }

    /**
     * Continues a vertex with the branches pointing to the vertices that are to be referenced.
     * @param currentVertexTail the vertex tail to be continued
     * @param edges the vertex tails to be referenced
     * @return the new vertex tail
     */
    public String addEdges(String currentVertexTail, String[] edges) {
        if(!InputValidator.isValidHash(currentVertexTail) || !InputValidator.areValidHashes(edges))
            return null;
        virtualTails.remove(currentVertexTail);
        for(String edge: edges) {
            TransactionBuilder transactionBuilder = new TransactionBuilder();
            transactionBuilder.trunkHash = currentVertexTail;
            transactionBuilder.branchHash = edge;
            Transaction transaction = transactionBuilder.build();
            transactionsByHash.put(transaction.hash, transaction);
            currentVertexTail = transaction.hash;
        }
        virtualTails.add(currentVertexTail);
        return currentVertexTail;
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

            Transaction t = transactionsByHash.get(currentVertexTail);

            if(t == null)
                break;

            if(Trytes.toTrits(t.tag())[1] == 1)
                break;

            String edge = t.branchHash();
            edges.add(edge);

            currentVertexTail = t.trunkHash();

        }

        Transaction head = transactionsByHash.get(currentVertexTail);
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

        return transactions;
    }

    /**
     * Returns the data bundle fragment tail from the vertex.
     * @param vertex the vertex tail
     * @return the data bundle fragment tail
     */
    public String getData(String vertex) {

        if(!InputValidator.isValidHash(vertex))
            return null;

        Transaction transaction = null;
        while(true) {
            transaction = transactionsByHash.get(vertex);

            if(transaction == null)
                return null;

            if(Trytes.toTrits(transaction.tag())[1] == 1)
                break;

            vertex = transaction.trunkHash();
        }
        return transaction.trunkHash();
    }

    /**
     * Returns all outgoing edges of a vertex
     * @param vertex the vertex tail
     * @return all outgoing edges of the vertex
     */
    public List<String> getEdges(String vertex) {
        List<String> ret = new ArrayList<>();
        while(true) {
            Transaction t = transactionsByHash.get(vertex);
            if(t == null)
                return ret;
            ret.add(t.branchHash());
            if(Trytes.toTrits(t.tag())[1] == 1 || t.trunkHash().equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                return ret;
            vertex = t.trunkHash();
        }
    }

    /**
     * Returns the next outgoing edge of a vertex
     * @param vertex the vertex tail
     * @param previousEdge the previous edge
     * @return the next outgoing edge of a vertex
     */
    public String getNextEdge(String vertex, String previousEdge) {

        while(true) {

            Transaction t = transactionsByHash.get(vertex);
            if(t == null)
                return null;

            if(t.branchHash().equals(previousEdge)) {

                Transaction next = transactionsByHash.get(vertex);
                if(t == null)
                    return null;

                return next.branchHash();

            }

            if(Trytes.toTrits(t.tag())[1] == 1) // check if last transaction of vertex
                return null;

            vertex = t.trunkHash();

        }

    }

    /**
     * Returns all vertices which point to given vertex
     * @param vertex the vertex tail to be checked
     * @return all vertices with edges incoming to given vertex
     */
    public List<String> getReferencingVertices(String vertex) {
        List<String> ret = new ArrayList<>();
        for(String tail: virtualTails)
            if(isReferencing(tail, vertex))
                ret.add(tail);
        return ret;
    }

    /**
     * Checks if a vertex fragment contains a specific trunk or branch
     * @param vertex the hash of the vertex tail
     * @param hash the branch or trunk that is to be checked
     * @return true if vertex contains hash
     * @return false if vertex does not contain hash
     */
    public boolean isReferencing(String vertex, String hash) {
        if(vertex.equals(hash))
            return false;
        while(true) {
            Transaction transaction = transactionsByHash.get(vertex);
            if(transaction == null)
                return false;
            if(transaction.trunkHash().equals(hash) || transaction.branchHash().equals(hash))
                return true;
            vertex = transaction.trunkHash();
        }
    }

    /**
     * Returns all transactions of the graph
     * @return all transactions of the graph
     */
    public Map<String,Transaction> getTransactionsByHash() {
        return transactionsByHash;
    }

    /**
     * Maps the virtual tail with the serialized tail
     */
    public void addSerializedTail(String virtualTail, String serializedTail) {
        serializedTailsByVirtualTails.put(virtualTail, serializedTail);
    }

    /**
     * Returns the serialized tail for a given virtual tail
     */
    public String getSerializedTail(String virtualTail, String serializedTail) {
        return serializedTailsByVirtualTails.get(virtualTail);
    }

}