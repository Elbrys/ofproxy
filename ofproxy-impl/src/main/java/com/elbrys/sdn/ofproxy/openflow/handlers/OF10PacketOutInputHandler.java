/*
 * Copyright (c) 2015,  BROCADE COMMUNICATIONS SYSTEMS, INC
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

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
        // TODO Check if packet should be forwarded to client
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
        // to port number. Use port number 0 if there is no OUTPUT action
        // present
        // FIXME It is not clear what should be done if PACKET_OUT received with
        // action DROP (no actions present)
        List<Action> actionList = pkt.getAction();
        int egressPortNumber = 0;
        for (Action ofAction : actionList) {
            if (ofAction.getType() == org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class) {
                PortAction pa = ofAction.getAugmentation(PortAction.class);
                if (pa != null) {
                    egressPortNumber = pa.getPort().getValue().intValue();
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
