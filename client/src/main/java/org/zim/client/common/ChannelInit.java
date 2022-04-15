package org.zim.client.common;

import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelInitializer;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipeline;
import org.zim.protocol.RemoteCommandSerializer;

import java.util.concurrent.Executor;

public class ChannelInit extends ZimChannelInitializer {

    // 业务对象
    private final ClientHandler clientHandler;

    private final boolean useParallel;

    private static final ZimChannelHandler remoteCommandSerializer = new RemoteCommandSerializer();

    public ChannelInit(ClientHandler clientHandler, boolean useParallel) {
        this.clientHandler = clientHandler;
        this.useParallel = useParallel;
    }

    @Override
    public void init(ZimChannel channel) {
        ZimChannelPipeline pipeline = channel.pipeline();
        Executor executor = useParallel ? clientHandler.getExecutor() : null;
        pipeline.addLast(executor,
                remoteCommandSerializer,
                clientHandler);
    }

}
