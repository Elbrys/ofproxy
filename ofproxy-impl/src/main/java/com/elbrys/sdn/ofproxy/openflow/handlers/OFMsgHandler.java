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

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.connection.ClientHandshake;
import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;

/**
 * OF message handler
 * 
 * @author Igor Kondrakhin
 * 
 */
public final class OFMsgHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OFMsgHandler.class);

    OF10StatsRequestHandler oF10StatRequestHandler;
    OF10FlowModInputHandler oF10FlowModHandler;

    public OFMsgHandler(final ConsumerContext sess) {
        oF10StatRequestHandler = new OF10StatsRequestHandler(sess);
        oF10FlowModHandler = new OF10FlowModInputHandler();
    }

    public void consume(final OFClientMsg msg) {
        try {
            DataObject om = msg.getMsg();
            if (om instanceof HelloMessage) {
                LOG.debug(" Received Hello");
                ClientHandshake.getInstance().onHelloReceived((HelloMessage) om);
            } else if (om instanceof OF10FeaturesRequestMessage) {
                LOG.debug(" Received FeaturesRequestMessage.");
                OF10FeaturesRequestHandler.consume(msg);
            } else if (om instanceof SetConfigInput) {
                LOG.debug(" Received SetConfigInput.");
                SetConfigInputHandler.consume(msg);
            } else if (om instanceof MultipartRequestInput) {
                // LOG.debug(" Received MultipartRequestInput.");
                // oF10StatRequestHandler.consume(msg);
            } else if (om instanceof PacketOutInput) {
                // LOG.trace(" Received PacketOutInput.");
                OF10PacketOutInputHandler.consume(msg);
            } else if (om instanceof FlowModInput) {
                LOG.trace(" Received FlowModInput.");
                oF10FlowModHandler.consume(msg);
            } else if (om instanceof EchoRequestMessage) {
                LOG.trace(" Received EchoRequestMessage.");
                EchoRequestHandler.consume(msg);
            } else if (om instanceof BarrierInput) {
                LOG.trace(" Received BarrierInput.");
                // TODO ODL allows to send Barrier Request to particular node
                // only
                LOG.trace(" ODL does not allow to send separate Barrier Mesage.");
            } else {
                LOG.debug(" >>>>>>>>>>>  Unexpected message {}.", om);
            }

        } catch (Exception e) {
            LOG.debug("parseOFMsg ", e);
        }
    }
}
