package org.zim.client.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.zim.client.common.ClientHandler;
import org.zim.common.channel.netty.NettyChannel;
import org.zim.protocol.RemoteCommand;

public class NettyClientHandler extends SimpleChannelInboundHandler<RemoteCommand> {

    private final ClientHandler clientHandler;

    public NettyClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientHandler.setChannel(new NettyChannel(ctx.channel()));
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemoteCommand msg) throws Exception {
        clientHandler.getMessageConsumer().handle(msg);
    }
}
