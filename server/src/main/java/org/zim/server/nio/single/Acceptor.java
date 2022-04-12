package org.zim.server.nio.single;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.ActionHandler;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.server.nio.single.handler.Handler;
import org.zim.server.nio.single.handler.ParallelHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class Acceptor implements ActionHandler {

    private final boolean useParallel;

    public Acceptor() {
        this(false);
    }

    public Acceptor(boolean useParallel) {
        this.useParallel = useParallel;
    }


    @Override
    public void action(SelectionKey key) throws IOException {
        if (key.isValid() && key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel channel = ssc.accept();
            if (channel != null) {
                log.info("zim server: [main select] accept: {}", channel.getRemoteAddress().toString());
                ZimChannelImpl zimChannel = new ZimChannelImpl(channel);

                ActionHandler actionHandler = useParallel
                        ? new ParallelHandler(zimChannel)
                        : new Handler(zimChannel);

                channel.configureBlocking(false);
                channel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, actionHandler);
            }
        }
    }
}
