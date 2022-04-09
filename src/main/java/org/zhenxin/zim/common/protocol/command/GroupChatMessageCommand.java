package org.zhenxin.zim.common.protocol.command;

import org.zhenxin.zim.common.protocol.RemoteCommand;
import org.zhenxin.zim.common.protocol.CommandRequestType;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:12
 */
public class GroupChatMessageCommand extends RemoteCommand {

    public GroupChatMessageCommand() {
        setCode(CommandRequestType.GROUP_CHAT_MESSAGE.getCode());
    }
}
