package com.elbrys.sdn.ofproxy.openflow.handlers;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ActionConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
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
        // TODO replace necessary fields (xid, etc.) Check if packet should be
        // forwarded to client
        LOG.trace("PacketOut message is forwarded to the switch.");
        sendPacketOut(msg.getClient(), (PacketOutInput) msg.getMsg());
    }

    public static void sendPacketOut(final Client client, final PacketOutInput pkt) {
        NodeRef ref = new NodeRef(client.getNodePath());

        TransmitPacketInputBuilder tPackBuilder = new TransmitPacketInputBuilder().setPayload(pkt.getData())
                .setNode(ref).setConnectionCookie(null);

        tPackBuilder.setBufferId(pkt.getBufferId());
        tPackBuilder.setIngress(client.getNodeConnectorRef(pkt.getInPort().getValue()));

        // Convert OF actions to ODL actions
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> mdActions = ActionConvertor
                .toMDSalActions(
                        pkt.getAction(),
                        OpenflowVersion.OF10,
                        ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION);
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> odlActions = new ArrayList<>();
        int actionKey = 0;
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action : mdActions) {
            ActionBuilder wrappedAction = new ActionBuilder();
            wrappedAction.setAction(action);
            wrappedAction.setKey(new ActionKey(actionKey));
            wrappedAction.setOrder(actionKey);
            odlActions.add(wrappedAction.build());
            actionKey++;
        }
        tPackBuilder.setAction(odlActions);

        // ODL requires egress port to be set, find output action and convert it
        // to port number
        // FIXME It is not clear what should be done if PACKET_OUT received with
        // action DROP (no actions present)
        List<Action> actionList = pkt.getAction();
        int egressPortNumber = 0;
        for (Action ofAction : actionList) {
            if (ofAction.getType() == org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class) {
                PortAction pa = ofAction.getAugmentation(PortAction.class);
                if (pa != null) {
                    egressPortNumber = pa.getPort().getValue().intValue();
                    // OutputActionBuilder output = new
                    // OutputActionBuilder().setMaxLength(0xffff).setOutputNodeConnector(
                    // client.getNodeConnectorIdByPortNumber(egressPortNumber));
                    // retVal.add(new ActionBuilder().setOrder(0)
                    // .setAction(new
                    // OutputActionCaseBuilder().setOutputAction(output.build()).build()).build());
                }
            }
        }
        tPackBuilder.setEgress(client.getNodeConnectorRef(egressPortNumber));

        // TODO Modify ODL to return transaction ID for transmitted packet
        // There is no way to set or get Xid from Packet out messages
        // So if error is return we are unable to track it back to message
        // that causes it.
        try {
            client.transmitPacket(tPackBuilder.build());
        } catch (Exception e) {
            LOG.warn("Unable to forward PacketOut message.", e);
        }
    }
}
