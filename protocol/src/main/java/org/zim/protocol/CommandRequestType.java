package org.zim.protocol;

import lombok.Getter;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.protocol.command.RegisterCommand;

@Getter
public enum CommandRequestType {

    REGISTER((short) 1, RegisterCommand.class),

    RENAME((short) 11, RegisterCommand.class),

    GROUP_CHAT_MESSAGE((short) 2, GroupChatMessageCommand.class),

    PRIVATE_CHAT_MESSAGE((short) 3, PrivateChatMessageCommand.class),

    QUERY_ALL_USER((short) 4),

    ECHO((short) 5),
    ;

    private final short code;
    private final Class<? extends RemoteCommand> type;

    CommandRequestType(short code) {
        this(code, RemoteCommand.class);
    }

    CommandRequestType(short code, Class<? extends RemoteCommand> type) {
        this.code = code;
        this.type = type;
    }

    public static CommandRequestType valueOf(short b) {
        for (CommandRequestType value : values()) {
            if (value.getCode() == b) {
                return value;
            }
        }
        return null;
    }
}
