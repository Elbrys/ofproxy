package com.elbrys.sdn.ofproxy.openflow.queues;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.ClientMsg;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.MsgDecoder;

public final class InboundMsgQueue implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(InboundMsgQueue.class);

    private LinkedBlockingQueue<ClientMsg> inboundMsgs;
    private boolean running = false;
    private MsgDecoder decoder;

    public InboundMsgQueue(final int msgQueueSize) {
        inboundMsgs = new LinkedBlockingQueue<ClientMsg>(msgQueueSize);
        running = false;
        decoder = new MsgDecoder();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            ClientMsg msg;
            try {
                msg = inboundMsgs.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                msg = null;
                e.printStackTrace();
            }
            if (msg != null) {
//                LOG.debug("-- Inbound msg {}", msg);
                parseOFMsg(msg);
            }
        }
    }

   public void stop() {
        running = false;
    }

    public boolean offer(final ClientMsg clientMsg) {
        if (!inboundMsgs.offer(clientMsg)) {
            LOG.debug("Unable to add element {} to inbound queue. Queue size: {}", clientMsg, inboundMsgs.size());
            return false;
        }
//        LOG.debug(" Msg {} added to inbound queue", clientMsg);
        return true;
    }

    private void parseOFMsg(final ClientMsg msg) {
        try {
            DataObject ofMsg = decoder.deserialize(msg.getMsg());
            if (ofMsg != null) {
               msg.getClient().consume(ofMsg);
            } else {
//                LOG.warn("Unable to deserialize {}",ByteBufUtils.byteBufToHexString(msg.getMsg()));
            }
        } catch (Exception e) {
            LOG.debug("Unable to parse OF message", e);
        }
    }

 }
