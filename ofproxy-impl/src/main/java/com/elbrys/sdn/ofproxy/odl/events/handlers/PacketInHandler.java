package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.InvalidTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.NoMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.PacketInEvent;
import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.ClientList;

public final class PacketInHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PacketInHandler.class);

    public static void consume(final PacketInEvent event) {
        
        ClientList cl = OFProxy.getInstance().getClientList(event.getNodePath());
        if (cl != null) {
            for (Client client:cl.getClients().values()) {
                PacketInMessage pim = createPacketIn(client, event.getPkt());
                if (pim != null) {
                    client.send(pim);
                }
            }
        } else {
            // Received PacketIn message from unmanaged or one of the proxy nodes.
            // Skip packet.
        }
    }

    private final static PacketInMessage createPacketIn(final Client client, final PacketReceived pkt) {
        PacketInMessageBuilder pimb = new PacketInMessageBuilder();
        pimb.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        pimb.setXid(client.getXid());
        // FIXME ODL unable to provide bufferId from PacketIn messages, so
        // replace it with NO_BUFFER constant
        pimb.setBufferId(0xffffffffl);
        pimb.setInPort(getInPort(pkt));
        pimb.setReason(getReason(pkt.getPacketInReason()));
        pimb.setData(pkt.getPayload());
        return pimb.build();
    }
    
    private static PacketInReason getReason(
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInReason> packetInReason) {
        if (packetInReason.getName().equals(NoMatch.class.getName())) {
            return PacketInReason.OFPRNOMATCH;
        } else if (packetInReason.getName().equals(InvalidTtl.class.getName())) {
            return PacketInReason.OFPRINVALIDTTL;
        } else if (packetInReason.getName().equals(SendToController.class.getName())) {
            return PacketInReason.OFPRACTION;
        } else {
            LOG.warn("Undefined packet in reason. Report as OFPRNOMATCH.");
            return PacketInReason.OFPRNOMATCH;
        }
    }


    public static int getInPort(PacketReceived pkt) {
        // Get the Ingress nodeConnectorRef
        final NodeConnectorRef ncr = pkt.getIngress();

        // Get the instance identifier for the nodeConnectorRef
        @SuppressWarnings("unchecked")
        final InstanceIdentifier<NodeConnector> ncri =
                (InstanceIdentifier<NodeConnector>) ncr.getValue();

        final NodeConnectorKey key =
                InstanceIdentifier.<NodeConnector, NodeConnectorKey>keyOf(ncri);

        String[] split = key.getId().getValue().split(":");
        if (split[split.length - 1].equals("LOCAL")) {
            return 0xfffffffe; //OFPP_LOCAL
        } else {
            return Integer.decode(split[split.length - 1]);
        }
    }
}
