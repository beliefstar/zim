package org.zim.reactor.api.channel.pipeline;

import org.zim.reactor.api.channel.ZimChannel;

public interface ZimChannelPipelineContext {

    void fireRead(Object msg);

    void fireWrite(Object msg);

    void close();

    void fireRegister();

    void fireActive();

    void fireExceptionCaught(Throwable e);

    void write(Object msg);

    ZimChannel channel();

    ZimChannelPipeline pipeline();
}
