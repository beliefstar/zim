package org.zim.reactor.api.eventloop;


import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;

import java.nio.channels.Selector;
import java.util.concurrent.Executor;

public interface EventLoop extends Executor {

    Selector selector();

    ZimChannelFuture register(ZimChannel channel);

    boolean inEventLoop();

    void close();
}
