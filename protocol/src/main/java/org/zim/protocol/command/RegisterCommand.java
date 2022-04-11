package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

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
