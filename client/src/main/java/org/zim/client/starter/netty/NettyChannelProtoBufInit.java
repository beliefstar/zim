package org.zim.client.starter.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.zim.client.common.ClientHandler;
import org.zim.protocol.netty.ProtoBufConvertCodec;
import org.zim.protocol.protobuf.entity.RemoteCommandProtocol;

public class NettyChannelProtoBufInit extends ChannelInitializer<NioSocketChannel> {

    private final ClientHandler clientHandler;

    public NettyChannelProtoBufInit(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(RemoteCommandProtocol.getDefaultInstance()))
                //
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())

                .addLast(new ProtoBufConvertCodec())
                .addLast(new NettyClientHandler(clientHandler));
    }
}
