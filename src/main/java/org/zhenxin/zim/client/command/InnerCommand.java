package org.zhenxin.zim.client.command;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:14
 */
public interface InnerCommand {

    int handleCommand(String parameter, SocketChannel sc) throws IOException;
}
