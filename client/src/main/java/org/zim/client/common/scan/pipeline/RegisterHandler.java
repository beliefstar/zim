package org.zim.client.common.scan.pipeline;

import com.alibaba.fastjson.JSON;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.model.ClientInfo;
import org.zim.common.pipeline.PipelineContext;
import org.zim.common.pipeline.PipelineHandler;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.nio.ByteBuffer;
import java.util.List;

public class RegisterHandler implements PipelineHandler<String>, MessageHandler {

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
    public void handle(String line, PipelineContext<String> context) {
        if (registered) {
            context.fireHandle(line);
            return;
        }
        if (sendRegister) {
            // ignore
            return;
        }
        clientHandler.setUserId(System.currentTimeMillis());
        clientHandler.setUserName(line);

        remoteRegister();
    }

    public void markRegistered() {
        registered = true;
    }

    public void markUnRegistered() {
        registered = false;
    }

    public void remoteRegister() {
        RegisterCommand command = new RegisterCommand();
        command.setUserId(clientHandler.getUserId());
        command.setUserName(clientHandler.getUserName());

        byte[] encode = command.encode();
        clientHandler.getChannel().write(ByteBuffer.wrap(encode));
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
            default:{
                markUnRegistered();
                EchoHelper.printSystem(response.getBodyString());
            }
        }

        if (sendRegister) {
            sendRegister = false;
        }
    }
}
