package org.zim.client.common.command.impl;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.Command;
import org.zim.client.common.command.InnerCommand;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.util.Map;

public class RenameCommand implements InnerCommand, MessageHandler {

    private volatile String newName;

    @Override
    public void handleCommand(Command console, ClientHandler clientHandler) {
        newName = console.getParameter().trim();

        if (newName.equals(clientHandler.getUserName())) {
            EchoHelper.printSystem("same name");
            return;
        }

        RegisterCommand command = new RegisterCommand();
        command.setCode(CommandRequestType.RENAME.getCode());
        command.setUserId(clientHandler.getUserId());
        command.setUserName(newName);

        clientHandler.getChannel().write(command);
    }

    @Override
    public void consumeMessage(RemoteCommand response) {
        ClientHandler clientHandler = ClientHandler.INSTANCE;
        if (response.getCode() == CommandResponseType.RENAME_OK.getCode()) {
            String newUserName = newName;
            newName = null;
            clientHandler.setUserName(newUserName);
            EchoHelper.printSystem("rename [{}] success", newUserName);
            return;
        }

        if (response.getCode() == CommandResponseType.BROADCAST_RENAME.getCode()) {
            Long userId = Long.parseLong(response.getExtendField(MessageConstants.TO));
            String userName = response.getExtendField(MessageConstants.FROM_NAME);
            String newUserName = response.getExtendField(MessageConstants.TO_NAME);

            Map<String, ClientInfo> onlineClientInfoMap = clientHandler.onlineClientInfoMap;
            onlineClientInfoMap.remove(userName);
            onlineClientInfoMap.put(newUserName, ClientInfo.of(userId, newUserName));

            EchoHelper.printSystem("user [{}] rename to [{}]", userName, newUserName);
            return;
        }

        EchoHelper.printSystem(response.getBodyString());
    }
}
