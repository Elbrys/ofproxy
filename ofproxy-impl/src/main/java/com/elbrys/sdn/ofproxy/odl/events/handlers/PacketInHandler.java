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

/**
 * ODL OacketIn event handler
 * 
 * @author Igor Kondrakhin
 * 
 */
public final class PacketInHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PacketInHandler.class);

    public static void consume(final PacketInEvent event) {

        ClientList cl = OFProxy.getInstance().getConnections(event.getNodePath());
        if (cl != null) {
            for (Client client : cl.getClients().values()) {
                PacketInMessage pim = createPacketIn(client, event.getPkt());
                if (pim != null) {
                    client.send(pim);
                }
            }
        } else {
            // Received PacketIn message from unmanaged or one of the proxy
            // nodes.
            // Skip packet.
        }
    }

    private final static PacketInMessage createPacketIn(final Client client, final PacketReceived pkt) {
        PacketInMessageBuilder pimb = new PacketInMessageBuilder();
        pimb.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        pimb.setXid(client.getXid());
        // ODL is unable to provide bufferId from PacketIn messages, so
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
        final InstanceIdentifier<NodeConnector> ncri = (InstanceIdentifier<NodeConnector>) ncr.getValue();

        final NodeConnectorKey key = InstanceIdentifier.<NodeConnector, NodeConnectorKey> keyOf(ncri);

        String[] split = key.getId().getValue().split(":");
        if (split[split.length - 1].equals("LOCAL")) {
            return 0xfffffffe; // OFPP_LOCAL
        } else {
            return Integer.decode(split[split.length - 1]);
        }
    }
}
