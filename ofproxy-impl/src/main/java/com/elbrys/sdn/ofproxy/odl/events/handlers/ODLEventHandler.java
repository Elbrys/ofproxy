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

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.odl.events.AddNodeEvent;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;
import com.elbrys.sdn.ofproxy.odl.events.ODLEvent;
import com.elbrys.sdn.ofproxy.odl.events.PacketInEvent;
import com.elbrys.sdn.ofproxy.odl.events.RemoveNodeEvent;

/**
 * ODL event handler
 * 
 * @author igork
 * 
 */
public final class ODLEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ODLEventHandler.class);

    public ODLEventHandler(final ConsumerContext sess) {
    }

    public void consume(final ODLEvent msg) {
        try {
            if (msg instanceof AddNodeEvent) {
                AddNodeHandler.consume((AddNodeEvent) msg);
            } else if (msg instanceof RemoveNodeEvent) {
                RemoveNodeHandler.consume((RemoveNodeEvent) msg);
            } else if (msg instanceof FlowRemovedEvent) {
                FlowRemovedHandler.consume((FlowRemovedEvent) msg);
            } else if (msg instanceof PacketInEvent) {
                PacketInHandler.consume((PacketInEvent) msg);
            } else {
                LOG.debug(" >>>>>>>>>>>  Unexpected ODL event {}.", msg);
            }

        } catch (Exception e) {
            LOG.debug("parseOFMsg ", e);
        }
    }
}
