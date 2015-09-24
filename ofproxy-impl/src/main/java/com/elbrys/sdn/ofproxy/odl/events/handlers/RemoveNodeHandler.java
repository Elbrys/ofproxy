package com.elbrys.sdn.ofproxy.odl.events.handlers;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.RemoveNodeEvent;

/**
 * ODL RemoveNode event handler
 * 
 * @author igork
 * 
 */
public final class RemoveNodeHandler {

    public static void consume(final RemoveNodeEvent event) {
        // TODO REmove in release
        if (AddNodeHandler.clientAdded > 0) {
            AddNodeHandler.clientAdded--;
        }
        OFProxy.getInstance().removeConnections(event.getNode());
    }
}
