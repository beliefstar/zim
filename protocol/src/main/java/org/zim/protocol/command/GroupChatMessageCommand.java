package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

public class GroupChatMessageCommand extends RemoteCommand {

    public GroupChatMessageCommand() {
        setCode(CommandRequestType.GROUP_CHAT_MESSAGE.getCode());
    }
}
