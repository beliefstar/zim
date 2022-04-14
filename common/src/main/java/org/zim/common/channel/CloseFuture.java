package org.zim.common.channel;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CloseFuture {

    private final Queue<ZimChannelCloseListener> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean state = new AtomicBoolean(false);

    public void addListener(ZimChannelCloseListener actionHandler) {
        if (state.get()) {
            actionHandler.onClose();
            return;
        }
        queue.offer(actionHandler);
    }

    public void closeComplete() {
        state.set(true);

        try {
            while (!queue.isEmpty()) {
                queue.poll().onClose();
            }
        } catch (Exception ignore) {
        }
    }
}
