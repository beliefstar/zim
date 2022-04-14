package org.zim.common.channel;

import org.zim.common.channel.pipeline.ZimChannelPipeline;

import java.nio.channels.Selector;

public interface ZimChannel {

    void register(Selector selector);

    void write(Object msg);

    void close();

    CloseFuture closeFuture();

    ZimChannelPipeline pipeline();

    Unsafe unsafe();

    interface Unsafe {
        void read();
        void write(Object msg);
        void flush();
        void close();
    }
}
