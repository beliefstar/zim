package org.zim.server.version1;


import org.zim.server.common.channel.AbstractZimChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:29
 */
public class ZimChannelImpl extends AbstractZimChannel {

    private final SocketChannel channel;

    public ZimChannelImpl(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        int read = channel.read(buffer);
        triggerOnRead(buffer, read);
        return read;
    }

    @Override
    public void write(byte[] data) {
        write(ByteBuffer.wrap(data));
    }

    @Override
    public void write(ByteBuffer buffer) {
        triggerOnWrite(buffer);
        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void close() {
        triggerOnClose(this);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
