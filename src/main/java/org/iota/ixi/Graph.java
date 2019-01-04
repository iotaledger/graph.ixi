package org.iota.ixi;

import org.iota.ict.ixi.DefaultIxiModule;
import org.iota.ict.ixi.IctProxy;
import org.iota.ict.model.Bundle;
import org.iota.ict.model.Transaction;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;
import org.iota.ict.utils.Trytes;
import org.iota.ixi.utils.VertexGenerator;

import java.util.ArrayList;
import java.util.List;

public class Graph extends DefaultIxiModule {

    public Graph(IctProxy ict) {
        super(ict);
    }

    @Override
    public void run() {

        Bundle b = VertexGenerator.generateVertex();
        for(Transaction t: b.getTransactions())
            submit(t);

        List<String> e = getEdges(b.getTransactions().get(0).hash);

        for(String s: e)
            System.out.println(s);

    }



    // returns all outgoing edges for the current vertex hash
    public List<String> getEdges(String vertex) {

        List<String> ret = new ArrayList<>();

        while(true) {

            Transaction t = findTransactionByHash(vertex);

            for(String edge: t.signatureFragments.split("(?<=\\G.{81})"))
                if(!edge.equals(Trytes.NULL_HASH))
                    ret.add(edge);

            if(Trytes.toTrits(t.tag)[0] == 1 || t.trunkHash.equals(Trytes.NULL_HASH)) // check if last transaction of vertex
                break;

            vertex = t.trunkHash;

        }

        return ret;

    }




    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        System.out.println("vertex received");
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        System.out.println("vertex submitted");
    }

    @Override
    public void onIctShutdown() {
        super.onIctShutdown();
    }

}
