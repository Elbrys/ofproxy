/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package com.elbrys.sdn.ofproxy.openflow.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFramer extends LengthFieldBasedFrameDecoder {

    /** Length of OpenFlow 1.3 header */
    public static final byte LENGTH_OF_HEADER = 8;
    private static final byte LENGTH_INDEX_IN_HEADER = 2;
    private static final int MAX_FRAME_LENGTH = 1500;
    private static final int LENGTH_FIELD_OFFSET = 2;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int LENGTH_ADJUSTMENT = -4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFramer.class);

    public ClientFramer() {
        super(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP);
    }
  
    @Override
    protected Object decode(final ChannelHandlerContext ctx, final ByteBuf buf)
        throws Exception {

        // THIS IS IMPORTANT!!!!!
        ByteBuf bb = (ByteBuf) super.decode(ctx, buf);
        if (bb == null) {
            return null;
        }
        
        if (bb.readableBytes() < LENGTH_OF_HEADER) {
            LOGGER.debug("skipping bb - too few data for header: " + bb.readableBytes());
            LOGGER.debug("decode " + ByteBufUtils.byteBufToHexString(bb));
            // TODO should we skip invalid bytes?
            return null;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        if (bb.readableBytes() < length) {
            LOGGER.debug("skipping bb - too few data for msg: " + bb.readableBytes() + " < " + length);
            LOGGER.debug("Too short. decode Fail " + ByteBufUtils.byteBufToHexString(bb));
            // TODO should we skip invalid bytes?
            // bb.skipBytes(bb.readableBytes());
            return null;
        }

        ByteBuf messageBuffer = bb.slice(bb.readerIndex(), length);
        messageBuffer.retain();
        bb.skipBytes(length);

        return messageBuffer;
    }
  
}
