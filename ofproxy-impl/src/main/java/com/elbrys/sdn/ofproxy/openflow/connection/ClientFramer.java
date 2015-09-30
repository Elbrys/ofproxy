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

package com.elbrys.sdn.ofproxy.openflow.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decoder that splits the received ChannelBuffers dynamically by the value of
 * the length field in the message
 * 
 * @author Igor Kondrakhin
 * 
 */
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
    protected Object decode(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {

        // THIS IS IMPORTANT!!!!!
        ByteBuf bb = (ByteBuf) super.decode(ctx, buf);
        if (bb == null) {
            return null;
        }

        if (bb.readableBytes() < LENGTH_OF_HEADER) {
            LOGGER.debug("skipping bb - too few data for header: " + bb.readableBytes());
            LOGGER.debug("decode " + ByteBufUtils.byteBufToHexString(bb));
            bb.skipBytes(bb.readableBytes());
            return null;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        if (bb.readableBytes() < length) {
            LOGGER.debug("skipping bb - too few data for msg: " + bb.readableBytes() + " < " + length);
            LOGGER.debug("Too short. decode Fail " + ByteBufUtils.byteBufToHexString(bb));
            bb.skipBytes(bb.readableBytes());
            return null;
        }

        ByteBuf messageBuffer = bb.slice(bb.readerIndex(), length);
        messageBuffer.retain();
        bb.skipBytes(length);

        return messageBuffer;
    }

}
