package org.zim.server.version1.server;


import org.zim.common.channel.ZimChannel;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.CommandProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 17:17
 */
public class ZimServerHandler {

    private final CommandProcessor commandProcessor = new CommandProcessor();

    public void handleRead(ByteBuffer buffer, ZimChannel channel) throws IOException {
        byte[] bytes = buffer.array();
        RemoteCommand command = RemoteCommand.decode(bytes);
        commandProcessor.process(command, channel);
    }
}
