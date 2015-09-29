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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.queues.InboundMsgQueue;
import com.google.common.util.concurrent.SettableFuture;

/**
 * OF connection channel initializer
 * 
 * @author igork
 * 
 */
public final class ClientInitializer extends ChannelInitializer<NioSocketChannel> {

    private SettableFuture<Boolean> isOnlineFuture;
    private Client client;
    private ChannelHandlerContext ctx;
    private ClientHandler clientHandler;

    public ClientInitializer(final Client client, final InboundMsgQueue inboundQueue) {
        this.isOnlineFuture = SettableFuture.create();
        this.client = client;
        clientHandler = new ClientHandler(this, inboundQueue);
    }

    @Override
    public void initChannel(final NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // if (cfg.isSecured()) {
        // SSLEngine engine = ClientSslContextFactory.getClientContext()
        // .createSSLEngine();
        // engine.setUseClientMode(true);
        // pipeline.addLast("ssl", new SslHandler(engine));
        // }
        pipeline.addLast("framer", new ClientFramer());
        pipeline.addLast("handler", clientHandler);
    }

    public void setCtx(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Client getClient() {
        return client;
    }

    public ChannelHandlerContext getChannelCtx() {
        return ctx;
    }

    public SettableFuture<Boolean> getIsOnlineFuture() {
        return isOnlineFuture;
    }
}