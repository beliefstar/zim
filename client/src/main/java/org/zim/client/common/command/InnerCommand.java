package org.zim.client.common.command;

import org.zim.client.common.ClientHandler;

public interface InnerCommand {

    int handleCommand(Command command, ClientHandler clientHandler);
}
