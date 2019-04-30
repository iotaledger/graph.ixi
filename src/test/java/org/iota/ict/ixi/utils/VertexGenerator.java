package org.iota.ict.ixi.utils;

import java.security.SecureRandom;

public class VertexGenerator {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
    private static SecureRandom r = new SecureRandom();

    private static String random(){
        StringBuilder sb = new StringBuilder(81);
        for( int i = 0; i < 81; i++ )
            sb.append( alphabet.charAt( r.nextInt(alphabet.length()) ) );
        return sb.toString();
    }

    public static String[] generateRandomEdges(int edges) {

        String[] e = new String[edges];

        for(int i = 0; i < edges; i++)
            e[i] = random();

        return e;

    }

}
