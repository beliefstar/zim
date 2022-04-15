package org.zim.client.nio.single;

import org.zim.client.common.ReconnectHelper;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.impl.ZimNioChannel;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.reactor.EventLoopAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 *
 * 单线程 Reactor
 *
 */
public class Reactor {

    private Selector selector;
    private volatile Thread selectorThread;

    private final String host;
    private final int port;

    private final ZimChannelHandler channelHandler;

    private final Queue<ZimChannel> registerQueue = new ConcurrentLinkedQueue<>();

    public Reactor(String host, int port, ZimChannelHandler channelHandler) {
        this.host = host;
        this.port = port;
        this.channelHandler = channelHandler;
    }

    public void start() throws Exception {
        selector = Selector.open();
        connect();

        if (selectorThread == null) {
            Thread t = new Thread(() -> {
                try {
                    select();
                } catch (IOException e) {
                    System.out.println("select done");
                    e.printStackTrace();
                    selectorThread = null;
                }
            });
            t.start();
            selectorThread = t;
        }
    }

    private ZimChannel connect() throws IOException {
        SocketChannel sc = SocketChannel.open();
        boolean b = sc.connect(new InetSocketAddress(host, port));
        sc.configureBlocking(false);
        EchoHelper.printSystemError("connect: " + b);
        ZimChannel channel = new ZimNioChannel(sc);
        channel.pipeline().addLast(channelHandler);

        channel.closeFuture().addListener(future -> ReconnectHelper.handleReconnect(this::connect));

        registerQueue.offer(channel);
        selector.wakeup();

        return channel;
    }

    private void select() throws IOException {
        while (true) {
            int select = selector.select(5000);
            if (select > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    dispatch(key);

                    iterator.remove();
                }
            }

            runRegisterTask();
        }
    }

    private void dispatch(SelectionKey key) {
        ZimChannel channel = (ZimChannel) key.attachment();
        ZimChannel.Unsafe unsafe = channel.unsafe();
        if (key.isValid() && key.isReadable()) {
            unsafe.read();
        }
        if (key.isValid() && key.isWritable()) {
            unsafe.flush();
        }
    }

    private void runRegisterTask() {
        while (!registerQueue.isEmpty()) {
            ZimChannel channel = registerQueue.poll();

            ZimChannelFuture future = new ZimChannelFuture(channel);

            channel.register(new EventLoopAdapter() {
                @Override
                public Selector selector() {
                    return selector;
                }
            }, future);
        }
    }
}
