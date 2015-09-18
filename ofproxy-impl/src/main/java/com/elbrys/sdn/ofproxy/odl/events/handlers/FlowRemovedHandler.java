package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorV10Impl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedReasonFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;
import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.ClientList;

public final class FlowRemovedHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRemovedHandler.class);

    public static void consume(final FlowRemovedEvent event) {
        
        @SuppressWarnings("unchecked")
        ClientList cl = OFProxy.getInstance().getClientList((InstanceIdentifier<Node>) event.getFlow().getNode().getValue());
        if (cl != null) {
            for (Client client:cl.getClients().values()) {
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
