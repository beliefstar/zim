package org.zim.reactor.channel;

import lombok.extern.slf4j.Slf4j;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;
import org.zim.reactor.api.channel.ZimChannelFutureListener;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultZimChannelFuture implements ZimChannelFuture {

    private volatile Queue<ZimChannelFutureListener> queue = null;

    private final AtomicBoolean completeState = new AtomicBoolean(false);
    // 1: success / 2: fail
    private final AtomicInteger answerState = new AtomicInteger(0);

    private final ZimChannel channel;

    private volatile int waiter = 0;

    public DefaultZimChannelFuture(ZimChannel channel) {
        this.channel = channel;
    }

    @Override
    public DefaultZimChannelFuture addListener(ZimChannelFutureListener listener) {
        synchronized (this) {
            addListener0(listener);
        }

        if (isDone()) {
            invokeListener();
        }
        return this;
    }

    @Override
    public void complete() {
        if (answerState.compareAndSet(0, 1)) {

            postDone();
        }
    }

    @Override
    public void failure() {
        if (answerState.compareAndSet(0, 2)) {

            postDone();
        }
    }

    private void postDone() {
        if (!completeState.compareAndSet(false, true)) {
            return;
        }

        notifyWaiters();

        invokeListener();
    }

    @Override
    public DefaultZimChannelFuture sync() throws InterruptedException {

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

    @Override
    public boolean isDone() {
        return completeState.get();
    }

    @Override
    public boolean isSuccess() {
        return answerState.get() == 1;
    }

    private void notifyWaiters() {
        synchronized (this) {
            if (waiter > 0) {
                notifyAll();
            }
        }
    }

    @Override
    public ZimChannel channel() {
        return channel;
    }

    private void addListener0(ZimChannelFutureListener listener) {
        if (queue == null) {
            queue = new ArrayDeque<>();
        }
        queue.add(listener);
    }

    private void invokeListener() {
        Queue<ZimChannelFutureListener> listeners;
        synchronized (this) {
            if (queue == null || queue.isEmpty()) {
                return;
            }
            listeners = queue;
            queue = null;
        }
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (ZimChannelFutureListener listener : listeners) {
            try {
                listener.onComplete(this);
            } catch (Exception e) {
                log.error("invoke ChannelFutureListener error: {}", e.getMessage());
            }
        }
    }
}
