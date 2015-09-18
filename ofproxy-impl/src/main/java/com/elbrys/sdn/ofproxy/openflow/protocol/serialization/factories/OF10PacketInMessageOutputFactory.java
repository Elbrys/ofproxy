/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;

public final class OF10PacketInMessageOutputFactory implements OFSerializer<PacketInMessage>{
    private static final byte MESSAGE_TYPE = 10;
    private static final byte PADDING_IN_PACKET_IN = 1;

    @Override
    public void serialize(final PacketInMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getBufferId().intValue());
        int totalLenIndex = outBuffer.readableBytes();
        outBuffer.writeShort(0);  // Replace it with real length
        outBuffer.writeShort(message.getInPort());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING_IN_PACKET_IN);
        outBuffer.writeBytes(message.getData());
        outBuffer.setShort(totalLenIndex, message.getData().length);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
}
