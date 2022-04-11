package org.zim.common.channel;

import java.nio.ByteBuffer;

public interface ZimChannel {

    int READ_STATE = 1;
    int WRITE_STATE = 2;

    void read() throws Exception;

    void write(byte[] data);

    void write(ByteBuffer buffer);

    void writeRemaining();

    void close();

    void registerListener(ZimChannelListener channelListener);
}
