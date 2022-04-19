package org.zim.client.starter;

import org.zim.client.common.ChannelInit;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.ReconnectHelper;
import org.zim.client.nio.single.Reactor;
import org.zim.common.bootstrap.ZimBootstrap;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.impl.ZimNioChannel;
import org.zim.common.reactor.ReactorEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ZimClientStarter {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        final AtomicInteger count = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("zim-client-nio-exec-" + count.incrementAndGet());
            return t;
        }
    };


    public static void main(String[] args) throws Exception {
        ClientHandler clientHandler = new ClientHandler();

        // reactor
//        startWithSingleReactor(clientHandler);

        // 事件循环
        startWithNioEventLoop(clientHandler);

        clientHandler.listenScan();
    }

    /**
     * reactor
     * @param clientHandler
     * @throws Exception
     */
    private static void startWithSingleReactor(ClientHandler clientHandler) throws Exception {
        Reactor reactor = new Reactor("127.0.0.1", 7436, new ChannelInit(clientHandler, true));
        ZimChannel channel = reactor.start();

        channel.closeFuture().addListener(future -> {
            if (clientHandler.isRunning()) {
                ReconnectHelper.handleReconnect(reactor::connect);
            } else {
                reactor.close();
            }
        });
    }

    /**
     * 事件循环
     * @param clientHandler
     */
    private static void startWithNioEventLoop(ClientHandler clientHandler) throws InterruptedException {
        ReactorEventLoopGroup workGroup = new ReactorEventLoopGroup(1, THREAD_FACTORY);

        ZimBootstrap bootstrap = new ZimBootstrap();
        bootstrap.group(workGroup)
                .channel(ZimNioChannel.class)
                .handler(new ChannelInit(clientHandler, true));

        ZimChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 7436)).sync();
        if (future.isSuccess()) {
            System.out.println("bootstrap success");
        }
        future.channel().closeFuture().addListener(f -> {
            if (clientHandler.isRunning()) {
                ReconnectHelper.handleReconnect(() -> {
                    ZimChannelFuture sync = bootstrap.connect(new InetSocketAddress("127.0.0.1", 7436)).sync();
                    if (!sync.isSuccess()) {
                        throw new RuntimeException();
                    }
                });
            } else {
                bootstrap.close();
            }
        });
    }
}
