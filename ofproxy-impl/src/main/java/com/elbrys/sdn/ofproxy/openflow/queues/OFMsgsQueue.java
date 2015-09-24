package com.elbrys.sdn.ofproxy.openflow.queues;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.handlers.OFClientMsg;
import com.elbrys.sdn.ofproxy.openflow.handlers.OFMsgHandler;

public final class OFMsgsQueue implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(OFMsgsQueue.class);

    private LinkedBlockingQueue<OFClientMsg> ofMsgs;
    private boolean running = false;
    private OFMsgHandler ofHandler;

    public OFMsgsQueue(final int msgQueueSize, final ConsumerContext sess) {
        ofMsgs = new LinkedBlockingQueue<OFClientMsg>(msgQueueSize);
        running = false;
        ofHandler = new OFMsgHandler(sess);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            OFClientMsg msg;
            try {
                msg = ofMsgs.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                msg = null;
                e.printStackTrace();
            }
            if (msg != null) {
                // LOG.debug("-- OF message {}", msg);
                ofHandler.consume(msg);
            }
        }
    }

    public void stop() {
        running = false;
    }

    public boolean offer(final OFClientMsg clientMsg) {
        if (!ofMsgs.offer(clientMsg)) {
            LOG.debug("Unable to add element {} to OF message queue. Queue size: {}", clientMsg, ofMsgs.size());
            return false;
        }
        // LOG.debug(" Msg {} added to OF message queue", clientMsg);
        return true;
    }

}
