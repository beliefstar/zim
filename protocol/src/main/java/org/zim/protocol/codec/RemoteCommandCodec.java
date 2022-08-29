package org.zim.protocol.codec;

import lombok.extern.slf4j.Slf4j;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.serializer.SerializerFactory;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;

import java.nio.ByteBuffer;

@Slf4j
public class RemoteCommandCodec implements ZimChannelHandler {

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) msg;
            RemoteCommand command = SerializerFactory.getDefault().deserialize(buffer);
            ctx.fireRead(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void handleWrite(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        if (msg instanceof RemoteCommand) {
            RemoteCommand command = (RemoteCommand) msg;
            ctx.fireWrite(ByteBuffer.wrap(SerializerFactory.getDefault().serialize(command)));
        } else {
            log.error("encode type: {}", msg.getClass());
            throw new IllegalArgumentException();
        }
    }
}
