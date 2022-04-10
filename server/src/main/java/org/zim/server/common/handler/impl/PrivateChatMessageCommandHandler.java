package org.zim.server.common.handler.impl;

import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;
import org.zim.server.common.model.ServerClientInfo;
import org.zim.server.common.service.AccountService;

public class PrivateChatMessageCommandHandler extends AbstractCommandHandler {

    public PrivateChatMessageCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        PrivateChatMessageCommand messageCommand = (PrivateChatMessageCommand) command;
        AccountService accountService = commandProcessor.getAccountService();

        Long fromId = messageCommand.getFrom();
        Long toId = messageCommand.getTo();
        ServerClientInfo fromClient = accountService.queryById(fromId);
        ServerClientInfo toClient = accountService.queryById(toId);
        if (fromClient == null) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR);
        }
        if (toClient == null) {
            return RemoteCommand.createResponseCommand(CommandResponseType.PRIVATE_CHAT_MSG_USER_NOT_FOUND);
        }

        messageCommand.markResponse();
        messageCommand.setCode(CommandResponseType.PRIVATE_CHAT_MSG_OK.getCode());
        messageCommand.setFromName(fromClient.getUserName());

        toClient.getZimChannel().write(messageCommand.encode());
        return RemoteCommand.createResponseCommand(CommandResponseType.PRIVATE_CHAT_MSG_SEND_OK);
    }
}
