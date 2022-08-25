package org.zim.reactor.api.channel.pipeline;

import org.zim.reactor.api.channel.ZimChannel;

public interface ZimChannelPipelineContext {

    void fireRead(Object command);

    void fireWrite(Object command);

    void close();

    void fireRegister();

    void fireActive();

    void fireExceptionCaught(Throwable e);

    void write(Object msg);

    ZimChannel channel();

    ZimChannelPipeline pipeline();
}
