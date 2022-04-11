package org.zim.protocol;

import lombok.Getter;

@Getter
public enum CommandResponseType {

    REGISTER_OK((short) 10),

    REGISTER_ERROR((short) 11),

    REGISTER_BROADCAST((short) 12),

    PRIVATE_CHAT_MSG_SEND_OK((short) 30),

    PRIVATE_CHAT_MSG((short) 31),

    PRIVATE_CHAT_MSG_USER_NOT_FOUND((short) 32),

    QUERY_ALL_OK((short) 40),

    ECHO_OK((short) 50),

    ERROR((short) 99)
    ;

    private final short code;

    CommandResponseType(short code) {
        this.code = code;
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