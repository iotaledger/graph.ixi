package org.iota.ict.ixi;

import org.iota.ict.Ict;
import org.iota.ict.utils.Properties;

/**
 * This class is just for testing graph.ixi, so you don't have to run Ict manually.
 * */
public class Main {

    public static void main(String[] args) {

        Properties properties = new Properties();
        Ict ict = new Ict(properties);
        new Thread(new Graph(new IctProxy(ict))).start();

    }

}
