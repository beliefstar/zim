package org.zim.server.common.handler;


import org.zim.protocol.RemoteCommand;
import org.zim.server.common.channel.ZimChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:27
 */
public interface CommandHandler {

    RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel);
}
