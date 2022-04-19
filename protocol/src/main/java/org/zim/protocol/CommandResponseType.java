package org.zim.protocol;

import lombok.Getter;

@Getter
public enum CommandResponseType {

    REGISTER_OK((short) 10),

    REGISTER_ERROR((short) 11),

    RENAME_OK((short) 12),

    BROADCAST_ONLINE((short) 101),

    BROADCAST_OFFLINE((short) 102),

    BROADCAST_RENAME((short) 103),

    MSG_SEND_OK((short) 20),

    GROUP_CHAT_MSG((short) 21),

    PRIVATE_CHAT_MSG((short) 31),

    PRIVATE_CHAT_MSG_USER_NOT_FOUND((short) 32, "user not found"),

    QUERY_ALL_OK((short) 40),

    ECHO_OK((short) 50),

    ERROR((short) 99)
    ;

    private final short code;

    private final String info;

    CommandResponseType(short code) {
        this(code, null);
    }

    CommandResponseType(short code, String info) {
        this.code = code;
        this.info = info;
    }

    public static CommandResponseType valueOf(short b) {
        for (CommandResponseType value : values()) {
            if (value.getCode() == b) {
                return value;
            }
        }
        return null;
    }
}
