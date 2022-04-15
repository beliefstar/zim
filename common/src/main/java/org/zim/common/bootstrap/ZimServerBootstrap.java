package org.zim.common.bootstrap;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.ZimChannelInitializer;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;
import org.zim.common.reactor.ReactorEventLoopGroup;

import java.net.SocketAddress;

public class ZimServerBootstrap {

    private ReactorEventLoopGroup group;
    private ReactorEventLoopGroup childGroup;

    private Class<? extends ZimChannel> channelClass;

    private ZimChannelHandler childHandler;

    private volatile SocketAddress localAddress;

    public ZimServerBootstrap() {
    }

    public ZimServerBootstrap group(ReactorEventLoopGroup group) {
        return group(group, group);
    }

    public ZimServerBootstrap group(ReactorEventLoopGroup group, ReactorEventLoopGroup childGroup) {
        this.group = group;
        this.childGroup = childGroup;
        return this;
    }

    public ZimServerBootstrap channel(Class<? extends ZimChannel> cls) {
        this.channelClass = cls;
        return this;
    }

    public ZimServerBootstrap childHandler(ZimChannelHandler channelHandler) {
        this.childHandler = channelHandler;
        return this;
    }

    public ZimServerBootstrap validate() {
        if (group == null) {
            throw new IllegalStateException("group not set");
        }
        if (channelClass == null) {
            throw new IllegalStateException("channel not set");
        }
        if (childHandler == null) {
            throw new IllegalStateException("childHandler not set");
        }
        if (childGroup == null) {
            throw new IllegalStateException("childGroup is not set");
        }
        return this;
    }

    public ZimServerBootstrap localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public ZimChannelFuture bind() {
        validate();
        if (this.localAddress == null) {
            throw new IllegalArgumentException("socket address not set");
        }
        return doBind(this.localAddress);
    }

    public ZimChannelFuture bind(SocketAddress socketAddress) {
        validate();
        if (socketAddress == null) {
            throw new IllegalArgumentException("socket address not set");
        }
        return doBind(socketAddress);
    }

    private ZimChannelFuture doBind(SocketAddress socketAddress) {
        ZimChannel channel;
        try {
            channel = channelClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("generate class instance fail! class: " + channelClass.getName());
        }

        channel.pipeline().addLast(new ZimChannelInitializer() {
            @Override
            public void init(ZimChannel ch) {
                ch.pipeline().addLast(new ServerChannelAcceptor(childGroup, childHandler));
            }
        });

        ZimChannelFuture regFuture = group.register(channel);

        ZimChannelFuture bindFuture = new ZimChannelFuture(channel);
        regFuture.addListener(future -> {
            if (future.isSuccess()) {
                doBind0(channel, socketAddress, bindFuture);
            } else {
                bindFuture.failure();
            }
        });

        return bindFuture;
    }

    private void doBind0(ZimChannel channel, SocketAddress socketAddress, ZimChannelFuture bindFuture) {
        channel.eventLoop().execute(() -> {
            ZimChannel.Unsafe unsafe = channel.unsafe();
            unsafe.bind(socketAddress, bindFuture);
        });
    }

    private static class ServerChannelAcceptor implements ZimChannelHandler {

        private final ReactorEventLoopGroup childGroup;
        private final ZimChannelHandler childHandler;

        private ServerChannelAcceptor(ReactorEventLoopGroup childGroup, ZimChannelHandler childHandler) {
            this.childGroup = childGroup;
            this.childHandler = childHandler;
        }

        @Override
        public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
            ZimChannel channel = (ZimChannel) msg;

            channel.pipeline().addLast(childHandler);

            childGroup.register(channel).addListener(future -> {
                if (!future.isSuccess()) {
                    channel.unsafe().close();
                }
            });
        }
    }
}
