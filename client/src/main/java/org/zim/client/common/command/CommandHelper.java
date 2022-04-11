package org.zim.client.common.command;

import lombok.Getter;
import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.impl.EchoCommand;
import org.zim.client.common.command.impl.HelpCommand;
import org.zim.client.common.command.impl.MessageChatCommand;
import org.zim.client.common.command.impl.QueryAllUserCommand;
import org.zim.common.EchoHelper;

@Getter
public enum CommandHelper {

    QUERY_ALL_USER("-listu", "查看所有在线用户",        new QueryAllUserCommand()),
    ECHO          ("-echo",  "Echo消息",              new EchoCommand()),
    MESSAGE       (":",      ":[userName] msg 私聊",  new MessageChatCommand()),
    HELP          ("-help",  "帮助",                  new HelpCommand()),
    ;

    private final String command;
    private final String desc;
    private final InnerCommand commandHandler;

    CommandHelper(String command, String desc, InnerCommand commandHandler) {
        this.command = command;
        this.desc = desc;
        this.commandHandler = commandHandler;
    }

    public static int fireCommand(String line, ClientHandler clientHandler) {
        Command command = Command.parse(line);
        InnerCommand innerCommand = chooseCommand(command);
        if (innerCommand == null) {
            EchoHelper.print("command [{}] not found", command.getName());
            return 0;
        }
        Command.CURRENT_COMMAND = innerCommand;
        return innerCommand.handleCommand(command.getParameter(), clientHandler);
    }

    public static InnerCommand chooseCommand(Command command) {
        for (CommandHelper value : values()) {
            if (command.getName().startsWith(value.getCommand())) {
                command.tripHead(value.getCommand());
                return value.getCommandHandler();
            }
        }
        return null;
    }

}
