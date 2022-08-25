package org.zim.reactor.api.channel.pipeline;

import org.zim.reactor.api.channel.ZimChannel;

import java.util.concurrent.Executor;

public interface ZimChannelPipeline {

    void fireRegister();

    void fireActive();

    void fireRead(Object command);

    void fireWrite(Object msg);

    void fireClose();

    void fireExceptionCaught(Throwable cause);

    ZimChannelPipeline addLast(ZimChannelHandler handler);

    ZimChannelPipeline addLast(Executor executor, ZimChannelHandler handler);

    ZimChannelPipeline addLast(Executor executor, ZimChannelHandler... handlers);

    ZimChannel channel();
}
