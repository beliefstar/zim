package org.zim.client.common.command.impl;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.InnerCommand;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.StringTokenHelper;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MessageChatCommand implements InnerCommand, MessageHandler {

    @Override
    public int handleCommand(String parameter, ClientHandler clientHandler) {
        StringTokenHelper tokens = new StringTokenHelper(parameter);
        String toName;
        String msg;
        if (!tokens.hasNext()) {
            EchoHelper.printSystem("命令格式错误, [-help to]查看");
            return 0;
        }
        toName = tokens.next();
        if (!tokens.hasNext()) {
            EchoHelper.printSystem("命令格式错误, [-help to]查看");
            return 0;
        }
        msg = tokens.remaining();

        Map<String, ClientInfo> clientInfoMap = clientHandler.onlineClientInfoMap;
        ClientInfo toClient = clientInfoMap.get(toName);
        if (toClient == null) {
            // maybe group
            if (toName.equals("all")) {
                handleGroupMessage(msg, clientHandler);
                return 0;
            }

            EchoHelper.printSystem("用户[{}]不存在", toName);
            return 0;
        }

        handlePrivateMessage(msg, toClient, clientHandler);
        return 0;
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

        clientHandler.getChannel().write(ByteBuffer.wrap(command.encode()));
    }

    private void handlePrivateMessage(String msg, ClientInfo toClient, ClientHandler clientHandler) {
        PrivateChatMessageCommand command = new PrivateChatMessageCommand();
        command.setFrom(clientHandler.getUserId());
        command.setTo(toClient.getUserId());
        command.setBody(msg.getBytes(StandardCharsets.UTF_8));
        clientHandler.getChannel().write(ByteBuffer.wrap(command.encode()));
    }
}
