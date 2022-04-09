package org.zhenxin.zim.common.protocol;

import lombok.Getter;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 17:15
 */
@Getter
public enum CommandResponseType {

    OK((short) 0),

    ERROR((short) 1)
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
