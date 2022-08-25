package org.zim.reactor.eventloop;

import lombok.extern.slf4j.Slf4j;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.ZimChannelFuture;
import org.zim.reactor.api.eventloop.EventLoop;
import org.zim.reactor.channel.DefaultZimChannelFuture;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class ReactorEventLoop implements EventLoop {

    private final Selector selector;

    private final ThreadFactory threadFactory;

    private final Queue<Runnable> registerQueue = new ConcurrentLinkedQueue<>();

    private Thread thread;

    public ReactorEventLoop(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public ZimChannelFuture register(ZimChannel channel) {
        DefaultZimChannelFuture regFuture = new DefaultZimChannelFuture(channel);
        channel.register(this, regFuture);
        return regFuture;
    }

    private void checkRunning() {
        if (thread == null) {
            synchronized (this) {
                if (thread == null) {
                    thread = threadFactory.newThread(this::run);
                    thread.start();
                }
            }
        }
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                int count = selector.select();
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        dispatch(key);
                        iterator.remove();
                    }
                }

                runRegisterTask();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatch(SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        ZimChannel channel = (ZimChannel) key.attachment();
        ZimChannel.Unsafe unsafe = channel.unsafe();

        if (key.isReadable() || key.isAcceptable()) {
            unsafe.read();
        }
        if (key.isValid() && key.isWritable()) {
            unsafe.flush();
        }
        if (key.isValid() && key.isConnectable()) {
            int ops = key.interestOps();
            ops &= ~SelectionKey.OP_CONNECT;
            key.interestOps(ops);

            unsafe.finishConnect();
        }
    }

    private void runRegisterTask() {
        while (!registerQueue.isEmpty()) {
            Runnable command = registerQueue.poll();

            command.run();
        }
    }

    @Override
    public void execute(Runnable command) {
        checkRunning();

        registerQueue.offer(command);

        if (!inEventLoop()) {
            selector.wakeup();
        }
    }

    public boolean inEventLoop() {
        return thread == Thread.currentThread();
    }

    @Override
    public Selector selector() {
        return this.selector;
    }

    @Override
    public void close() {
        if (thread != null) {
            thread.interrupt();
            selector.wakeup();
        }
    }
}
