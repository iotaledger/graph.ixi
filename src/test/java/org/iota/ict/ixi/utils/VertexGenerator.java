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

        String[] e = new String[edges];

        for(int i = 0; i < e.length; i++)
            e[i] = random();

        Graph graph = new Graph();
        String tail = graph.createVertex(random(), e);

        return graph.finalizeVertex(tail);

    }

}
