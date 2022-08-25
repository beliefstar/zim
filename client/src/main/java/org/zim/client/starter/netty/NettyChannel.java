package org.zim.client.starter.netty;

import io.netty.channel.Channel;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;
import org.zim.reactor.api.eventloop.EventLoop;
import org.zim.reactor.api.eventloop.EventLoopAdapter;
import org.zim.reactor.channel.DefaultZimChannelFuture;
import org.zim.reactor.channel.pipeline.DefaultZimChannelPipeline;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Objects;

public class NettyChannel implements ZimChannel {

    private final Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void register(EventLoop eventLoop, ZimChannelFuture regFuture) {

    }

    @Override
    public void write(Object msg) {
        channel.writeAndFlush(msg);
    }

    @Override
    public DefaultZimChannelFuture close() {
        return new ChannelFutureAdapter(this, channel.close());
    }

    @Override
    public DefaultZimChannelFuture closeFuture() {
        return new ChannelFutureAdapter(this, channel.closeFuture());
    }

    @Override
    public DefaultZimChannelPipeline pipeline() {
        return null;
    }

    @Override
    public EventLoop eventLoop() {
        return new EventLoopAdapter() {
            @Override
            public Selector selector() {
                return null;
            }

            @Override
            public boolean inEventLoop() {
                return channel.eventLoop().inEventLoop();
            }
        };
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }

    @Override
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public SelectionKey selectionKey() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyChannel that = (NettyChannel) o;
        return Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }
}
