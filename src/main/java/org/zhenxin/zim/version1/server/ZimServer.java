package org.zhenxin.zim.version1.server;

import org.zhenxin.zim.common.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 15:23
 */
public class ZimServer {

    private final Selector mainSelector;
    private final Selector childSelector;
    private final ServerSocketChannel ssc;

    private Thread mainSelectorThread;
    private ZimServerHandler serverHandler = new ZimServerHandler();

    public ZimServer() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("127.0.0.1", Constants.SERVER_PORT));
        ssc.configureBlocking(false);

        mainSelector = Selector.open();
        childSelector = Selector.open();
        ssc.register(mainSelector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        mainSelectorThread = new Thread(() -> {
            try {
                this.mainSelectLoop();
            } catch (Exception e) {
                System.out.println("zim server: main select loop error!!!");
                e.printStackTrace();
            }
        });

        mainSelectorThread.start();

//        new Thread(() -> {
//            try {
//                this.childSelectLoop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    private void mainSelectLoop() throws Exception {
        System.out.println("zim server: waiting accept...");
        while (true) {
            int select = mainSelector.select(5000);
            if (select == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = mainSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = ssc.accept();
                    if (socketChannel != null) {
                        System.out.println("zim server: [main select] accept: " + socketChannel.getRemoteAddress().toString());
                        socketChannel.configureBlocking(false);
                        socketChannel.register(mainSelector, SelectionKey.OP_READ);
                    }
                }
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    try {
                        serverHandler.handleRead(channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        key.cancel();
                        channel.close();
                    }
                }
                iterator.remove();
            }
        }
    }

    private void childSelectLoop() throws Exception {
        while (true) {
            int select = childSelector.select(5000);
            if (select == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = childSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    serverHandler.handleRead(channel);
                }
                iterator.remove();
            }
        }
    }

}
