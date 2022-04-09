package org.zim.server.common.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:28
 */
public interface ZimChannel {

    int read(ByteBuffer buffer) throws IOException;

    void write(byte[] data);

    void write(ByteBuffer buffer);

    void close();

    void registerListener(ZimChannelListener channelListener);
}
