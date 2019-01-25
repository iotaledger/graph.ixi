package org.iota.ict.ixi.model;

import org.iota.ict.ixi.util.InputValidator;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.BundleBuilder;
import org.iota.ict.model.Transaction;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.*;

public class Graph {

    private Map<String, Transaction> transactionsByHash = Collections.synchronizedMap(new LinkedHashMap());

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
        for(String edge: edges) {
            TransactionBuilder transactionBuilder = new TransactionBuilder();
            transactionBuilder.trunkHash = midVertexHash;
            transactionBuilder.branchHash = edge;
            Transaction transaction = transactionBuilder.build();
            transactionsByHash.put(transaction.hash, transaction);
            midVertexHash = transaction.hash;
        }
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
     * Deserializes and adds all vertices included in a bundle to the graph.
     * @param bundle the bundle to deserialize
     * @return to tails of the added vertices
     */
    public String[] deserializeAndAdd(Bundle bundle) {
        return deserializeAndStore(bundle.getTransactions());
    }

    /**
     * Deserializes and adds all vertices included in a bundle fragment to the graph.
     * @param transactions the bundle fragment to deserialize
     * @return to tails of the added vertices
     */
    public String[] deserializeAndStore(List<Transaction> transactions) {

        List<String> tails = new ArrayList<>();

        // Dissociate vertices from bundle

        List<List<Transaction>> vertices = new ArrayList<>();

        boolean vertexStart = false;
        List<Transaction> vertex = new ArrayList<>();
        for(Transaction t: transactions) {

            if(InputValidator.hasVertexStartFlagSet(t))
                vertexStart = true;

            vertex.add(t);

            if(InputValidator.hasVertexEndFlagSet(t))
                if(vertexStart) {
                    vertices.add(vertex);
                    vertexStart = false;
                    vertex = new ArrayList<>();
                }

        }

        // Get all edges of found vertices

        List<List<String>> edgesOfAllVertices = new ArrayList<>();

        for(List<Transaction> v: vertices) {

            List<String> edges = new ArrayList<>();
            for(Transaction t: v) {
                for(String edge: t.signatureFragments().split("(?<=\\G.{81})"))
                    if(!edge.equals(Trytes.NULL_HASH))
                        edges.add(edge);

                if(Trytes.toTrits(t.tag())[1] == 1) // check if last transaction of bundle
                    break;
            }

            String serializedVertexHash = transactions.get(0).hash;
            edges.add(serializedVertexHash);
            String data = transactions.get(0).extraDataDigest();

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

            if(Trytes.toTrits(t.tag())[1] == 1 || t.trunkHash().equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                return null;

            vertex = t.trunkHash();

        }

    }

    /**
     * Returns all vertices which point to a specific data bundle fragment
     * @param data the data bundle fragment tail
     * @return the list of all vertices which point to the data bundle fragment
     */
    public List<String> getCompoundVertex(String data) {
        return getReferencingVertices(data);
    }

    /**
     * Returns all vertices which point to given vertex
     * @param vertex the vertex tail to be checked
     * @return all vertices with edges incoming to given vertex
     */
    public List<String> getReferencingVertices(String vertex) {
        List<String> ret = new ArrayList<>();
        for(Transaction transaction: transactionsByHash.values())
            if(isDescendant(transaction.hash, vertex)) {
                ret.add(transaction.hash);
                for(String descendant: new ArrayList<>(ret))
                    if(isDescendant(transaction.hash, descendant))
                        ret.remove(descendant);
            }
        return ret;
    }

    /**
     * Checks if a vertex fragment contains a specific trunk or branch
     * @param vertex the hash of the vertex tail
     * @param descendant the branch or trunk that is to be checked
     * @return true if vertex contains hash
     * @return false if vertex does not contain hash
     */
    public boolean isDescendant(String vertex, String descendant) {
        if(vertex.equals(descendant))
            return false;
        while(true) {
            Transaction transaction = transactionsByHash.get(vertex);
            if(transaction == null)
                return false;
            if(transaction.trunkHash().equals(descendant) || transaction.branchHash().equals(descendant))
                return true;
            vertex = transaction.trunkHash();
        }
    }

    /**
     * Returns next vertex which points to a specific data bundle fragment
     * @param data the data bundle fragment tail
     * @return the next vertex which points to the data bundle fragment
     */
    public String getNextCompoundVertex(String data, String previousVertex) {
        List<String> vertices = getCompoundVertex(data);
        for(int i = 0; i < vertices.size(); i++)
            if(previousVertex.equals(vertices.get(i)))
                if(i + 1 < vertices.size())
                    return vertices.get(i + 1);
        return null;
    }

    /**
     * Returns the next vertex which points to given vertex
     * @param vertex the vertex tail to be checked
     * @return the next vertex with an edge incoming to a given vertex
     */
    public String getNextReferencingVertex(String vertex, String previousVertex) {
        List<String> vertices = getReferencingVertices(vertex);
        for(int i = 0; i < vertices.size(); i++)
            if(previousVertex.equals(vertices.get(i)))
                if(i + 1 < vertices.size())
                    return vertices.get(i + 1);
        return null;
    }

    /**
     * Returns all transactions of the graph
     * @return all transactions of the graph
     */
    public Map<String,Transaction> getTransactionsByHash() {
        return transactionsByHash;
    }

}