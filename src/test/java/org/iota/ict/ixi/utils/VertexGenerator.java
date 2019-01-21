package org.iota.ict.ixi.utils;

import org.iota.ict.ixi.model.Graph;
import org.iota.ict.model.TransactionBuilder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class VertexGenerator {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
    private static SecureRandom r = new SecureRandom();

    private static String random(){
        StringBuilder sb = new StringBuilder(81);
        for( int i = 0; i < 81; i++ )
            sb.append( alphabet.charAt( r.nextInt(alphabet.length()) ) );
        return sb.toString();
    }

    public static List<TransactionBuilder> generateRandomVertex(int edges) {

        List<TransactionBuilder> ret = new ArrayList<>();

        if(edges <= 0)
            return ret;

        String[] e = generateRandomEdges(edges).stream().toArray(String[]::new);

        Graph graph = new Graph();
        String tail = graph.createVertex(random(), e);

        return graph.finalizeVertex(tail);

    }

    public static List<String> generateRandomEdges(int edges) {

        List<String> ret = new ArrayList<>();

        if(edges <= 0)
            return ret;

        for(int i = 0; i < edges; i++)
            ret.add(random());

        return ret;

    }

}
