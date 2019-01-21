package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Graph;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class GraphTestTemplate {

    protected static Graph graph;

    @BeforeClass
    public static void setUp() {
        graph = new Graph();
    }

    @After
    public void tearDown() {
        graph.getTransactionsByHash().clear();
    }

}
