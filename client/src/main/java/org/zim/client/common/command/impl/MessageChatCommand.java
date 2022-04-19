package org.zim.client.common.command.impl;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.Command;
import org.zim.client.common.command.InnerCommand;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.StringChecker;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MessageChatCommand implements InnerCommand, MessageHandler {
    public static final String GROUP_ALL = "all";

    @Override
    public void handleCommand(Command command, ClientHandler clientHandler) {
        String toName = command.getName();
        String msg = command.getParameter();

        if (StringChecker.isEmpty(msg)) {
            EchoHelper.printSystem("命令格式错误, 内容不可为空");
            return;
        }

        Map<String, ClientInfo> clientInfoMap = clientHandler.onlineClientInfoMap;
        ClientInfo toClient = clientInfoMap.get(toName);
        if (toClient == null) {
            // maybe group
            if (toName.equals(GROUP_ALL)) {
                handleGroupMessage(msg, clientHandler);
                return;
            }

            EchoHelper.printSystem("用户[{}]不存在", toName);
            return;
        }

        handlePrivateMessage(msg, toClient, clientHandler);
    }

    @Override
    public void consumeMessage(RemoteCommand response) {
        if (response.getCode() != CommandResponseType.MSG_SEND_OK.getCode()) {
            EchoHelper.printSystem(response.getBodyString());
        }
    }

    private void handleGroupMessage(String msg, ClientHandler clientHandler) {
        GroupChatMessageCommand command = new GroupChatMessageCommand();
        command.setFrom(clientHandler.getUserId());
        command.setBody(msg.getBytes(StandardCharsets.UTF_8));

        clientHandler.getChannel().write(command);
    }

    private void handlePrivateMessage(String msg, ClientInfo toClient, ClientHandler clientHandler) {
        PrivateChatMessageCommand command = new PrivateChatMessageCommand();
        command.setFrom(clientHandler.getUserId());
        command.setTo(toClient.getUserId());
        command.setBody(msg.getBytes(StandardCharsets.UTF_8));
        clientHandler.getChannel().write(command);
    }
}
