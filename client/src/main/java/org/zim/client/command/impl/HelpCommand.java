package org.zim.client.command.impl;

import org.zim.client.command.CommandHelper;
import org.zim.client.command.InnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;

import java.io.IOException;

public class HelpCommand implements InnerCommand {
    @Override
    public int handleCommand(String parameter, ZimChannel channel) throws IOException {
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
