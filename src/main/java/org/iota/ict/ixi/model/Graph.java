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
     * This method creates a vertex with trunk pointing to the data and branch pointing to the first outgoing reflected vertex tail that is to be referenced.
     * @param data the hash of the data bundle fragment tail
     * @param edges the hashes of all outgoing reflected vertex tail
     * @return the hash of the reflected vertex tail
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
     * This method starts a reflected vertex with trunk pointing to the data and branch pointing to the first outgoing reflected vertex tail that is to be referenced.
     * @param data the hash of the data bundle fragment tail
     * @param edge the hash of the outgoing reflected vertex tail
     * @return the hash of the created reflected vertex head
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
     * This method continues a reflected vertex with branch pointing to the next outgoing reflected vertex tail that is to be referenced.
     * @param midVertexHash the hash of the reflected vertex tail to be continued
     * @param edge the hash of the outgoing reflected vertex tail
     * @return the hash of the new reflected vertex tail
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
     * This method serializes a reflected vertex into a bundle fragment ready to put into a bundle.
     * @param reflectedTail the hash of the reflected vertex tail to be finalized
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
        Collections.reverse(edges);

        List<TransactionBuilder> transactions = new ArrayList<>();

        TransactionBuilder t = new TransactionBuilder();
        t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 1, 0 }), Transaction.Field.TAG.tryteLength);
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

        // if first transaction == last transaction
        if(Trytes.toTrits(t.tag)[1] == 1)
            t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 1, 1 }), Transaction.Field.TAG.tryteLength);
        else
            t.tag = Trytes.padRight(Trytes.fromTrits(new byte[] { 0, 0, 1 }), Transaction.Field.TAG.tryteLength);

        transactions.add(t);

        return transactions;
    }

    public static Bundle serialize(List<TransactionBuilder> ... vertices) {
        List<TransactionBuilder> collection = new ArrayList<>();
        for(List<TransactionBuilder> transactionBuilderList: vertices)
            collection.addAll(transactionBuilderList);
        BundleBuilder bundleBuilder = new BundleBuilder();
        bundleBuilder.append(collection);
        return bundleBuilder.build();
    }

    public String deserialize(Bundle bundle) {

        List<String> edges = new ArrayList<>();
        for(Transaction t: bundle.getTransactions()) {

            for(String edge: t.signatureFragments().split("(?<=\\G.{81})"))
                if(!edge.equals(Trytes.NULL_HASH))
                    edges.add(edge);

            if(Trytes.toTrits(t.tag())[1] == 1 || t.trunkHash().equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                break;

        }

        String serializedVertexHash = bundle.getTransactions().get(0).hash;
        edges.add(serializedVertexHash);
        String data = bundle.getTransactions().get(0).extraDataDigest();

        return createVertex(data, edges.toArray(new String[edges.size()]));

    }

    /**
     * This method returns the hash of the data bundle fragment tail.
     * @param vertex the hash of the reflected vertex tail
     * @return the hash of the data bundle fragment tail
     */
    public String getData(String vertex) {

        if(!InputValidator.isValidHash(vertex))
            return null;

        Transaction transaction = null;
        while(true) {
            transaction = transactionsByHash.get(vertex);
            if(Trytes.toTrits(transaction.tag())[0] == 1)
                break;
            if(transaction.trunkHash().equals(Trytes.NULL_HASH))
                return null;
            vertex = transaction.trunkHash();
        }
        return transaction.trunkHash();
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
            ret.add(t.branchHash());
            if(Trytes.toTrits(t.tag())[1] == 1 || t.trunkHash().equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                return ret;
            vertex = t.trunkHash();
        }
    }

    /**
     * This method returns the next outgoing edge for the current reflected vertex tail
     * @param vertex the hash of the current reflected vertex tail
     * @param previousEdge the previous edge
     * @return the next outgoing edge for the current reflected vertex tail
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

    // returns all vertices for data hash
    public List<String> getCompoundVertex(String data) {
        return getReferencingVertices(data);
    }

    // returns all vertices with edges incoming to given vertex.
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

    public Map<String,Transaction> getTransactionsByHash() {
        return transactionsByHash;
    }

}