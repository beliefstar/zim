package org.zim.common.bootstrap;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.reactor.ReactorEventLoopGroup;

import java.net.SocketAddress;

public class ZimBootstrap {

    private ReactorEventLoopGroup group;

    private Class<? extends ZimChannel> channelClass;

    private ZimChannelHandler handler;

    public ZimBootstrap() {
    }

    public ZimBootstrap group(ReactorEventLoopGroup group) {
        this.group = group;
        return this;
    }


    public ZimBootstrap channel(Class<? extends ZimChannel> cls) {
        this.channelClass = cls;
        return this;
    }

    public ZimBootstrap handler(ZimChannelHandler handler) {
        this.handler = handler;
        return this;
    }

    public ZimBootstrap validate() {
        if (group == null) {
            throw new IllegalStateException("group not set");
        }
        if (channelClass == null) {
            throw new IllegalStateException("channel not set");
        }
        if (handler == null) {
            throw new IllegalStateException("childHandler not set");
        }
        return this;
    }

    public ZimChannelFuture connect(SocketAddress socketAddress) {
        validate();
        if (socketAddress == null) {
            throw new IllegalStateException("address is null");
        }
        return doConnect(socketAddress);
    }

    private ZimChannelFuture doConnect(SocketAddress socketAddress) {
        ZimChannel channel;
        try {
            channel = channelClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("generate class instance fail! class: " + channelClass.getName());
        }

        channel.pipeline().addLast(handler);

        ZimChannelFuture regFuture = group.register(channel);

        ZimChannelFuture connectFuture = new ZimChannelFuture(channel);
        regFuture.addListener(future -> {
            if (future.isSuccess()) {
                doConnect0(channel, socketAddress, connectFuture);
            } else {
                connectFuture.failure();
            }
        });

        return connectFuture;
    }

    private void doConnect0(ZimChannel channel, SocketAddress socketAddress, ZimChannelFuture connFuture) {
        channel.eventLoop().execute(() -> {
            ZimChannel.Unsafe unsafe = channel.unsafe();
            unsafe.connect(socketAddress, connFuture);
        });
    }

    public void close() {
        group.close();
    }
}
