package org.zim.protocol;

import lombok.Getter;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 17:15
 */
@Getter
public enum CommandResponseType {

    REGISTER_OK((short) 10),

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
