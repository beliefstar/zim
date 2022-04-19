package org.zim.server.starter;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.zim.common.EchoHelper;
import org.zim.common.bootstrap.ZimServerBootstrap;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.impl.ZimNioServerChannel;
import org.zim.common.reactor.ReactorEventLoopGroup;
import org.zim.server.common.ChannelInit;
import org.zim.server.netty.NettyChannelInit;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动器
 */
public class ZimServerStarter {

    private static ThreadFactory threadFactory = new ThreadFactory() {
        final AtomicInteger count = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("zim-server-nio-exec-" + count.incrementAndGet());
            return t;
        }
    };

    public static void main(String[] args) throws Exception {
        System.out.println("welcome to zim!\nversion: 1");

        // 单 Reactor
//        new Reactor("127.0.0.1", 7436, new ChannelInit(true)).start();

        // 主从 Reactor
//        new MasterReactor("127.0.0.1", 7436, 5, new ChannelInit(true), threadFactory).start();

        // 主从事件循环
//        startWithEventLoop();

        // Netty 版本
        startWithNetty();
    }

    public static void startWithEventLoop() {
        ReactorEventLoopGroup bossGroup = new ReactorEventLoopGroup(1, threadFactory);
        ReactorEventLoopGroup workGroup = new ReactorEventLoopGroup(3, threadFactory);

        ZimServerBootstrap serverBootstrap = new ZimServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(ZimNioServerChannel.class)
                .localAddress(new InetSocketAddress("127.0.0.1", 7436))
                .childHandler(new ChannelInit(true));

        try {
            ZimChannelFuture future = serverBootstrap.bind().sync();
            if (future.isSuccess()) {
                EchoHelper.print("zim server: waiting accept...");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startWithNetty() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, threadFactory);
        EventLoopGroup workGroup = new NioEventLoopGroup(3, threadFactory);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .localAddress(new InetSocketAddress("127.0.0.1", 7436))
                .childHandler(new NettyChannelInit(true));

        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            EchoHelper.print("zim server: waiting accept...");
        }
    }
}
