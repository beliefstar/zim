package org.zhenxin.zim.common.protocol.command;

import org.zhenxin.zim.common.protocol.CommandRequestType;
import org.zhenxin.zim.common.protocol.RemoteCommand;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/7 17:40
 */
public class RegisterCommand extends RemoteCommand {

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";

    public RegisterCommand() {
        setCode(CommandRequestType.REGISTER.getCode());
    }

    public void setUserId(long userId) {
        putExtendField(USER_ID, Long.toString(userId));
    }

    public void setUserName(String userName) {
        putExtendField(USER_NAME, userName);
    }

    public Long getUserId() {
        String v = getExtendField(USER_ID);
        if (v != null) {
            return Long.parseLong(v);
        }
        return null;
    }

    public String getUserName() {
        return getExtendField(USER_NAME);
    }
}
