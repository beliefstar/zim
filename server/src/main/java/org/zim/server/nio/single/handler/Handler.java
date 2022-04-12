package org.zim.server.nio.single.handler;

import org.zim.common.ActionHandler;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.server.common.CommandProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;


/**
 * 单线程处理
 */
public class Handler implements ActionHandler {

    private final ZimChannel zimChannel;

    private static final CommandProcessor commandProcessor = new CommandProcessor();

    public Handler(ZimChannel zimChannel) {
        this.zimChannel = zimChannel;
        zimChannel.registerListener(new ZimChannelListener() {
            @Override
            public void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {
                commandProcessor.handleRead(buffer, channel);
            }
        });
    }

    @Override
    public void action(SelectionKey key) throws IOException {
        try {
            if (key.isValid() && key.isReadable()) {
                zimChannel.read();
            }
            if (key.isValid() && key.isWritable()) {
                zimChannel.writeRemaining();
            }
        } catch (IOException e) {
            key.cancel();
            zimChannel.close();
        }
    }
}
