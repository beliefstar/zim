package org.zim.server.nio.sub;

import org.zim.common.channel.ZimChannel;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

public class SubReactor implements Runnable {

    private final Selector selector;

    private volatile Thread runThread;
    private final ThreadFactory threadFactory;

    private final Queue<ZimChannel> registerQueue = new ConcurrentLinkedQueue<>();

    public SubReactor(ThreadFactory threadFactory) throws IOException {
        this.threadFactory = threadFactory;

        this.selector = Selector.open();
    }

    public void register(ZimChannel channel) {
        checkRunning();

        registerQueue.offer(channel);
        selector.wakeup();
    }

    private void checkRunning() {
        if (runThread == null) {
            synchronized (this) {
                if (runThread == null) {
                    runThread = threadFactory.newThread(this);
                    runThread.start();
                }
            }
        }
    }

    @Override
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
        ZimChannel channel = (ZimChannel) key.attachment();
        ZimChannel.Unsafe unsafe = channel.unsafe();
        if (key.isValid() && key.isReadable()) {
            unsafe.read();
        }
        if (key.isValid() && key.isWritable()) {
            unsafe.flush();
        }
    }

    private void runRegisterTask() {
        while (!registerQueue.isEmpty()) {
            ZimChannel channel = registerQueue.poll();

            channel.register(selector);
        }
    }
}
