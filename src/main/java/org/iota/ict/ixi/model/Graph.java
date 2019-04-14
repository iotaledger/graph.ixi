package org.iota.ict.ixi.model;

import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.bundle.Bundle;
import org.iota.ict.model.bundle.BundleBuilder;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.*;

public class Graph {

    private Map<String, Transaction> transactionsByHash = new LinkedHashMap();
    private Set<String> vertices = new LinkedHashSet<>();

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
        vertices.add(transaction.hash);
        return transaction.hash;
    }

    /**
     * Continues a vertex with branch pointing to the next vertex that is to be referenced.
     * @param midVertexHash the vertex tail to be continued
     * @param edge the vertex tail that is to be referenced
     * @return the new vertex tail
     */
    public String addEdge(String midVertexHash, String edge) {
        if(!InputValidator.isValidHash(midVertexHash) || !InputValidator.isValidHash(edge))
            return null;
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.trunkHash = midVertexHash;
        transactionBuilder.branchHash = edge;
        Transaction transaction = transactionBuilder.build();
        transactionsByHash.put(transaction.hash, transaction);
        vertices.remove(midVertexHash);
        vertices.add(transaction.hash);
        return transaction.hash;
    }

    /**
     * Continues a vertex with the branches pointing to the vertices that are to be referenced.
     * @param midVertexHash the vertex tail to be continued
     * @param edges the vertex tails to be referenced
     * @return the new vertex tail
     */
    public String addEdges(String midVertexHash, String[] edges) {
        if(!InputValidator.isValidHash(midVertexHash) || !InputValidator.areValidHashes(edges))
            return null;
        vertices.remove(midVertexHash);
        for(String edge: edges) {
            TransactionBuilder transactionBuilder = new TransactionBuilder();
            transactionBuilder.trunkHash = midVertexHash;
            transactionBuilder.branchHash = edge;
            Transaction transaction = transactionBuilder.build();
            transactionsByHash.put(transaction.hash, transaction);
            midVertexHash = transaction.hash;
        }
        vertices.add(midVertexHash);
        return midVertexHash;
    }

    /**
     * Finalizes a vertex ready to put into a bundle.
     * @param reflectedTail the vertex tail to be finalized
     * @return the bundle fragment ready to put into a bundle
     */
    public List<TransactionBuilder> finalizeVertex(String reflectedTail) {

        if(!InputValidator.isValidHash(reflectedTail))
            return null;

        List<String> edges = new LinkedList<>();

        while(true) {

            Transaction t = transactionsByHash.get(reflectedTail);

            if(t == null)
                break;

            if(Trytes.toTrits(t.tag())[1] == 1)
                break;

            String edge = t.branchHash();
            edges.add(edge);

            reflectedTail = t.trunkHash();

        }

        Transaction head = transactionsByHash.get(reflectedTail);
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
     * Serializes bundle fragments ready to attach to the Tangle.
     * @param vertices the bundle fragments to serialize
     * @return the bundle ready to attach to the Tangle
     */
    public Bundle serialize(List<TransactionBuilder> ... vertices) {
        List<TransactionBuilder> collection = new ArrayList<>();
        for(List<TransactionBuilder> transactionBuilderList: vertices)
            collection.addAll(transactionBuilderList);
        Collections.reverse(collection);
        BundleBuilder bundleBuilder = new BundleBuilder();
        bundleBuilder.append(collection);
        return bundleBuilder.build();
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
        for(Transaction t: transactions) {

            if(InputValidator.hasVertexStartFlagSet(t))
                vertexFragmentStart = true;

            vertexFragment.add(t);

            if(InputValidator.hasVertexEndFlagSet(t))
                if(vertexFragmentStart) {
                    vertices.add(vertexFragment);
                    vertexFragmentStart = false;
                    vertexFragment = new ArrayList<>();
                }

        }

        List<String> tails = new ArrayList<>();

        // Get all edges of found vertices
        for(List<Transaction> vertex: vertices) {

            List<String> edges = new ArrayList<>();
            for(Transaction t: vertex) {
                for(String edge: t.signatureFragments().split("(?<=\\G.{81})"))
                    if(!edge.equals(Trytes.NULL_HASH))
                        edges.add(edge);
            }

            String data = vertex.get(0).extraDataDigest();
            Collections.reverse(edges);

            tails.add(createVertex(data, edges.toArray(new String[edges.size()])));

        }

        return tails.toArray(new String[tails.size()]);

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
        for(String tail: vertices)
            if(isReferencing(tail, vertex))
                ret.add(tail);
        return ret;
    }

    /**
     * Checks if a vertex fragment contains a specific trunk or branch
     * @param vertex the hash of the vertex tail
     * @param neighbor the branch or trunk that is to be checked
     * @return true if vertex contains hash
     * @return false if vertex does not contain hash
     */
    public boolean isReferencing(String vertex, String neighbor) {
        if(vertex.equals(neighbor))
            return false;
        while(true) {
            Transaction transaction = transactionsByHash.get(vertex);
            if(transaction == null)
                return false;
            if(transaction.trunkHash().equals(neighbor) || transaction.branchHash().equals(neighbor))
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
     * Returns all vertices
     * @return all tails of the vertices
     */
    public Set<String> getVertices() {
        return vertices;
    }

}