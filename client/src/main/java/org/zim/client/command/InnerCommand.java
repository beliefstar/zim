package org.zim.client.command;

import org.zim.common.channel.ZimChannel;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:14
 */
public interface InnerCommand {

    int handleCommand(String parameter, ZimChannel channel) throws IOException;

    default int handleCommandResponse(RemoteCommand response) throws IOException {
        return 0;
    }
}
