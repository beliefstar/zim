package org.zim.server.nio.single;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.ActionHandler;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.common.channel.pipeline.ZimChannelHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class Acceptor implements ActionHandler {

    private final ZimChannelHandler channelHandler;

    public Acceptor(ZimChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    @Override
    public void action(SelectionKey key) throws IOException {
        if (key.isValid() && key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel channel = ssc.accept();
            if (channel != null) {
                log.info("zim server: [main select] accept: {}", channel.getRemoteAddress().toString());
                ZimChannel zimChannel = new ZimChannelImpl(channel);

                ActionHandler actionHandler = new Handler(zimChannel);

                channel.configureBlocking(false);

                zimChannel.pipeline().addLast(channelHandler);

                channel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, actionHandler);
                zimChannel.pipeline().fireRegister();
            }
        }
    }
}
