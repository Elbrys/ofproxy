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
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.ClientMsg;
import com.elbrys.sdn.ofproxy.openflow.queues.InboundMsgQueue;

/**
 * OF packet handler
 * 
 * @author igork
 * 
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private static final int LENGTH_INDEX_IN_HEADER = 2;
    public static final byte LENGTH_OF_HEADER = 8;
    private ClientInitializer clientInitializer;
    private InboundMsgQueue msgQueue;
    private ClientHandshake clientHandshake;

    /**
     * @param isOnlineFuture
     *            future notifier of connected channel
     * @param clientInitializer
     * @param msgQueue2
     */
    public ClientHandler(final ClientInitializer clientInitializer, final InboundMsgQueue inboundQueue) {
        this.clientInitializer = clientInitializer;
        this.msgQueue = inboundQueue;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        if (bb.readableBytes() < LENGTH_OF_HEADER) {
            LOGGER.debug("skipping message - too few data for header: " + bb.readableBytes());
            return;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        if (bb.readableBytes() < length) {
            LOGGER.debug("skipping message - too few data for msg: " + bb.readableBytes() + " < " + length);
            return;
        }

        if (!msgQueue.offer(ClientMsg.create(clientInitializer.getClient(), bb))) {
            LOGGER.warn("Unable to queue inbound message. Client {}. Msg {}.", clientInitializer.getClient(), msg);
        }

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("Client is active");
        clientInitializer.getIsOnlineFuture().set(true);
        // Save ChannelHandlerContext to be used to send messages
        clientInitializer.setCtx(ctx);
        // Start handshake
        clientHandshake = new ClientHandshake(clientInitializer.getClient());
        clientHandshake.start();
    }
}
