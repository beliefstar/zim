package org.zim.server.nio;

import org.zim.common.EchoHelper;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.server.common.CommandProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 单线程 Reactor
 *
 */
public class SingleReactor {

    private final String host;
    private final int port;

    private Selector mainSelector;
    private ServerSocketChannel ssc;

    private final CommandProcessor commandProcessor = new CommandProcessor();

    public SingleReactor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(host, port));
        ssc.configureBlocking(false);

        mainSelector = Selector.open();
        ssc.register(mainSelector, SelectionKey.OP_ACCEPT);

        this.mainSelectLoop();
    }

    private void mainSelectLoop() throws IOException {
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
                                commandProcessor.handleRead(buffer, channel);
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
