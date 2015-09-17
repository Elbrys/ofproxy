package com.elbrys.sdn.ofproxy.odl.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.odl.events.ODLEvent;
import com.elbrys.sdn.ofproxy.odl.events.handlers.ODLEventHandler;

public final class ODLEventQueue implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ODLEventQueue.class);

    private LinkedBlockingQueue<ODLEvent> ofMsgs;
    private boolean running = false;
    private ODLEventHandler odlHandler;

    public ODLEventQueue(final int msgQueueSize, final ConsumerContext sess) {
        ofMsgs = new LinkedBlockingQueue<ODLEvent>(msgQueueSize);
        running = false;
        odlHandler = new ODLEventHandler(sess);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            ODLEvent msg;
            try {
                msg = ofMsgs.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                msg = null;
                e.printStackTrace();
            }
            if (msg != null) {
                LOG.debug("-- ODL message {}", msg);
                odlHandler.consume(msg);
            }
        }
    }

   public void stop() {
        running = false;
    }

    public boolean offer(final ODLEvent odlEvent) {
        if (!ofMsgs.offer(odlEvent)) {
            LOG.debug("Unable to add element {} to ODL event queue. Queue size: {}", odlEvent, ofMsgs.size());
            return false;
        }
//        LOG.debug(" Msg {} added to OF message queue", clientMsg);
        return true;
    }


 }
