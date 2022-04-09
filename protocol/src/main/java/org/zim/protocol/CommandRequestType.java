package org.zim.protocol;

import lombok.Getter;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.protocol.command.RegisterCommand;

import java.util.function.Supplier;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/7 15:02
 */
@Getter
public enum CommandRequestType {

    REGISTER((short) 0, RegisterCommand::new),

    GROUP_CHAT_MESSAGE((short) 1, GroupChatMessageCommand::new),

    PRIVATE_CHAT_MESSAGE((short) 2, PrivateChatMessageCommand::new),

    QUERY_ALL_USER((short) 3, RemoteCommand::new),
    ;

    private final short code;
    private final Supplier<? extends RemoteCommand> supplier;

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
