package org.zim.client.common.scan.pipline;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.CommandHelper;
import org.zim.common.pipline.PipLineContext;
import org.zim.common.pipline.PipLineHandler;

public class CommandHandler implements PipLineHandler<String> {

    private final ClientHandler clientHandler;

    public CommandHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void handle(String command, PipLineContext<String> context) {
        CommandHelper.fireCommand(command, clientHandler);
    }
}
