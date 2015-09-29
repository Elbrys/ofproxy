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

package com.elbrys.sdn.ofproxy.openflow;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpenflowMgr;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientInitializer;
import com.elbrys.sdn.ofproxy.openflow.queues.InboundMsgQueue;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Class responsible for managinf connection to third party controller
 * 
 * @author igork
 * 
 */
public final class Client extends ClientNode implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private ClientConfig cfg;
    private EventLoopGroup group;
    private ClientInitializer clientInitializer;
    private Channel channel;
    private OpenflowMgr ofMgr;
    private int xid;

    public Client(final InstanceIdentifier<Node> nodePath, final ClientConfig config,
            final InboundMsgQueue inboundQueue, OpenflowMgr openflowMgr) {
        super(nodePath);
        this.cfg = config;
        clientInitializer = new ClientInitializer(this, inboundQueue);
        ofMgr = openflowMgr;
        xid = 1;
    }

    /**
     * Return next XId to be used in OF messages
     * 
     * @return Xid
     */
    public long getXid() {
        return xid++;
    }

    @Override
    public void run() {
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(clientInitializer);

            ChannelFuture cf = b.connect(cfg.getHost(), cfg.getPort()).sync();

            // Add a completion listener
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        LOG.debug("Client connected to [{}:{}]", cfg.getHost(), cfg.getPort());
                    } else {
                        if (future.isCancelled()) {
                            LOG.debug("Connection request cancelled");
                        } else {
                            LOG.debug("Unable to connect. Success: " + future.isSuccess() + "  Done: "
                                    + future.isDone() + "  Cause: " + future.cause());
                        }
                        throw new RuntimeException("Unable to connect to " + cfg.getHost() + ":" + cfg.getPort());
                    }
                }
            });

            if (!cf.awaitUninterruptibly(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout connecting to " + cfg.getHost() + ":" + cfg.getPort());
            }

            if (!cf.isDone()) {
                LOG.warn("Failed to connect to [{}:{}]", cfg.getHost(), cfg.getPort());
                LOG.warn("  * reason: {}", cf.cause());
                throw new RuntimeException("Could not connect to " + cfg.getHost() + ":" + cfg.getPort(), cf.cause());
            }

            channel = cf.channel();
            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            LOG.debug("shutting down");
            close();
        }
    }

    /**
     * Closes connection to controller
     */
    public void close() {
        try {
            channel.close().awaitUninterruptibly();
            group.shutdownGracefully().get();
            LOG.debug("shutdown succesful");
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Return connection configuration
     * 
     * @return connection configuration
     */
    public ClientConfig getConfig() {
        return cfg;
    }

    /**
     * Sends raw message to connected OF controller
     * 
     * @param out
     *            Raw message
     */
    public void send(final ByteBuf out) {
        ChannelHandlerContext ctx = clientInitializer.getChannelCtx();
        if (ctx != null) {
            ctx.writeAndFlush(out);
        }
    }

    /**
     * Return true if connection to OF controller is established.
     * 
     * @return
     */
    public SettableFuture<Boolean> getIsOnlineFuture() {
        return clientInitializer.getIsOnlineFuture();
    }

    /**
     * Returns connection context
     * 
     * @return Connection context
     */
    public ChannelHandlerContext getCtxt() {
        return clientInitializer.getChannelCtx();
    }

    /**
     * Consumes OF message
     * 
     * @param ofMsg
     *            OF message
     */
    public void consume(DataObject ofMsg) {
        ofMgr.consume(this, ofMsg);
    }

    /**
     * Queue OF message to be send to OF controller
     * 
     * @param msg
     *            OF message
     */
    public void send(DataObject msg) {
        ofMgr.send(this, msg);
    }

    /**
     * Sends ODL PacketOut message
     * 
     * @param packet
     *            ODL packetOut message
     */
    public void transmitPacket(TransmitPacketInput packet) {
        ofMgr.transmitPacket(packet);
    }

    /**
     * Sends ODL FlowMNod message
     * 
     * @param flow
     *            Flow to be set
     */
    public void addFlow(AddFlowInput flow) {
        ofMgr.addFlow(flow);
    }

    /**
     * Return third party controller connection key
     * 
     * @return connection key
     */
    public String getKey() {
        return cfg.getKey();
    }
}
