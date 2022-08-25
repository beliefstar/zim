package org.zim.reactor.eventloop;

import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;
import org.zim.reactor.api.eventloop.EventLoop;

import java.util.concurrent.ThreadFactory;

public class ReactorEventLoopGroup {

    private final EventLoop[] eventLoops;
    private int eventLoopIndex = -1;

    public ReactorEventLoopGroup(int size, ThreadFactory threadFactory) {
//        Assert.check(size > 0);

        eventLoops = new EventLoop[size];

        for (int i = 0; i < eventLoops.length; i++) {
            eventLoops[i] = new ReactorEventLoop(threadFactory);
        }
    }

    public EventLoop next() {
        eventLoopIndex = (eventLoopIndex + 1) % eventLoops.length;
        return eventLoops[eventLoopIndex];
    }

    public ZimChannelFuture register(ZimChannel channel) {
        return next().register(channel);
    }

    public void close() {
        for (EventLoop loop : eventLoops) {
            loop.close();
        }
    }
}
