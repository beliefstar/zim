package org.zim.protocol;

import lombok.Getter;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.protocol.command.RegisterCommand;

import java.util.function.Supplier;

@Getter
public enum CommandRequestType {

    REGISTER((short) 1, RegisterCommand::new),

    GROUP_CHAT_MESSAGE((short) 2, GroupChatMessageCommand::new),

    PRIVATE_CHAT_MESSAGE((short) 3, PrivateChatMessageCommand::new),

    QUERY_ALL_USER((short) 4),

    ECHO((short) 5),
    ;

    private final short code;
    private final Supplier<? extends RemoteCommand> supplier;

    CommandRequestType(short code) {
        this(code, RemoteCommand::new);
    }
    CommandRequestType(short code, Supplier<? extends RemoteCommand> supplier) {
        this.code = code;
        this.supplier = supplier;
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
