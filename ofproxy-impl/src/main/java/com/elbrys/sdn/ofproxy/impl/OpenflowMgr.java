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

package com.elbrys.sdn.ofproxy.impl;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.ClientList;
import com.elbrys.sdn.ofproxy.openflow.ClientMsg;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;
import com.elbrys.sdn.ofproxy.openflow.handlers.OFClientMsg;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.MsgEncoder;
import com.elbrys.sdn.ofproxy.openflow.queues.InboundMsgQueue;
import com.elbrys.sdn.ofproxy.openflow.queues.OFMsgsQueue;
import com.elbrys.sdn.ofproxy.openflow.queues.OutboundMsgQueue;

/**
 * Class responsible for managing events coming from third party OF controllers
 * 
 * @author igork
 * 
 */
public final class OpenflowMgr {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowMgr.class);

    private static final long CONNECTION_TIMEOUT = 5000;
    private static final int MSG_QUEUE_SIZE = 1000;

    private ExecutorService executor;
    private OFMsgsQueue ofMsgs;
    private InboundMsgQueue inboundMsgs;
    private OutboundMsgQueue outboundMsgs;
    private PacketProcessingService pps;
    private SalFlowService fs;
    private ConcurrentHashMap<InstanceIdentifier<Node>, ClientList> clients;

    /**
     * OpenflowMgr constructor
     * @param sess ODL consumer context
     * @param executor Executor service
     */
    public OpenflowMgr(final ConsumerContext sess, ExecutorService executor) {
        LOG.debug("Openflow manager constructor started.");
        this.executor = executor;
        ofMsgs = new OFMsgsQueue(MSG_QUEUE_SIZE, sess);
        inboundMsgs = new InboundMsgQueue(MSG_QUEUE_SIZE);
        outboundMsgs = new OutboundMsgQueue(MSG_QUEUE_SIZE);
        LOG.debug("Openflow manager constructor finished.");
        LOG.debug("Openflow manager constructor registering node listener.");
        pps = sess.getRpcService(PacketProcessingService.class);
        fs = sess.getRpcService(SalFlowService.class);
        clients = new ConcurrentHashMap<InstanceIdentifier<Node>, ClientList>();
    }

    /**
     * Starts OpenFlowMgr
     */
    public void start() {
        LOG.debug("Openflow manager starting.");
        // Create inbound/outbound threads
        LOG.debug("Openflow manager starting ofMsgs.");
        executor.execute(ofMsgs);
        LOG.debug("Openflow manager starting inboundMsgs.");
        executor.execute(inboundMsgs);
        LOG.debug("Openflow manager starting outboundMsgs.");
        executor.execute(outboundMsgs);
        LOG.debug("Openflow manager started.");

    }

    /**
     * Stops OpenflowMgr
     */
    public void stop() {
        ofMsgs.stop();
        inboundMsgs.stop();
        outboundMsgs.stop();
    }

    /**
     * Creates connection to third party OF controller
     * @param nodePath ODL node path
     * @param cfg Connection configuration
     * @return OF client context
     */
    public Client createConnection(final InstanceIdentifier<Node> nodePath, final ClientConfig cfg) {
        LOG.debug("Openflow manager addClient {} {}.", nodePath, cfg);
        Client sc = new Client(nodePath, cfg, inboundMsgs, this);
        executor.execute(sc);
        try {
            sc.getIsOnlineFuture().get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            sc.close();
            return null;
        }
        return sc;
    }

    /**
     * Consumes message from OF controller
     * @param client OF controller
     * @param ofMsg OF message
     */
    public void consume(final Client client, final DataObject ofMsg) {
        if (!ofMsgs.offer(OFClientMsg.create(client, ofMsg))) {
            LOG.warn("Unable to queue client's OF message. Client {}, OFMessage {}", client, ofMsg);
        }
    }

    /**
     * Sends message to OF controller
     * @param client OF controller
     * @param ofMsg OF message
     */
    public void send(final Client client, final DataObject ofMsg) {
        try {
            ByteBuf out = client.getCtxt().alloc().buffer();
            MsgEncoder encoder = outboundMsgs.getEncoder();
            encoder.messageToBuffer(EncodeConstants.OF10_VERSION_ID, out, ofMsg);
            ClientMsg clientMsg = ClientMsg.create(client, out);
            if (!outboundMsgs.offer(clientMsg)) {
                LOG.warn("Unable to queue outbound OF message. Client {}, OFMessage {}", clientMsg.getClient(),
                        clientMsg.getMsg());
            }
        } catch (Exception e) {
            LOG.error("Unable to serialize message {}", ofMsg, e);
        }
    }

    /**
     * Sends Packet Out message.
     * @param pktOut ODL PacketOut message
     */
    public void transmitPacket(final TransmitPacketInput pktOut) {
        pps.transmitPacket(pktOut);
    }

    /**
     * Sends FlowMod message
     * @param flow ODL FlowMod message
     */
    public void addFlow(final AddFlowInput flow) {
        fs.addFlow(flow);
    }

    /**
     * Creates connection to third party controller.
     * 
     * @param nodePath
     *            ODL node
     * @param cfg
     *            Controller connection configuration
     */
    public void addConnection(InstanceIdentifier<Node> nodePath, ClientConfig cfg) {
        // try to establish connection
        Client client = createConnection(nodePath, cfg);
        if (client != null) {
            // Register client
            ClientList cl = clients.get(nodePath);
            if (cl == null) {
                cl = new ClientList();
                clients.putIfAbsent(nodePath, cl);
            }
            cl.addConnection(client);
        }
    }

    /**
     * Removes connection to third party controllers connected to target node
     * 
     * @param node
     *            ODL node
     */
    public void removeConnections(InstanceIdentifier<Node> node) {
        ClientList cl = clients.remove(node);
        if (cl != null) {
            cl.stop();
        }
    }

    /**
     * Returns list of third party controllers connected to target node
     * 
     * @param nodePath
     *            ODL node
     * @return list of third party controllers connected to target node
     */
    public ClientList getConnections(InstanceIdentifier<Node> nodePath) {
        if (nodePath == null) {
            return null;
        }
        return clients.get(nodePath);
    }
    
    /**
     * Check if connection is established
     * @param nodePath ODL node path
     * @param cc Client configuration
     * @return true if connection is established
     */
    public boolean isClientConnected(InstanceIdentifier<Node> nodePath, ClientConfig cc) {
        ClientList cl = clients.get(nodePath);
        if (cl != null) {
            Client c = cl.getClients().get(cc.getKey());
            if (c != null) {
                LOG.debug("Client {} is already connected", c);
                return true;
            }
        }
        return false;
    }
}
