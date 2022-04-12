package org.zim.client.common.scan.pipeline;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.CommandHelper;
import org.zim.common.pipeline.PipelineContext;
import org.zim.common.pipeline.PipelineHandler;

public class CommandHandler implements PipelineHandler<String> {

    private final ClientHandler clientHandler;

    public CommandHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void handle(String command, PipelineContext<String> context) {
        CommandHelper.fireCommand(command, clientHandler);
    }
}
