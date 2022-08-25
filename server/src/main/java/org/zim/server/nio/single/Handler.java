package org.zim.server.nio.single;

import org.zim.common.ActionHandler;
import org.zim.reactor.api.channel.ZimChannel;

import java.io.IOException;
import java.nio.channels.SelectionKey;


/**
 * 单线程处理
 */
public class Handler implements ActionHandler {

    private final ZimChannel.Unsafe unsafe;

    public Handler(ZimChannel zimChannel) {
        this.unsafe = zimChannel.unsafe();
    }

    @Override
    public void action(SelectionKey key) throws IOException {
        if (key.isValid() && key.isReadable()) {
            unsafe.read();
        }
        if (key.isValid() && key.isWritable()) {
            unsafe.flush();
        }
    }
}
