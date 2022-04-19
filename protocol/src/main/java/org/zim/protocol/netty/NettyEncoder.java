package org.zim.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.zim.protocol.RemoteCommand;

public class NettyEncoder extends MessageToByteEncoder<RemoteCommand> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RemoteCommand msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.encode());
    }
}
