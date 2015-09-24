package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.AddNodeEvent;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;

/**
 * ODL AddNode event handler
 * 
 * @author igork
 * 
 */
public final class AddNodeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AddNodeHandler.class);
    // TODO remove clientAdded flag in release version.
    public static int clientAdded = 0;

    public static void consume(final AddNodeEvent event) {
        // TODO check configuration to verify that we need to create commection
        // to third party controller.
        if (clientAdded == 0) {
            // Get client configuration from configuration file
            ClientConfig cc = ClientConfig.create("127.0.0.1", 6633, false);
            clientAdded++;
            // TODO remove sleep in release.
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.debug("Add Client config: {} ", cc);
            OFProxy.getInstance().addConnection(event.getNode(), cc);
        }
    }
}
