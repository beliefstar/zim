package org.zim.server.nio.single;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.ActionHandler;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.eventloop.EventLoopAdapter;
import org.zim.reactor.channel.DefaultZimChannelFuture;
import org.zim.reactor.channel.impl.ZimNioChannel;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
                ZimChannel zimChannel = new ZimNioChannel(channel);

                ActionHandler actionHandler = new Handler(zimChannel);

                channel.configureBlocking(false);

                zimChannel.pipeline().addLast(channelHandler);

                DefaultZimChannelFuture future = new DefaultZimChannelFuture(zimChannel);
                zimChannel.register(new EventLoopAdapter() {
                    @Override
                    public Selector selector() {
                        return key.selector();
                    }
                }, future);

                future.addListener(f -> {
                    if (f.isSuccess()) {
                        f.channel().selectionKey().attach(actionHandler);
                    }
                });
//                channel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, actionHandler);
//                zimChannel.pipeline().fireRegister();
            }
        }
    }
}
