package org.zim.client.common.command.impl;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.Command;
import org.zim.client.common.command.CommandHelper;
import org.zim.client.common.command.InnerCommand;
import org.zim.common.EchoHelper;

public class HelpCommand implements InnerCommand {
    @Override
    public int handleCommand(Command command, ClientHandler clientHandler) {
        String parameter = command.getParameter();

        CommandHelper[] values = CommandHelper.values();
        if (parameter != null && !parameter.isEmpty()) {
            for (CommandHelper value : values) {
                if (parameter.equals(value.getCommand())) {
                    EchoHelper.print(value.getDesc());
                    return 0;
                }
            }
            EchoHelper.print("command [{}] not found", parameter);
        } else {
            for (CommandHelper value : values) {
                EchoHelper.print("[{}] : {}", value.getCommand(), value.getDesc());
            }
        }
        return 0;
    }
}
