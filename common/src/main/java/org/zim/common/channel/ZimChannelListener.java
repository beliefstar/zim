package org.zim.common.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ZimChannelListener {

    default void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {}

    default void onClose(ZimChannel zimChannel) {}
}
