package org.zim.client.common.command.impl;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.InnerCommand;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import java.nio.charset.StandardCharsets;

public class EchoCommand implements InnerCommand, MessageHandler {

    @Override
    public int handleCommand(String parameter, ClientHandler clientHandler) {
        RemoteCommand command = new RemoteCommand();
        command.setCode(CommandRequestType.ECHO.getCode());
        command.setBody(parameter.getBytes(StandardCharsets.UTF_8));
        clientHandler.getChannel().write(command);
        return 0;
    }

    @Override
    public void consumeMessage(RemoteCommand response) {
        EchoHelper.print(response.getBodyString());
    }
}
