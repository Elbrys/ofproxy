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