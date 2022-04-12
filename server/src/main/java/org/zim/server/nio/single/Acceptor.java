package org.zim.server.nio.single;

import org.zim.common.ActionHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.impl.ZimChannelImpl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements ActionHandler {

    @Override
    public void action(SelectionKey key) throws IOException {
        if (key.isValid() && key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel channel = ssc.accept();
            if (channel != null) {
                EchoHelper.print("zim server: [main select] accept: {}", channel.getRemoteAddress().toString());
                ZimChannelImpl zimChannel = new ZimChannelImpl(channel);

                channel.configureBlocking(false);
                channel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Handler(zimChannel));
            }
        }
    }
}
