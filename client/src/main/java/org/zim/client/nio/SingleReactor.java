package org.zim.client.nio;

import org.zim.client.common.ClientHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.impl.ZimChannelImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleReactor {

    private Selector selector;
    private volatile Thread selectorThread;

    private final String host;
    private final int port;

    private ScheduledExecutorService scheduledExecutorService;

    private ClientHandler clientHandler;

    private volatile boolean reconnect = false;

    public SingleReactor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

        selector = Selector.open();
        ZimChannel channel = connect();

        clientHandler = new ClientHandler(channel);

        if (selectorThread == null) {
            Thread t = new Thread(() -> {
                try {
                    doSelect();
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

    private void doSelect() throws IOException {
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
                    if (key.isValid() && key.isReadable()) {
                        try {
                            clientHandler.getChannel().read();
                        } catch (UnCompleteException ignore) {
                        } catch (Exception e) {
                            EchoHelper.printSystemError("lost connection");
                            key.cancel();
                            clientHandler.getChannel().close();
                            reconnect(1);
                        }
                    }
                    if (key.isValid() && key.isWritable()) {
                        clientHandler.getChannel().writeRemaining();
                    }
                    iterator.remove();
                }
            }
        }
    }

    private void reconnect(int time) {
        EchoHelper.printSystemError("reconnect..." + time);
        try {
            doReconnect();
            return;
        } catch (Exception ignore) {
        }
        scheduledExecutorService.schedule(() -> reconnect(time + 1), 10, TimeUnit.SECONDS);
    }

    private void doReconnect() throws Exception {
        ZimChannel channel = connect();
        clientHandler.resetChannel(channel);
    }
}
