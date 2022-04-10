package org.zim.server.version1.server;


import org.zim.common.EchoHelper;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.server.common.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
    private final ServerSocketChannel ssc;

    private Thread mainSelectorThread;
    private final ZimServerHandler serverHandler = new ZimServerHandler();

    public ZimServer() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("127.0.0.1", Constants.SERVER_PORT));
        ssc.configureBlocking(false);

        mainSelector = Selector.open();
        ssc.register(mainSelector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        mainSelectorThread = new Thread(() -> {
            try {
                this.mainSelectLoop();
            } catch (Exception e) {
                EchoHelper.print("zim server: main select loop error!!!");
                e.printStackTrace();
            }
        });

        mainSelectorThread.start();
    }

    private void mainSelectLoop() throws Exception {
        EchoHelper.print("zim server: waiting accept...");
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
                        EchoHelper.print("zim server: [main select] accept: {}", socketChannel.getRemoteAddress().toString());
                        socketChannel.configureBlocking(false);
                        SelectionKey selectionKey = socketChannel.register(mainSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        ZimChannelImpl zimChannel = new ZimChannelImpl(socketChannel);
                        zimChannel.registerListener(new ZimChannelListener() {
                            @Override
                            public void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {
                                serverHandler.handleRead(buffer, channel);
                            }
                        });
                        selectionKey.attach(zimChannel);
                    }
                }
                if (key.isValid() && key.isReadable()) {
                    ZimChannel zimChannel = (ZimChannel) key.attachment();
                    try {
                        zimChannel.read();
                    } catch (UnCompleteException ignore) {
                    } catch (Exception e) {
                        key.cancel();
                        zimChannel.close();
                    }
                }
                if (key.isValid() && key.isWritable()) {
                    ZimChannel zimChannel = (ZimChannel) key.attachment();
                    zimChannel.writeRemaining();
                }
                iterator.remove();
            }
        }
    }
}
