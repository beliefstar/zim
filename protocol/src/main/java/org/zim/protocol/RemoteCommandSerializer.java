package org.zim.protocol;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;

import java.nio.ByteBuffer;

@Slf4j
public class RemoteCommandSerializer implements ZimChannelHandler {

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) {
        if (msg instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) msg;
            byte[] bytes = buffer.array();
            RemoteCommand command = RemoteCommand.decode(bytes);
            ctx.fireRead(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void handleWrite(ZimChannelPipelineContext ctx, Object msg) {
        if (msg instanceof RemoteCommand) {
            RemoteCommand command = (RemoteCommand) msg;
            ctx.fireWrite(ByteBuffer.wrap(command.encode()));
        } else {
            log.error("encode type: {}", msg.getClass());
            throw new IllegalArgumentException();
        }
    }
}
