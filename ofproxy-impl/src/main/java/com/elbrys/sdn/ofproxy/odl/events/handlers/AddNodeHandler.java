package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.AddNodeEvent;
import com.elbrys.sdn.ofproxy.openflow.ClientConfigList;
import com.elbrys.sdn.ofproxy.openflow.ClientNode;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;

/**
 * ODL AddNode event handler
 * 
 * @author igork
 * 
 */
public final class AddNodeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AddNodeHandler.class);

    public static void consume(final AddNodeEvent event) {
        ClientNode cn = new ClientNode(event.getNodePath());
        ClientConfigList ccl = OFProxy.getInstance().getConfiguredClient(cn.getDatapathId().toString());
        if (ccl != null) {
            for (ClientConfig cc : ccl.getClients().values()) {
                if (!OFProxy.getInstance().isClientConnected(event.getNodePath(), cc)) {
                    LOG.debug("Add Client {} ", cc);
                    OFProxy.getInstance().addConnection(event.getNodePath(), cc);
                }
            }
        }
    }
}
