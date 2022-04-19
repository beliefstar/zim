package org.zim.client.common.handler;

import org.zim.client.common.ClientHandler;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;
import org.zim.protocol.RemoteCommand;

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
