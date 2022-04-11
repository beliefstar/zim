package org.zim.server.common.handler;


import org.zim.server.common.CommandProcessor;

public abstract class AbstractCommandHandler implements CommandHandler {
    protected final CommandProcessor commandProcessor;

    public AbstractCommandHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
}
