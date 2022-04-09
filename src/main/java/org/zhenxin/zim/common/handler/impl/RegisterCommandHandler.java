package org.zhenxin.zim.common.handler.impl;

import org.zhenxin.zim.client.EchoHelper;
import org.zhenxin.zim.common.CommandProcessor;
import org.zhenxin.zim.common.channel.ZimChannel;
import org.zhenxin.zim.common.handler.AbstractCommandHandler;
import org.zhenxin.zim.common.protocol.CommandResponseType;
import org.zhenxin.zim.common.protocol.RemoteCommand;
import org.zhenxin.zim.common.protocol.command.RegisterCommand;
import org.zhenxin.zim.common.service.AccountService;

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
