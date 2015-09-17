/*
 * Copyright (c) 2012, Elbrys Networks
 * All Rights Reserved.
 */

package com.elbrys.sdn.ofproxy.odl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.openflow.ClientList;

public final class PacketInListener implements PacketProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(PacketInListener.class);

    public PacketInListener() {
    }

    @Override
    public void onPacketReceived(final PacketReceived pkt) {
        ClientList cl = OFProxy.getInstance().getClientList(getNodePath(pkt));
        if (cl != null) {
            cl.onPacketIn(pkt);
        } else {
            LOG.trace("Message from unmanaged node {}", pkt);
        }
    }

    public InstanceIdentifier<Node> getNodePath(PacketReceived pkt) {

        // Get the Ingress nodeConnectorRef
        final NodeConnectorRef ncr = pkt.getIngress();

        // Get the instance identifier for the nodeConnectorRef
        @SuppressWarnings("unchecked")
        final InstanceIdentifier<NodeConnector> ncri = (InstanceIdentifier<NodeConnector>) ncr.getValue();

        // Get the instance identifier for the Node in the tree above us
        return ncri.<Node> firstIdentifierOf(Node.class);
    }
}
