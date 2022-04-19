package org.zim.client.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.zim.client.common.ClientHandler;
import org.zim.protocol.netty.NettyDecoder;
import org.zim.protocol.netty.NettyEncoder;

public class NettyChannelInit extends ChannelInitializer<NioSocketChannel> {

    private final ClientHandler clientHandler;

    public NettyChannelInit(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new NettyDecoder())
                .addLast(new NettyEncoder())
                .addLast(new NettyClientHandler(clientHandler));
    }
}
