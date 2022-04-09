package org.zhenxin.zim.common.channel;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:21
 */
public abstract class AbstractZimChannel implements ZimChannel {

    private final ConcurrentLinkedQueue<ZimChannelListener> listeners = new ConcurrentLinkedQueue<>();

    @Override
    public void registerListener(ZimChannelListener channelListener) {
        listeners.offer(channelListener);
    }

    public void triggerOnRead(ByteBuffer buffer, int readSize) {
        for (ZimChannelListener listener : listeners) {
            listener.onRead(buffer, readSize);
        }
    }

    public void triggerOnWrite(ByteBuffer buffer) {
        for (ZimChannelListener listener : listeners) {
            listener.onWrite(buffer);
        }
    }

    public void triggerOnClose(ZimChannel channel) {
        for (ZimChannelListener listener : listeners) {
            listener.onClose(channel);
        }
    }
}
