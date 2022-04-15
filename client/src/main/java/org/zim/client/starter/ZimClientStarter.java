package org.zim.client.starter;

import org.zim.client.common.ChannelInit;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.ReconnectHelper;
import org.zim.common.bootstrap.ZimBootstrap;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.impl.ZimNioChannel;
import org.zim.common.reactor.ReactorEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ZimClientStarter {

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
        ClientHandler clientHandler = new ClientHandler();

        // reactor
//        new Reactor("127.0.0.1", 7436, new ChannelInit(clientHandler, true)).start();

        // 事件循环
        ReactorEventLoopGroup workGroup = new ReactorEventLoopGroup(1, threadFactory);

        ZimBootstrap bootstrap = new ZimBootstrap();
        bootstrap.group(workGroup)
                .channel(ZimNioChannel.class)
                .handler(new ChannelInit(clientHandler, true));

        ZimChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 7436)).sync();
        if (future.isSuccess()) {
            System.out.println("bootstrap success");
        }
        future.channel().closeFuture().addListener(f -> {
            ReconnectHelper.handleReconnect(() -> {
                ZimChannelFuture sync = bootstrap.connect(new InetSocketAddress("127.0.0.1", 7436)).sync();
                if (!sync.isSuccess()) {
                    throw new RuntimeException();
                }
            });
        });

        clientHandler.listenScan();
    }
}
