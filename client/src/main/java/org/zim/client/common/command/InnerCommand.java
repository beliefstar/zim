package org.zim.client.common.command;

import org.zim.client.common.ClientHandler;

public interface InnerCommand {

    void handleCommand(Command command, ClientHandler clientHandler);
}
