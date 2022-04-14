package org.zim.server.nio.single;


import org.zim.common.ActionHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.pipeline.ZimChannelHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;


/**
 *
 * 单 Reactor
 *
 */
public class Reactor {

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel ssc;

    private final ZimChannelHandler channelHandler;

    public Reactor(String host, int port, ZimChannelHandler channelHandler) {
        this.host = host;
        this.port = port;
        this.channelHandler = channelHandler;
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(host, port));
        ssc.configureBlocking(false);

        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, new Acceptor(channelHandler));

        this.ssc = ssc;

        this.select();
    }

    private void select() throws IOException {
        EchoHelper.print("zim server: waiting accept...");
        while (true) {
            int select = selector.select(5000);
            if (select == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                dispatch(key);

                iterator.remove();
            }
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        ActionHandler actionHandler = (ActionHandler) key.attachment();
        if (actionHandler != null) {
            try {
                actionHandler.action(key);
            } catch (IOException e) {
                key.cancel();
                key.channel().close();
            }
        }
    }
}
