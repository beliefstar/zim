package org.zim.server.common;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelInitializer;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipeline;
import org.zim.protocol.RemoteCommandSerializer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelInit extends ZimChannelInitializer {

    // 业务对象
    private static final CommandProcessor commandProcessor = new CommandProcessor();

    private static final ZimChannelHandler remoteCommandSerializer = new RemoteCommandSerializer();

    private Executor executor;

    public ChannelInit(boolean useParallel) {
        if (useParallel) {
            executor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
                final AtomicInteger count = new AtomicInteger();
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("zim-server-nio-exec-" + count.incrementAndGet());
                    return t;
                }
            });
        }
    }

    @Override
    public void init(ZimChannel channel) {
        ZimChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(executor,
                remoteCommandSerializer,
                commandProcessor,
                commandProcessor.getAccountService());
    }

}