package org.zim.server.starter.reactor;

import org.zim.protocol.RemoteCommand;
import org.zim.protocol.codec.LengthByteFrameDecoder;
import org.zim.protocol.codec.RemoteCommandCodec;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipeline;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;
import org.zim.reactor.channel.ZimChannelInitializer;
import org.zim.server.common.CommandProcessor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelInit extends ZimChannelInitializer {

    // 业务对象
    private static final CommandProcessor commandProcessor = new CommandProcessor();

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
                // 粘包拆包处理
                new LengthByteFrameDecoder(),
                new RemoteCommandCodec(),
                new ServerHandler());
    }


    private static class ServerHandler implements ZimChannelHandler {

        @Override
        public void handleRegister(ZimChannelPipelineContext ctx) throws Exception {
            System.out.println("accept " + ctx.channel().remoteAddress());
        }

        @Override
        public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
            commandProcessor.process(((RemoteCommand) msg), ctx.channel());
        }
    }
}
