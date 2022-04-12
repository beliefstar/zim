package org.zim.client.nio.single;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.ReconnectHelper;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.impl.ZimChannelImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


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

    private ClientHandler clientHandler;

    private volatile boolean reconnect = false;

    public Reactor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        selector = Selector.open();
        ZimChannel channel = connect();

        clientHandler = new ClientHandler(channel);

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

        clientHandler.listenScan();
    }

    private ZimChannel connect() throws IOException {
        SocketChannel sc = SocketChannel.open();
        boolean b = sc.connect(new InetSocketAddress(host, port));
        sc.configureBlocking(false);
        EchoHelper.printSystemError("connect: " + b);
        ZimChannel channel = new ZimChannelImpl(sc);

        reconnect = true;
        selector.wakeup();
        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        reconnect = false;

        return channel;
    }

    private void select() throws IOException {
        outer:
        while (true) {
            while (!reconnect) {
                int select = selector.select(5000);
                if (select <= 0) {
                    continue outer;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    try {
                        dispatch(key);

                    } catch (IOException e) {
                        EchoHelper.printSystemError("lost connection");
                        key.cancel();
                        clientHandler.getChannel().close();
                        ReconnectHelper.handleReconnect(() -> {
                            ZimChannel channel = connect();
                            clientHandler.resetChannel(channel);
                        });
                    }

                    iterator.remove();
                }
            }
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        if (key.isValid() && key.isReadable()) {
            clientHandler.getChannel().read();
        }
        if (key.isValid() && key.isWritable()) {
            clientHandler.getChannel().writeRemaining();
        }
    }
}
