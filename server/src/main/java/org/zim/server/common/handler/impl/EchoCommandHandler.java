package org.zim.server.common.handler.impl;


import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;

public class EchoCommandHandler extends AbstractCommandHandler {

    public EchoCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        RemoteCommand response = RemoteCommand.createResponseCommand(CommandResponseType.ECHO_OK);
        response.setBody(command.getBody());
        return response;
    }
}
