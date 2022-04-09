package org.zhenxin.zim.client.command;

import lombok.Getter;
import org.zhenxin.zim.client.EchoHelper;
import org.zhenxin.zim.client.command.impl.QueryAllUserCommand;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:13
 */
@Getter
public enum CommandHelper {

    QUERY_ALL_USER("listu", "查看所有在线用户", new QueryAllUserCommand())
    ;

    private final String command;
    private final String desc;
    private final InnerCommand commandHandler;

    CommandHelper(String command, String desc, InnerCommand commandHandler) {
        this.command = command;
        this.desc = desc;
        this.commandHandler = commandHandler;
    }

    public static int fireCommand(String line, SocketChannel sc) {
        Command command = Command.parse(line);
        InnerCommand innerCommand = chooseCommand(command.getName());
        if (innerCommand == null) {
            EchoHelper.print("command [{}] not found");
            return 0;
        }
        try {
            return innerCommand.handleCommand(command.getParameter(), sc);
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
