package org.zhenxin.zim.common.handler;

import org.zhenxin.zim.common.channel.ZimChannel;
import org.zhenxin.zim.common.protocol.RemoteCommand;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:27
 */
public interface CommandHandler {

    RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel);
}
