package org.zim.protocol.command;


import org.zim.protocol.CommandRequestType;
import org.zim.protocol.RemoteCommand;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:15
 */
public class PrivateChatMessageCommand extends RemoteCommand {

    public static final String FROM = "from";
    public static final String TO = "to";

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
}
