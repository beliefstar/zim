package org.zim.common.reactor;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;

import java.nio.channels.Selector;
import java.util.concurrent.Executor;

public interface EventLoop extends Executor {

    Selector selector();

    ZimChannelFuture register(ZimChannel channel);

    boolean inEventLoop();
}
