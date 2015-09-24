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
