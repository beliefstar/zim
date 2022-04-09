package org.zim.server.common.handler;


import org.zim.server.common.CommandProcessor;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:05
 */
public abstract class AbstractCommandHandler implements CommandHandler {
    protected final CommandProcessor commandProcessor;

    public AbstractCommandHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
}
