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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorV10Impl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedReasonFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;
import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.ClientList;

/**
 * ODL Flow removed event handler
 * 
 * @author Igor Kondrakhin
 * 
 */
public final class FlowRemovedHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRemovedHandler.class);

    public static void consume(final FlowRemovedEvent event) {

        ClientList cl = OFProxy.getInstance().getConnections(event.getNodePath());
        if (cl != null) {
            for (Client client : cl.getClients().values()) {
                FlowRemovedMessage frm = createFlowRemoved(client, event.getFlow());
                client.send(frm);
            }
        } else {
            LOG.warn("Unexpected messsage {}", event);
        }
    }

    private final static FlowRemovedMessage createFlowRemoved(final Client client, final SwitchFlowRemoved fre) {
        FlowRemovedMessageBuilder frmb = new FlowRemovedMessageBuilder();
        MatchConvertorV10Impl matchConvertorV10 = new MatchConvertorV10Impl();
        frmb.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        frmb.setXid(client.getXid());
        frmb.setMatchV10(matchConvertorV10.convert(fre.getMatch(), client.getDatapathId()));
        frmb.setCookie(fre.getCookie().getValue());
        frmb.setPriority(fre.getPriority());
        frmb.setReason(getReason(fre.getRemovedReason()));
        frmb.setDurationSec(fre.getDurationSec());
        frmb.setDurationNsec(fre.getDurationNsec());
        frmb.setIdleTimeout(fre.getIdleTimeout());
        frmb.setPacketCount(fre.getPacketCount());
        frmb.setByteCount(fre.getByteCount());
        return frmb.build();
    }

    private static FlowRemovedReason getReason(final RemovedReasonFlags removedReason) {
        if (removedReason.isDELETE()) {
            return FlowRemovedReason.OFPRRDELETE;
        } else if (removedReason.isGROUPDELETE()) {
            return FlowRemovedReason.OFPRRGROUPDELETE;
        } else if (removedReason.isHARDTIMEOUT()) {
            return FlowRemovedReason.OFPRRHARDTIMEOUT;
        } else if (removedReason.isIDLETIMEOUT()) {
            return FlowRemovedReason.OFPRRIDLETIMEOUT;
        } else {
            LOG.warn("Undefined flow removeal reason. Report as DELETE.");
            return FlowRemovedReason.OFPRRGROUPDELETE;
        }
    }
}
