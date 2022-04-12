package org.zim.server.common.handler.impl;

import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;
import org.zim.server.common.model.ServerClientInfo;
import org.zim.server.common.service.AccountService;

public class GroupChatMessageCommandHandler extends AbstractCommandHandler {

    public GroupChatMessageCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        GroupChatMessageCommand messageCommand = (GroupChatMessageCommand) command;
        AccountService accountService = commandProcessor.getAccountService();

        Long fromId = messageCommand.getFrom();
        ServerClientInfo fromClient = accountService.queryById(fromId);
        if (fromClient == null) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR);
        }

        messageCommand.markResponse();
        messageCommand.setCode(CommandResponseType.GROUP_CHAT_MSG.getCode());
        messageCommand.setFromName(fromClient.getUserName());

        for (ServerClientInfo clientInfo : accountService.queryAllUser()) {
            if (!clientInfo.getUserId().equals(fromId)) {
                clientInfo.getZimChannel().write(messageCommand.encode());
            }
        }

        return RemoteCommand.createResponseCommand(CommandResponseType.MSG_SEND_OK);
    }
}
