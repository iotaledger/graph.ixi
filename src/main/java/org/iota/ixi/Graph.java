package org.iota.ixi;

import org.iota.ict.ixi.DefaultIxiModule;
import org.iota.ict.ixi.IctProxy;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

public class Graph extends DefaultIxiModule {

    public Graph(IctProxy ict) {
        super(ict);
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        super.onTransactionReceived(event);
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        super.onTransactionSubmitted(event);
    }

    @Override
    public void onIctShutdown() {
        super.onIctShutdown();
    }

}
