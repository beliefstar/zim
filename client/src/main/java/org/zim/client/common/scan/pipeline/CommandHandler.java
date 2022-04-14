package org.zim.client.common.scan.pipeline;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.CommandHelper;
import org.zim.client.common.scan.ScanHandler;

public class CommandHandler implements ScanHandler {

    private final ClientHandler clientHandler;

    public CommandHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public boolean handle(String command) {
        CommandHelper.fireCommand(command, clientHandler);
        return false;
    }
}
