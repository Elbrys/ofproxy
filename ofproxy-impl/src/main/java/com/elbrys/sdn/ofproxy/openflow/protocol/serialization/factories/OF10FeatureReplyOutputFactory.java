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

package com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;

public final class OF10FeatureReplyOutputFactory implements OFSerializer<GetFeaturesOutput> {
    private static final byte MESSAGE_TYPE = 6;
    private static final byte PADDING_IN_FEATURES_REPLY_HEADER = 3;

    @Override
    public void serialize(final GetFeaturesOutput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getDatapathId().longValue());
        outBuffer.writeInt(message.getBuffers().intValue());
        outBuffer.writeByte(message.getTables().byteValue());
        outBuffer.writeZero(PADDING_IN_FEATURES_REPLY_HEADER);
        outBuffer.writeInt(createCapabilitiesV10(message.getCapabilitiesV10()));
        outBuffer.writeInt(createActionsV10(message.getActionsV10()));
        for (PhyPort port : message.getPhyPort()) {
            serializePort(port, outBuffer);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static void serializePort(final PhyPort port, final ByteBuf outBuffer) {
        short portN = 0;
        if (port.getPortNo() != null) {
            portN = port.getPortNo().shortValue();
        }
        outBuffer.writeShort(portN);

        String macAddress = port.getHwAddr().getValue();
        outBuffer.writeBytes(ByteBufUtils.macAddressToBytes(macAddress));

        // Name should be saved as 16 bit null terminated string
        String name = "";
        if (port.getName() != null) {
            name = port.getName().substring(0, Math.min(16, port.getName().length()));
        }
        outBuffer.writeBytes(name.getBytes());
        outBuffer.writeZero(16 - name.getBytes().length);

        outBuffer.writeInt(createConfigV10(port.getConfigV10()));
        outBuffer.writeInt(createStateV10(port.getStateV10()));
        outBuffer.writeInt(createPortFeaturesV10(port.getCurrentFeaturesV10()));
        outBuffer.writeInt(createPortFeaturesV10(port.getAdvertisedFeaturesV10()));
        outBuffer.writeInt(createPortFeaturesV10(port.getSupportedFeaturesV10()));
        outBuffer.writeInt(createPortFeaturesV10(port.getPeerFeaturesV10()));
    }

    private static int createPortFeaturesV10(final PortFeaturesV10 input) {
        return ByteBufUtils.fillBitMask(0, input.is_10mbHd(), input.is_10mbFd(), input.is_100mbHd(),
                input.is_100mbFd(), input.is_1gbHd(), input.is_1gbFd(), input.is_10gbFd(), input.isCopper(),
                input.isFiber(), input.isAutoneg(), input.isPause(), input.isPauseAsym());
    }

    private static int createStateV10(final PortStateV10 input) {

        return ByteBufUtils.fillBitMask(0, input.isLinkDown(), false, false, false, false, false, false, false,
                input.isStpBlock() || input.isStpBlock(), // 1<<8
                input.isStpForward(), // 2<<8
                input.isStpBlock() || input.isStpMask() // 3<<8
        );
    }

    private static int createConfigV10(final PortConfigV10 input) {
        return ByteBufUtils.fillBitMask(0, input.isPortDown(), input.isNoStp(), input.isNoRecv(), input.isNoRecvStp(),
                input.isNoFlood(), input.isNoFwd(), input.isNoPacketIn());
    }

    private static int createCapabilitiesV10(final CapabilitiesV10 input) {
        return ByteBufUtils.fillBitMask(0, input.isOFPCFLOWSTATS(), input.isOFPCTABLESTATS(), input.isOFPCPORTSTATS(),
                input.isOFPCSTP(), input.isOFPCRESERVED(), input.isOFPCIPREASM(), input.isOFPCQUEUESTATS(),
                input.isOFPCARPMATCHIP());
    }

    private static int createActionsV10(final ActionTypeV10 input) {
        return ByteBufUtils.fillBitMask(0, input.isOFPATOUTPUT(), input.isOFPATSETVLANVID(), input.isOFPATSETVLANPCP(),
                input.isOFPATSTRIPVLAN(), input.isOFPATSETDLSRC(), input.isOFPATSETDLDST(), input.isOFPATSETNWSRC(),
                input.isOFPATSETNWDST(), input.isOFPATSETNWTOS(), input.isOFPATSETTPSRC(), input.isOFPATSETTPDST(),
                input.isOFPATENQUEUE(), input.isOFPATVENDOR());
    }
}
