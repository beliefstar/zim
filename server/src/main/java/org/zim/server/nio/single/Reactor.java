package org.zim.server.nio.single;


import org.zim.common.ActionHandler;
import org.zim.common.EchoHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class Reactor {

    private final String host;
    private final int port;

    private Selector mainSelector;
    private ServerSocketChannel ssc;

    private final boolean useParallel;

    public Reactor(String host, int port) {
        this(host, port, false);
    }

    public Reactor(String host, int port, boolean useParallel) {
        this.host = host;
        this.port = port;
        this.useParallel = useParallel;
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(host, port));
        ssc.configureBlocking(false);

        mainSelector = Selector.open();
        ssc.register(mainSelector, SelectionKey.OP_ACCEPT, new Acceptor(useParallel));

        this.ssc = ssc;

        this.select();
    }

    private void select() throws IOException {
        EchoHelper.print("zim server: waiting accept...");
        while (true) {
            int select = mainSelector.select(5000);
            if (select == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = mainSelector.selectedKeys().iterator();
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
