package org.zim.client.command.impl;

import org.zim.client.command.InnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:02
 */
public class EchoCommand implements InnerCommand {

    @Override
    public int handleCommand(String parameter, ZimChannel channel) throws IOException {
        RemoteCommand command = new RemoteCommand();
        command.setCode(CommandRequestType.ECHO.getCode());
        command.setBody(parameter.getBytes(StandardCharsets.UTF_8));
        channel.write(ByteBuffer.wrap(command.encode()));
        return 0;
    }

    @Override
    public int handleCommandResponse(RemoteCommand response) throws IOException {
        String s = new String(response.getBody(), StandardCharsets.UTF_8);
        EchoHelper.print(s);
        return 0;
    }
}
