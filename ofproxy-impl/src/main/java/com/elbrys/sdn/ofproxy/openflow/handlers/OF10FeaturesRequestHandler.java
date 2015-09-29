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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityArpMatchIp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityIpReasm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityStp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.CommonPort.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;

public final class OF10FeaturesRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OF10FeaturesRequestHandler.class);

    public static void consume(final OFClientMsg msg) {
        Client client = msg.getClient();
        GetFeaturesOutput gfo = getFeaturesOutput(msg.getClient());
        if (gfo == null) {
            LOG.warn("Unable to create feature reply.");
            return;
        }
        client.send(gfo);
    }

    private static GetFeaturesOutput getFeaturesOutput(final Client client) {
        Node node = client.getNode();
        if (node == null) {
            return null;
        }

        FlowCapableNode fcn = client.getFlowCapableNode();
        if (fcn == null) {
            return null;
        }
        GetFeaturesOutputBuilder gfob = new GetFeaturesOutputBuilder();
        gfob.setXid(client.getXid());
        gfob.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        // FIXME in release Only one controller has been used in development test environment.
        // Add one to DPID To avoid creating multiple controller connections 
        gfob.setDatapathId(BigInteger.valueOf(client.getDatapathId().longValue() + 1));
        SwitchFeatures sf = fcn.getSwitchFeatures();
        gfob.setBuffers(sf.getMaxBuffers());
        gfob.setTables(sf.getMaxTables());
        gfob.setCapabilitiesV10(buildCapabilitiesV10(sf.getCapabilities()));
        // FIX ME Unable to get node's Actions list from ODL.
        // It may cause issues with other controllers, so report that node
        // supports all possible actions. If action is not supported by node,
        // then it will return error if invalid action is used by controller.
        gfob.setActionsV10(buildAllActionsV10());
        // Get port info
        List<PhyPort> ports = new ArrayList<>();
        for (NodeConnector nc : node.getNodeConnector()) {
            FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
            if (fcnc instanceof FlowCapableNodeConnector) {
                ports.add(buildPortInfo(fcnc));
            }
        }
        gfob.setPhyPort(ports);
        return gfob.build();
    }

    private static PhyPort buildPortInfo(final FlowCapableNodeConnector fcnc) {

        PhyPortBuilder pb = new PhyPortBuilder();
        pb.setPortNo(getPortNumberFromCommonPort(fcnc.getPortNumber()));
        pb.setHwAddr(fcnc.getHardwareAddress());
        pb.setName(fcnc.getName());
        pb.setConfigV10(buildConfigV10(fcnc.getConfiguration()));
        pb.setStateV10(buildPortStateV10(fcnc.getState()));
        pb.setCurrentFeaturesV10(buildPortFeaturesV10(fcnc.getCurrentFeature()));
        pb.setAdvertisedFeaturesV10(buildPortFeaturesV10(fcnc.getAdvertisedFeatures()));
        pb.setSupportedFeaturesV10(buildPortFeaturesV10(fcnc.getSupported()));
        pb.setPeerFeaturesV10(buildPortFeaturesV10(fcnc.getPeerFeatures()));
        return pb.build();
    }

    private static long getPortNumberFromCommonPort(PortNumber portNumber) {
        if (portNumber.getUint32() != null) {
            return portNumber.getUint32().longValue();
        } else {
            // Port LOCAL is only OF port used in OF feature replies.
            if (portNumber.getString() != null && "LOCAL".equals(portNumber.getString())) {
                return 0xfffffffel;
            } else {
                return 0;
            }
        }
    }

    private static PortFeaturesV10 buildPortFeaturesV10(final PortFeatures feature) {
        final Boolean _10mbHd = feature.isTenMbHd();
        final Boolean _10mbFd = feature.isTenMbFd();
        final Boolean _100mbHd = feature.isHundredMbHd();
        final Boolean _100mbFd = feature.isHundredMbFd();
        final Boolean _1gbHd = feature.isOneGbHd();
        final Boolean _1gbFd = feature.isOneGbFd();
        final Boolean _10gbFd = feature.isTenGbFd();
        final Boolean _copper = feature.isCopper();
        final Boolean _fiber = feature.isFiber();
        final Boolean _autoneg = feature.isAutoeng();
        final Boolean _pause = feature.isPause();
        final Boolean _pauseAsym = feature.isPauseAsym();
        return new PortFeaturesV10(_100mbFd, _100mbHd, _10gbFd, _10mbFd, _10mbHd, _1gbFd, _1gbHd, _autoneg, _copper,
                _fiber, _pause, _pauseAsym);
    }

    private static PortStateV10 buildPortStateV10(final State state) {
        // ODL is not saving some port state parameters. Set them to false
        final Boolean _linkDown = state.isLinkDown();
        final Boolean _blocked = state.isBlocked();
        final Boolean _live = state.isLive();
        final Boolean _stpListen = false;
        final Boolean _stpLearn = false;
        final Boolean _stpForward = false;
        final Boolean _stpBlock = false;
        final Boolean _stpMask = false;
        return new PortStateV10(_blocked, _linkDown, _live, _stpBlock, _stpForward, _stpLearn, _stpListen, _stpMask);
    }

    private static PortConfigV10 buildConfigV10(final PortConfig cfg) {
        // ODL is not saving some port configuration parameters. Set them to
        // false
        final Boolean _portDown = cfg.isPORTDOWN();
        final Boolean _noStp = false;
        final Boolean _noRecv = cfg.isNORECV();
        final Boolean _noRecvStp = false;
        final Boolean _noFlood = false;
        final Boolean _noFwd = cfg.isNOFWD();
        final Boolean _noPacketIn = cfg.isNOPACKETIN();
        return new PortConfigV10(_noFlood, _noFwd, _noPacketIn, _noRecv, _noRecvStp, _noStp, _portDown);

    }

    private static ActionTypeV10 buildAllActionsV10() {
        // Actions are not saved in ODL. Use actions reported by HP switches as
        // default.
        int input = 0x000007ff;
        final Boolean OUTPUT = (input & (1 << 0)) != 0;
        final Boolean SET_VLAN_VID = (input & (1 << 1)) != 0;
        final Boolean SET_VLAN_PCP = (input & (1 << 2)) != 0;
        final Boolean STRIP_VLAN = (input & (1 << 3)) != 0;
        final Boolean SET_DL_SRC = (input & (1 << 4)) != 0;
        final Boolean SET_DL_DST = (input & (1 << 5)) != 0;
        final Boolean SET_NW_SRC = (input & (1 << 6)) != 0;
        final Boolean SET_NW_DST = (input & (1 << 7)) != 0;
        final Boolean SET_NW_TOS = (input & (1 << 8)) != 0;
        final Boolean SET_TP_SRC = (input & (1 << 9)) != 0;
        final Boolean SET_TP_DST = (input & (1 << 10)) != 0;
        final Boolean ENQUEUE = (input & (1 << 11)) != 0;
        final Boolean VENDOR = (input & (1 << 12)) != 0;
        return new ActionTypeV10(ENQUEUE, OUTPUT, SET_DL_DST, SET_DL_SRC, SET_NW_DST, SET_NW_SRC, SET_NW_TOS,
                SET_TP_DST, SET_TP_SRC, SET_VLAN_PCP, SET_VLAN_VID, STRIP_VLAN, VENDOR);
    }

    private static CapabilitiesV10 buildCapabilitiesV10(final List<Class<? extends FeatureCapability>> capabilities) {
        boolean isOFPCARPMATCHIP = false, isOFPCFLOWSTATS = false, isOFPCIPREASM = false, isOFPCPORTSTATS = false, isOFPCQUEUESTATS = false, isOFPCRESERVED = false, isOFPCSTP = false, isOFPCTABLESTATS = false;
        for (Object capability : capabilities) {
            if (capability.equals(FlowFeatureCapabilityArpMatchIp.class)) {
                isOFPCARPMATCHIP = true;
            } else if (capability.equals(FlowFeatureCapabilityFlowStats.class)) {
                isOFPCFLOWSTATS = true;
            } else if (capability.equals(FlowFeatureCapabilityIpReasm.class)) {
                isOFPCIPREASM = true;
            } else if (capability.equals(FlowFeatureCapabilityPortStats.class)) {
                isOFPCPORTSTATS = true;
            } else if (capability.equals(FlowFeatureCapabilityQueueStats.class)) {
                isOFPCQUEUESTATS = true;
            } else if (capability.equals(FlowFeatureCapabilityReserved.class)) {
                isOFPCRESERVED = true;
            } else if (capability.equals(FlowFeatureCapabilityStp.class)) {
                isOFPCSTP = true;
            } else if (capability.equals(FlowFeatureCapabilityTableStats.class)) {
                isOFPCTABLESTATS = true;
            }
        }
        return new CapabilitiesV10(isOFPCARPMATCHIP, isOFPCFLOWSTATS, isOFPCIPREASM, isOFPCPORTSTATS, isOFPCQUEUESTATS,
                isOFPCRESERVED, isOFPCSTP, isOFPCTABLESTATS);
    }
}
