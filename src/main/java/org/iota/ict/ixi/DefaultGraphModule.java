package org.iota.ict.ixi;

import org.iota.ict.ixi.model.Graph;

import java.util.ArrayList;
import java.util.List;

public class DefaultGraphModule extends  AbstractGraphModule {

    private List<Graph> graphs = new ArrayList<>();

    public DefaultGraphModule(Ixi ixi) {
        super(ixi);
    }

    @Override
    public void run() { ; }

    public void addGraph(Graph graph) {
        graphs.add(graph);
    }

    public void removeGraph(Graph graph) {
        graphs.remove(graph);
    }

}
