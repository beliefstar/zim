package org.zim.server.nio.single.handler;

import org.zim.common.ActionHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.server.common.CommandProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 多线程处理
 *
 */
public class ParallelHandler implements ActionHandler {

    private final ZimChannel zimChannel;

    private static final CommandProcessor commandProcessor = new CommandProcessor();

    private static final Executor executor = new ThreadPoolExecutor(8, 8,
            60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        final AtomicInteger cnt = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread d = new Thread(r);
            d.setName("zim-server-nio-exec-" + cnt.getAndIncrement());
            return d;
        }
    });

    public ParallelHandler(ZimChannel zimChannel) {
        this.zimChannel = zimChannel;
        zimChannel.registerListener(new ZimChannelListener() {
            @Override
            public void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {
                executor.execute(() -> {
                    try {
                        commandProcessor.handleRead(buffer, channel);
                    } catch (IOException e) {
                        EchoHelper.print("executor error: {}", e.getMessage());
                        channel.close();
                    }
                });
            }
        });
    }

    @Override
    public void action(SelectionKey key) throws IOException {
        try {
            if (key.isValid() && key.isReadable()) {
                zimChannel.read();
            }
            if (key.isValid() && key.isWritable()) {
                zimChannel.writeRemaining();
            }
        } catch (IOException e) {
            key.cancel();
            zimChannel.close();
        }
    }
}
