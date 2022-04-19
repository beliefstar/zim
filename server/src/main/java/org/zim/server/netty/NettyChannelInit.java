package org.zim.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.zim.common.channel.netty.NettyChannel;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.netty.NettyDecoder;
import org.zim.protocol.netty.NettyEncoder;
import org.zim.server.common.CommandProcessor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyChannelInit extends ChannelInitializer<NioSocketChannel> {

    // 业务对象
    private static final CommandProcessor commandProcessor = new CommandProcessor();

    private EventExecutorGroup executor;

    public NettyChannelInit(boolean useParallel) {
        if (useParallel) {
            executor = new DefaultEventExecutorGroup(8, new ThreadFactory() {
                final AtomicInteger count = new AtomicInteger();
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("zim-server-nio-exec-" + count.incrementAndGet());
                    return t;
                }
            });
        }
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.pipeline().addLast(executor,
                new NettyDecoder(),
                new NettyEncoder(),
                new NettyServerHandler());
    }

    private static class NettyServerHandler extends SimpleChannelInboundHandler<RemoteCommand> {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("accept " + ctx.channel().remoteAddress());
            super.channelRegistered(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemoteCommand msg) throws Exception {
            commandProcessor.process(msg, new NettyChannel(ctx.channel()));
        }
    }
}
