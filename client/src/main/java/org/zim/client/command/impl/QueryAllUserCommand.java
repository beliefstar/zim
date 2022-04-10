package org.zim.client.command.impl;

import com.alibaba.fastjson.JSON;
import org.zim.client.ClientHandler;
import org.zim.client.command.InnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:02
 */
public class QueryAllUserCommand implements InnerCommand {

    @Override
    public int handleCommand(String parameter, ZimChannel channel) throws IOException {
        RemoteCommand command = new RemoteCommand();
        command.setCode(CommandRequestType.QUERY_ALL_USER.getCode());
        channel.write(ByteBuffer.wrap(command.encode()));
        return 0;
    }

    @Override
    public int handleCommandResponse(RemoteCommand response) throws IOException {
        String s = new String(response.getBody(), StandardCharsets.UTF_8);
        List<ClientInfo> list = JSON.parseArray(s, ClientInfo.class);
        for (ClientInfo v : list) {
            EchoHelper.print(v.getUserName());
        }
        ClientHandler.getInstance().updateOnlineUser(list);
        return 0;
    }
}
