package com.elbrys.sdn.ofproxy.openflow.handlers;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;

public final class OF10PacketOutInputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OF10PacketOutInputHandler.class);

    public static void consume(final OFClientMsg msg) {
        // TODO replace necessary fields (xid, etc.)
//        LOG.trace("PacketOut message is forwarded to the switch.");
        // TODO restore functionality later
//        sendPacketOut(msg.getClient(), (PacketOutInput) msg.getMsg());
    }
    
    public static void sendPacketOut(final Client client,
            final PacketOutInput pkt) {
        NodeRef ref = new NodeRef(client.getNodePath());

        TransmitPacketInputBuilder tPackBuilder =
                new TransmitPacketInputBuilder()
                .setPayload(pkt.getData()).setNode(ref)
                .setConnectionCookie(null);

        tPackBuilder.setBufferId(pkt.getBufferId());
        tPackBuilder.setIngress(client.getNodeConnectorRef(pkt.getInPort().getValue()));

        List<Action> actions = pkt.getAction();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> retVal = new ArrayList<>();

        // TODO we only support OUTPUT action for now
        int egressPortNumber = 0;
        for (Action action : actions) {
//            LOG.debug("Action {} to be converted", action);
            if (action.getType() == org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class) {
                PortAction pa = action.getAugmentation(PortAction.class);
                if (pa != null) {
                    egressPortNumber = pa.getPort().getValue().intValue();
                    OutputActionBuilder output = new OutputActionBuilder().setMaxLength(0xffff).setOutputNodeConnector(
                            client.getNodeConnectorIdByPortNumber(egressPortNumber));
                    retVal.add(new ActionBuilder().setOrder(0)
                            .setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build()).build());
                }
            }
        }
      
      // Try to find egress port in the actions.
      tPackBuilder.setEgress(client.getNodeConnectorRef(egressPortNumber));

      // TODO Get transaction ID from ODL
      // There is no way to set or get Xid from Packet out massages
      // So if error is return we are unable to track it back to message
      // that causes it.
      try {
          client.transmitPacket(tPackBuilder.build());
      } catch (Exception e) {
          LOG.warn("Unable to forward PacketOut message.", e);
      }
    }
}
