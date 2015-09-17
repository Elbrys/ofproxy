package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;
import com.elbrys.sdn.ofproxy.openflow.ClientList;

public final class FlowRemovedHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRemovedHandler.class);
    // TODO remove clientAdded flag in release version.
    public static int clientAdded = 0 ;

    public static void consume(final FlowRemovedEvent event) {
        
        @SuppressWarnings("unchecked")
        ClientList cl = OFProxy.getInstance().getClientList((InstanceIdentifier<Node>) event.getFlow().getNode().getValue());
        if (cl != null) {
            cl.onFlowRemoved(event.getFlow());
        } else {
            LOG.warn("Unexpected messsage {}", event);
        }
    }
}
