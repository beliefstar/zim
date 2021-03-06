package org.zim.client.common.command;

import lombok.Getter;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.impl.EchoCommand;
import org.zim.client.common.command.impl.HelpCommand;
import org.zim.client.common.command.impl.QueryAllUserCommand;
import org.zim.client.common.command.impl.RenameCommand;
import org.zim.common.EchoHelper;
import org.zim.common.StringChecker;

@Getter
public enum CommandHelper {

    QUERY_ALL_USER("list",   "查看所有在线用户",         new QueryAllUserCommand()),
    ECHO          ("echo",    "Echo消息",              new EchoCommand()),
    HELP          ("help",    "帮助",                  new HelpCommand()),
    RENAME        ("rename",  "重置昵称",               new RenameCommand()),
    ;

    private final String command;
    private final String desc;
    private final InnerCommand commandHandler;

    CommandHelper(String command, String desc, InnerCommand commandHandler) {
        this.command = command;
        this.desc = desc;
        this.commandHandler = commandHandler;
    }

    public static void fireCommand(String line, ClientHandler clientHandler) {
        Command command = Command.parse(line);
        InnerCommand innerCommand = chooseCommand(command);
        if (innerCommand == null) {
            if (StringChecker.isEmpty(command.getName())) {
                EchoHelper.print("command [{}] not found", command.getName());
                return;
            }
            // maybe message
            clientHandler.getMessageChatCommand().handleCommand(command, clientHandler);
            return;
        }
        innerCommand.handleCommand(command, clientHandler);
    }

    public static InnerCommand chooseCommand(Command command) {
        for (CommandHelper value : values()) {
            if (command.getName().startsWith(value.getCommand())) {
                return value.getCommandHandler();
            }
        }
        return null;
    }

}
