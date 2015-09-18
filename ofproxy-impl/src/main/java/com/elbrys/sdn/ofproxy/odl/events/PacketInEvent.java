package com.elbrys.sdn.ofproxy.odl.events;

import lombok.Getter;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@lombok.AllArgsConstructor(staticName = "create")
public class PacketInEvent implements ODLEvent {
    @Getter
    PacketReceived pkt;

    @Override
    public InstanceIdentifier<Node> getNodePath() {
        // Get the Ingress nodeConnectorRef
        final NodeConnectorRef ncr = pkt.getIngress();

        // Get the instance identifier for the nodeConnectorRef
        @SuppressWarnings("unchecked")
        final InstanceIdentifier<NodeConnector> ncri = (InstanceIdentifier<NodeConnector>) ncr.getValue();
        // Get the instanceID for the Node in the tree above us
        return ncri.<Node> firstIdentifierOf(Node.class);
    }
    
    @Override
    public boolean isCheckNode() {
        return true;
    }
}
