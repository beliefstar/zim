package org.zim.reactor.api.eventloop;


import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;

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
