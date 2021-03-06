package org.zim.client.common.command.impl;

import com.alibaba.fastjson.JSON;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.Command;
import org.zim.client.common.command.InnerCommand;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import java.util.List;

public class QueryAllUserCommand implements InnerCommand, MessageHandler {

    @Override
    public void handleCommand(Command console, ClientHandler clientHandler) {
        RemoteCommand command = new RemoteCommand();
        command.setCode(CommandRequestType.QUERY_ALL_USER.getCode());
        clientHandler.getChannel().write(command);
    }

    @Override
    public void consumeMessage(RemoteCommand response) {
        List<ClientInfo> list = JSON.parseArray(response.getBodyString(), ClientInfo.class);
        for (ClientInfo v : list) {
            EchoHelper.print(v.getUserName());
        }
        ClientHandler.INSTANCE.updateOnlineUser(list);
    }
}
