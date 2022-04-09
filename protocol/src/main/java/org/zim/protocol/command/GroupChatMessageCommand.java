package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

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
