package com.elbrys.sdn.ofproxy.openflow.queues;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.ClientMsg;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.MsgEncoder;

public final class OutboundMsgQueue implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(OutboundMsgQueue.class);

    private LinkedBlockingQueue<ClientMsg> outboundMsgs;
    private boolean running = false;
    private MsgEncoder encoder;

    public OutboundMsgQueue(final int msgQueueSize) {
        outboundMsgs = new LinkedBlockingQueue<ClientMsg>(msgQueueSize);
        running = false;
        encoder = new MsgEncoder();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            ClientMsg msg;
            try {
                msg = outboundMsgs.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                msg = null;
                e.printStackTrace();
            }
            if (msg != null) {
                msg.getClient().send(msg.getMsg());
//                LOG.debug("OF message sent. {}", msg);
            }
        }
    }

    public void stop() {
        running = false;
    }

    public boolean offer(final ClientMsg clientMsg) {
        if (!outboundMsgs.offer(clientMsg)) {
            LOG.debug("Unable to add element {} to outound queue. Queue size: {}", clientMsg, outboundMsgs.size());
            return false;
        }
//        LOG.debug(" Msg {} added to outound queue", clientMsg);
        return true;
    }

    public MsgEncoder getEncoder() {
        return encoder;
    }

}
