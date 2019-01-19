package org.iota.ict.ixi;

import org.iota.ict.Ict;
import org.iota.ict.utils.properties.Properties;
import org.junit.*;

public abstract class GraphTestTemplate {

    protected Ict ict;
    protected Graph graph;

    @Before
    public void setUp() {
        ict = new Ict(new Properties().toFinal());
        graph = new Graph(ict);
    }

    @After
    public void tearDown() {
        graph.terminate();
        ict.terminate();
    }

}
