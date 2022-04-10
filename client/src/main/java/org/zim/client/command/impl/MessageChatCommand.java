package org.zim.client.command.impl;

import org.zim.client.ClientHandler;
import org.zim.client.command.InnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.StringTokenHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:02
 */
public class MessageChatCommand implements InnerCommand {

    @Override
    public int handleCommand(String parameter, ZimChannel channel) throws IOException {
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

        Map<String, ClientInfo> clientInfoMap = ClientHandler.getInstance().onlineClientInfoMap;
        ClientInfo toClient = clientInfoMap.get(toName);
        if (toClient == null) {
            EchoHelper.printSystem("用户[{}]不存在", toName);
            return 0;
        }

        PrivateChatMessageCommand command = new PrivateChatMessageCommand();
        command.setFrom(ClientHandler.getInstance().getUserId());
        command.setTo(toClient.getUserId());
        command.setBody(msg.getBytes(StandardCharsets.UTF_8));
        channel.write(ByteBuffer.wrap(command.encode()));
        return 0;
    }

    @Override
    public int handleCommandResponse(RemoteCommand response) throws IOException {
        if (response.getCode() != CommandResponseType.PRIVATE_CHAT_MSG_SEND_OK.getCode()) {
            EchoHelper.printSystem(new String(response.getBody(), StandardCharsets.UTF_8));
        }
        return 0;
    }
}
