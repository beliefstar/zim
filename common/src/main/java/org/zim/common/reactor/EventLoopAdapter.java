package org.zim.common.reactor;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;

public abstract class EventLoopAdapter implements EventLoop {

    @Override
    public ZimChannelFuture register(ZimChannel channel) {
        return null;
    }

    @Override
    public boolean inEventLoop() {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public void close() {

    }
}
