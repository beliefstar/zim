package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import static org.zim.protocol.MessageConstants.FROM;
import static org.zim.protocol.MessageConstants.FROM_NAME;

public class GroupChatMessageCommand extends RemoteCommand {

    public GroupChatMessageCommand() {
        setCode(CommandRequestType.GROUP_CHAT_MESSAGE.getCode());
    }

    public void setFrom(long from) {
        putExtendField(FROM, Long.toString(from));
    }

    public Long getFrom() {
        String v = getExtendField(FROM);
        if (v != null) {
            return Long.parseLong(v);
        }
        return null;
    }

    public void setFromName(String fromName) {
        putExtendField(FROM_NAME, fromName);
    }
}
