package org.zim.server.nio.sub;


import lombok.extern.slf4j.Slf4j;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.channel.impl.ZimNioChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;


/**
 *
 * 主从 Reactor 模型
 *
 */
@Slf4j
public class MasterReactor {

    private final Selector selector;

    private final String host;
    private final int port;

    private final SubReactor[] subReactors;
    private int subIndex = -1;

    private volatile Thread runThread;

    private final ZimChannelHandler channelHandler;
    private final ThreadFactory threadFactory;

    public MasterReactor(String host, int port, int subSize, ZimChannelHandler channelHandler, ThreadFactory threadFactory) throws IOException {
        this.host = host;
        this.port = port;
        this.threadFactory = threadFactory;
        this.selector = Selector.open();
        this.subReactors = new SubReactor[subSize];
        this.channelHandler = channelHandler;
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(this.host, this.port));
        ssc.configureBlocking(false);

        ssc.register(this.selector, SelectionKey.OP_ACCEPT);

        for (int i = 0; i < subReactors.length; i++) {
            subReactors[i] = new SubReactor(this.threadFactory);
        }

        runThread = this.threadFactory.newThread(this::select);
        runThread.start();
    }

    private void select() {
        while (!Thread.interrupted()) {
            try {
                int count = selector.select(5000);
                if (count <= 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    dispatch(key);
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("== selector done ==");
    }

    private void dispatch(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel channel = ssc.accept();
        if (channel != null) {
            log.info("zim server: [main select] accept: {}", channel.getRemoteAddress());

            channel.configureBlocking(false);
            ZimNioChannel zimChannel = new ZimNioChannel(channel);
            zimChannel.pipeline().addLast(this.channelHandler);

            chooseSubReactor().register(zimChannel);
        }
    }

    private SubReactor chooseSubReactor() {
        subIndex = (subIndex + 1) % subReactors.length;
        return subReactors[subIndex];
    }
}
