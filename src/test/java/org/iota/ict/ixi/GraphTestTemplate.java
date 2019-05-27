package org.iota.ict.ixi;

import org.iota.ict.Ict;
import org.iota.ict.utils.properties.EditableProperties;
import org.junit.After;
import org.junit.Before;

import java.util.Set;

public abstract class GraphTestTemplate {

    protected Ict ict1;
    protected Ict ict2;
    protected static GraphModule graphModule1;
    protected static GraphModule graphModule2;

    @Before
    public void setup() throws Exception {
        EditableProperties properties1 = new EditableProperties().host("localhost").port(1337).minForwardDelay(0).maxForwardDelay(10).guiEnabled(false);
        EditableProperties properties2 = new EditableProperties().host("localhost").port(1338).minForwardDelay(0).maxForwardDelay(10).guiEnabled(false);

        ict1 = new Ict(properties1.toFinal());
        ict2 = new Ict(properties2.toFinal());

        ict1.getModuleHolder().initAllModules();
        graphModule1 = (GraphModule) ict1.getModuleHolder().loadVirtualModule(GraphModule.class, "Graph.ixi");
        ict1.getModuleHolder().startAllModules();

        ict2.getModuleHolder().initAllModules();
        graphModule2 = (GraphModule) ict2.getModuleHolder().loadVirtualModule(GraphModule.class, "Graph.ixi");
        ict2.getModuleHolder().startAllModules();

        addNeighborToIct(ict1,ict2);
        addNeighborToIct(ict2,ict1);
    }

    @After
    public void tearDown() {
        ict1.terminate();
        ict2.terminate();
    }

    protected final static void addNeighborToIct(Ict ict, Ict neighbor) {
        EditableProperties properties = ict.getProperties().toEditable();
        Set<String> neighbors = properties.neighbors();
        neighbors.add(neighbor.getAddress().getHostName() + ":" + neighbor.getAddress().getPort());
        properties.neighbors(neighbors);
        ict.updateProperties(properties.toFinal());
    }

}
