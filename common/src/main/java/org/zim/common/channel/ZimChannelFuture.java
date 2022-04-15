package org.zim.common.channel;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZimChannelFuture {

    private final Queue<ZimChannelCloseListener> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean completeState = new AtomicBoolean(false);
    private final AtomicBoolean answerState = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);

    private final ZimChannel channel;

    private int waiter = 0;

    public ZimChannelFuture(ZimChannel channel) {
        this.channel = channel;
    }

    public ZimChannelFuture addListener(ZimChannelCloseListener actionHandler) {
        if (completeState.get()) {
            actionHandler.onComplete(this);
            return this;
        }
        processing.set(true);
        try {
            queue.offer(actionHandler);
        } finally {
            processing.set(false);
        }
        return this;
    }

    public void failure() {
        answerState.set(false);

        postDone();
    }

    public void complete() {
        answerState.set(true);

        postDone();
    }

    private void postDone() {
        completeState.set(true);

        while (processing.get());

        notifyWaiters();

        while (!queue.isEmpty()) {
            ZimChannelCloseListener listener = queue.poll();
            try {
                listener.onComplete(this);
            } catch (Exception ignore) {
            }
        }
    }

    public ZimChannelFuture sync() throws InterruptedException {

        if (channel.eventLoop().inEventLoop()) {
            throw new RuntimeException("dead lock");
        }

        synchronized (this) {
            while (!isDone()) {
                ++waiter;
                wait();
                --waiter;
            }
        }
        return this;
    }

    public boolean isDone() {
        return completeState.get();
    }

    public boolean isSuccess() {
        return answerState.get();
    }

    private void notifyWaiters() {
        synchronized (this) {
            if (waiter > 0) {
                notifyAll();
            }
        }
    }

    public ZimChannel channel() {
        return channel;
    }
}
