package org.zim.common.channel;

import org.zim.common.channel.pipeline.ZimChannelPipeline;
import org.zim.common.reactor.EventLoop;

import java.net.SocketAddress;

public interface ZimChannel {

    void register(EventLoop eventLoop, ZimChannelFuture regFuture);

    void write(Object msg);

    void close();

    ZimChannelFuture closeFuture();

    ZimChannelPipeline pipeline();

    EventLoop eventLoop();

    Unsafe unsafe();

    SocketAddress localAddress();

    SocketAddress remoteAddress();

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
