package com.elbrys.sdn.ofproxy.impl;

import io.netty.buffer.ByteBuf;

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
import com.elbrys.sdn.ofproxy.openflow.ClientMsg;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;
import com.elbrys.sdn.ofproxy.openflow.handlers.OFClientMsg;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.MsgEncoder;
import com.elbrys.sdn.ofproxy.openflow.queues.InboundMsgQueue;
import com.elbrys.sdn.ofproxy.openflow.queues.OFMsgsQueue;
import com.elbrys.sdn.ofproxy.openflow.queues.OutboundMsgQueue;

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
	}
    
	public void stop() {
        ofMsgs.stop();
        inboundMsgs.stop();
        outboundMsgs.stop();
	}

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
	
	public Client addClient(final InstanceIdentifier<Node> nodePath, final ClientConfig cfg) {
        LOG.debug("Openflow manager addClient {} {}.", nodePath, cfg);
        // TODO create client's list
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

    public void consume(final Client client, final DataObject ofMsg) {
        if (!ofMsgs.offer(OFClientMsg.create(client, ofMsg))) {
            LOG.warn("Unable to queue client's OF message. Client {}, OFMessage {}", client, ofMsg);
        }
    }

    public void send(final Client client, final DataObject ofMsg) {
        try {
//            LOG.debug("encode  {}", ofMsg);
            ByteBuf out = client.getCtxt().alloc().buffer();
            MsgEncoder encoder = outboundMsgs.getEncoder();
            encoder.messageToBuffer(EncodeConstants.OF10_VERSION_ID, out, ofMsg);
//            LOG.debug("outBuf {}", out);
            ClientMsg clientMsg = ClientMsg.create(client, out);
            if (!outboundMsgs.offer(clientMsg)) {
                LOG.warn("Unable to queue outbound OF message. Client {}, OFMessage {}", clientMsg.getClient(),
                        clientMsg.getMsg());
            }
        } catch (Exception e) {
            LOG.error("Unable to serialize message {}", ofMsg, e);
        }
    }

    public void transmitPacket(final TransmitPacketInput pktOut) {
        // TODO check future after sending PacketOut
        pps.transmitPacket(pktOut);
    }

    public void addFlow(final AddFlowInput flow) {
        // TODO check future after adding flow
        fs.addFlow(flow);
    }
}
