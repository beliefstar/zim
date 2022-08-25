package org.zim.client.common.scan.pipeline;

import com.alibaba.fastjson.JSON;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.message.MessageHandler;
import org.zim.client.common.scan.ScanHandler;
import org.zim.common.EchoHelper;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.util.List;

import static org.zim.protocol.MessageConstants.*;

public class RegisterHandler implements ScanHandler, MessageHandler {

    private final ClientHandler clientHandler;

    private boolean registered = false;
    private volatile boolean sendRegister = false;

    public RegisterHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void echoNotice() {
        EchoHelper.print("userName:");
    }

    @Override
    public boolean handle(String line) {
        if (registered) {
            return true;
        }
        if (!sendRegister) {
            clientHandler.setUserName(line);

            remoteRegister();
        }
        return false;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void markRegistered() {
        registered = true;
    }

    public void markUnRegistered() {
        registered = false;
    }

    public void remoteRegister() {
        RegisterCommand command = new RegisterCommand();
        command.setUserName(clientHandler.getUserName());

        clientHandler.getChannel().write(command);
        sendRegister = true;
    }

    @Override
    public void consumeMessage(RemoteCommand response) {
        CommandResponseType type = CommandResponseType.valueOf(response.getCode());
        if (type == null) {
            EchoHelper.printSystemError("unknown remote command code [{}]", response.getCode());
            return;
        }
        switch (type) {
            case REGISTER_OK: {
                List<ClientInfo> clientInfos = JSON.parseArray(response.getBodyString(), ClientInfo.class);

                clientHandler.updateOnlineUser(clientInfos);

                Long userId = Long.parseLong(response.getExtendField(USER_ID));

                clientHandler.setUserId(userId);

                EchoHelper.printSystem("online success! username is [" + clientHandler.getUserName() + "], welcome to zim!");
                markRegistered();
                break;
            }
            case BROADCAST_ONLINE: {
                Long userId = Long.valueOf(response.getExtendField(MessageConstants.TO));
                String userName = response.getExtendField(MessageConstants.TO_NAME);
                clientHandler.onlineClientInfoMap.put(userName, ClientInfo.of(userId, userName));

                EchoHelper.printSystem("user [{}] online", userName);
                break;
            }
            case BROADCAST_OFFLINE: {
                Long userId = Long.valueOf(response.getExtendField(MessageConstants.TO));
                String userName = response.getExtendField(MessageConstants.TO_NAME);
                clientHandler.onlineClientInfoMap.remove(userName);

                EchoHelper.printSystem("user [{}] offline", userName);
                break;
            }
            default: {
                markUnRegistered();
                EchoHelper.printSystem(response.getBodyString());
            }
        }

        if (sendRegister) {
            sendRegister = false;
        }
    }
}
