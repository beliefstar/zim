package org.zim.client.starter.reactor;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.handler.ClientChannelHandler;
import org.zim.protocol.codec.LengthByteFrameDecoder;
import org.zim.protocol.codec.RemoteCommandCodec;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipeline;
import org.zim.reactor.channel.ZimChannelInitializer;

import java.util.concurrent.Executor;

public class ChannelInit extends ZimChannelInitializer {

    // 业务对象
    private final ClientHandler clientHandler;

    private final boolean useParallel;

    public ChannelInit(ClientHandler clientHandler, boolean useParallel) {
        this.clientHandler = clientHandler;
        this.useParallel = useParallel;
    }

    @Override
    public void init(ZimChannel channel) {
        ZimChannelPipeline pipeline = channel.pipeline();
        Executor executor = useParallel ? clientHandler.getExecutor() : null;
        pipeline.addLast(executor,
                // 粘包拆包处理
                new LengthByteFrameDecoder(),
                new RemoteCommandCodec(),
                new ClientChannelHandler(clientHandler));
    }

}
