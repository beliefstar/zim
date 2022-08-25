package org.zim.reactor.api.channel;


import org.zim.reactor.api.channel.pipeline.ZimChannelPipeline;
import org.zim.reactor.api.eventloop.EventLoop;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

public interface ZimChannel {

    void register(EventLoop eventLoop, ZimChannelFuture regFuture);

    void write(Object msg);

    ZimChannelFuture close();

    ZimChannelFuture closeFuture();

    ZimChannelPipeline pipeline();

    EventLoop eventLoop();

    Unsafe unsafe();

    SocketAddress localAddress();

    SocketAddress remoteAddress();

    SelectionKey selectionKey();

    interface Unsafe {

        SocketAddress localAddress();

        SocketAddress remoteAddress();

        void read();

        void write(Object msg);

        void flush();

        void close();

        default void connect(SocketAddress socketAddress, ZimChannelFuture future) {
            future.complete();
        }

        default void finishConnect() { }

        default void bind(SocketAddress socketAddress, ZimChannelFuture future) {
            future.complete();
        }
    }
}
