package org.zim.common.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ZimChannel {

    int READ_STATE = 1;
    int WRITE_STATE = 2;

    void read() throws IOException;

    void write(byte[] data);

    void write(ByteBuffer buffer);

    void writeRemaining() throws IOException;

    void close();

    void registerListener(ZimChannelListener channelListener);
}
