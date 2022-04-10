package org.zim.client.command;

import lombok.Getter;
import org.zim.client.command.impl.EchoCommand;
import org.zim.client.command.impl.QueryAllUserCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;

import java.io.IOException;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:13
 */
@Getter
public enum CommandHelper {

    QUERY_ALL_USER("listu", "查看所有在线用户", new QueryAllUserCommand()),
    ECHO          ("echo",  "Echo消息",       new EchoCommand())
    ;

    private final String command;
    private final String desc;
    private final InnerCommand commandHandler;

    CommandHelper(String command, String desc, InnerCommand commandHandler) {
        this.command = command;
        this.desc = desc;
        this.commandHandler = commandHandler;
    }

    public static int fireCommand(String line, ZimChannel channel) {
        Command command = Command.parse(line);
        InnerCommand innerCommand = chooseCommand(command.getName());
        if (innerCommand == null) {
            EchoHelper.print("command [{}] not found", command.getName());
            return 0;
        }
        try {
            Command.CURRENT_COMMAND = innerCommand;
            return innerCommand.handleCommand(command.getParameter(), channel);
        } catch (IOException e) {
            EchoHelper.print("found error: {}", e.getMessage());
            return 0;
        }
    }

    public static InnerCommand chooseCommand(String command) {
        for (CommandHelper value : values()) {
            if (value.getCommand().equals(command)) {
                return value.getCommandHandler();
            }
        }
        return null;
    }

}
