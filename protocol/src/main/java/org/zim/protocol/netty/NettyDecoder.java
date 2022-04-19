package org.zim.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.zim.protocol.RemoteCommand;

import java.nio.ByteBuffer;

public class NettyDecoder extends LengthFieldBasedFrameDecoder {

    public NettyDecoder() {
        super(8092, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
        if (byteBuf == null) {
            return null;
        }
        ByteBuffer buffer = byteBuf.nioBuffer();
        try {
            return RemoteCommand.decode(buffer);
        } finally {
            byteBuf.release();
        }
    }
}
