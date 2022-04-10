package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

import static org.zim.protocol.MessageConstants.*;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:15
 */
public class PrivateChatMessageCommand extends RemoteCommand {


    public PrivateChatMessageCommand() {
        setCode(CommandRequestType.PRIVATE_CHAT_MESSAGE.getCode());
    }

    public void setFrom(long from) {
        putExtendField(FROM, Long.toString(from));
    }

    public void setTo(long to) {
        putExtendField(TO, Long.toString(to));
    }

    public Long getFrom() {
        String v = getExtendField(FROM);
        if (v != null) {
            return Long.parseLong(v);
        }
        return null;
    }

    public Long getTo() {
        String v = getExtendField(TO);
        if (v != null) {
            return Long.parseLong(v);
        }
        return null;
    }

    public void setFromName(String fromName) {
        putExtendField(FROM_NAME, fromName);
    }

    public void setToName(String toName) {
        putExtendField(TO_NAME, toName);
    }

    public String getFromName() {
        return getExtendField(FROM_NAME);
    }

    public String getToName() {
        return getExtendField(TO_NAME);
    }
}
