package org.zim.common.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:21
 */
public abstract class AbstractZimChannel implements ZimChannel {

    private final ConcurrentLinkedQueue<ZimChannelListener> listeners = new ConcurrentLinkedQueue<>();

    protected volatile int state = 0;

    @Override
    public void registerListener(ZimChannelListener channelListener) {
        listeners.offer(channelListener);
    }

    public void triggerOnClose() {
        for (ZimChannelListener listener : listeners) {
            listener.onClose(this);
        }
    }

    public void triggerOnRead(ByteBuffer buffer) throws IOException {
        for (ZimChannelListener listener : listeners) {
            listener.onRead(this, buffer);
        }
    }

    public boolean isReadState() {
        return state == ZimChannel.READ_STATE;
    }

    public boolean isWriteState() {
        return state == ZimChannel.WRITE_STATE;
    }

    public void markReadState() {
        state = ZimChannel.READ_STATE;
    }

    public void markWriteState() {
        state = ZimChannel.WRITE_STATE;
    }
}
