package org.zim.client.common.scan.pipline;

import com.alibaba.fastjson.JSON;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.common.model.ClientInfo;
import org.zim.common.pipline.PipLineContext;
import org.zim.common.pipline.PipLineHandler;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegisterHandler implements PipLineHandler<String>, MessageHandler {

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
    public void handle(String line, PipLineContext<String> context) {
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
        if (response.getCode() == CommandResponseType.REGISTER_OK.getCode()) {
            String body = new String(response.getBody(), StandardCharsets.UTF_8);
            List<ClientInfo> clientInfos = JSON.parseArray(body, ClientInfo.class);

            clientHandler.updateOnlineUser(clientInfos);
            EchoHelper.printSystem("online success! username is [" + clientHandler.getUserName() + "], welcome to zim!");
            markRegistered();
        }
        else if (response.getCode() == CommandResponseType.REGISTER_BROADCAST.getCode()) {
            Long userId = Long.valueOf(response.getExtendField(MessageConstants.TO));
            String userName = response.getExtendField(MessageConstants.TO_NAME);
            clientHandler.onlineClientInfoMap.put(userName, ClientInfo.of(userId, userName));

            EchoHelper.printSystem("user [{}] online", userName);
        }
        else {
            markUnRegistered();
            EchoHelper.printSystem(new String(response.getBody(), StandardCharsets.UTF_8));
        }
        if (sendRegister) {
            sendRegister = false;
        }
    }
}
