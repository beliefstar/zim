package org.zim.server.common.handler.impl;


import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.EchoHelper;
import org.zim.server.common.channel.ZimChannel;
import org.zim.server.common.handler.AbstractCommandHandler;
import org.zim.server.common.service.AccountService;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:33
 */
public class RegisterCommandHandler extends AbstractCommandHandler {

    public RegisterCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        RegisterCommand registerCommand = (RegisterCommand) command;
        AccountService accountService = commandProcessor.getAccountService();
        if (accountService.register(registerCommand.getUserId(), registerCommand.getUserName(), channel)) {
            EchoHelper.print("user [{}] online", registerCommand.getUserName());
            return RemoteCommand.createResponseCommand(CommandResponseType.OK);
        }
        return RemoteCommand.createResponseCommand(CommandResponseType.ERROR, "user exist");
    }
}
