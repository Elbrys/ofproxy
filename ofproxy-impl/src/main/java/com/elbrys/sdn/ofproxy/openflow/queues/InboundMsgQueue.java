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
                // LOG.debug("-- Inbound msg {}", msg);
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
        // LOG.debug(" Msg {} added to inbound queue", clientMsg);
        return true;
    }

    private void parseOFMsg(final ClientMsg msg) {
        try {
            DataObject ofMsg = decoder.deserialize(msg.getMsg());
            if (ofMsg != null) {
                msg.getClient().consume(ofMsg);
            } else {
                // LOG.warn("Unable to deserialize {}",ByteBufUtils.byteBufToHexString(msg.getMsg()));
            }
        } catch (Exception e) {
            LOG.debug("Unable to parse OF message", e);
        }
    }

}
