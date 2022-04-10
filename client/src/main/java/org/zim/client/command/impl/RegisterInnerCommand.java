package org.zim.client.command.impl;

import com.alibaba.fastjson.JSON;
import org.zim.client.ClientHandler;
import org.zim.client.command.InnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:02
 */
public class RegisterInnerCommand implements InnerCommand {

    private RegisterCommand registerCommand;

    @Override
    public int handleCommand(String parameter, ZimChannel channel) throws IOException {
        ClientHandler clientHandler = ClientHandler.getInstance();
        clientHandler.setUserId(System.currentTimeMillis());
        clientHandler.setUserName(parameter);

        RegisterCommand command = new RegisterCommand();
        command.setUserId(clientHandler.getUserId());
        command.setUserName(clientHandler.getUserName());

        byte[] encode = command.encode();
        channel.write(ByteBuffer.wrap(encode));

        registerCommand = command;
        return 0;
    }

    @Override
    public int handleCommandResponse(RemoteCommand response) throws IOException {
        if (response.getCode() == CommandResponseType.REGISTER_OK.getCode()) {
            String body = new String(response.getBody(), StandardCharsets.UTF_8);
            List<ClientInfo> clientInfos = JSON.parseArray(body, ClientInfo.class);

            ClientHandler.getInstance().updateOnlineUser(clientInfos);
            EchoHelper.printSystem("online success! username is [" + registerCommand.getUserName() + "], welcome to zim!");
            return 0;
        } else {
            EchoHelper.printSystem(new String(response.getBody(), StandardCharsets.UTF_8));
            return -1;
        }
    }
}
