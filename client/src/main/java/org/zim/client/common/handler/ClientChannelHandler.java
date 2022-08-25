package org.zim.client.common.handler;

import org.zim.client.common.ClientHandler;
import org.zim.protocol.RemoteCommand;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;

public class ClientChannelHandler implements ZimChannelHandler {

    private final ClientHandler clientHandler;

    public ClientChannelHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void handleActive(ZimChannelPipelineContext ctx) throws Exception {
        clientHandler.setChannel(ctx.channel());
    }

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        clientHandler.getMessageConsumer().handle(((RemoteCommand) msg));
    }
}
