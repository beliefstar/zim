package org.zim.client.command.impl;

import com.alibaba.fastjson.JSON;
import org.zim.client.EchoHelper;
import org.zim.client.command.InnerCommand;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:02
 */
public class QueryAllUserCommand implements InnerCommand {

    @Override
    public int handleCommand(String parameter, SocketChannel sc) throws IOException {
        RemoteCommand command = new RemoteCommand();
        command.setCode(CommandRequestType.QUERY_ALL_USER.getCode());
        sc.write(ByteBuffer.wrap(command.encode()));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        sc.read(buffer);
        RemoteCommand response = RemoteCommand.decode(buffer.array());
        String s = new String(response.getBody(), StandardCharsets.UTF_8);
        List<String> list = JSON.parseArray(s, String.class);
        for (String v : list) {
            EchoHelper.print(v);
        }
        return 0;
    }
}
