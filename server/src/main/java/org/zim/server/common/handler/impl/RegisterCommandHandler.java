package org.zim.server.common.handler.impl;


import com.alibaba.fastjson.JSON;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;
import org.zim.server.common.model.ServerClientInfo;
import org.zim.server.common.service.AccountService;

import java.util.List;

public class RegisterCommandHandler extends AbstractCommandHandler {

    public RegisterCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        RegisterCommand registerCommand = (RegisterCommand) command;
        AccountService accountService = commandProcessor.getAccountService();

        ServerClientInfo serverClientInfo = new ServerClientInfo();
        serverClientInfo.setUserId(registerCommand.getUserId());
        serverClientInfo.setUserName(registerCommand.getUserName());
        serverClientInfo.setZimChannel(channel);

        if (accountService.register(serverClientInfo)) {
            EchoHelper.print("user [{}] online", registerCommand.getUserName());

            accountService.broadcastOnline(serverClientInfo);

            List<ServerClientInfo> infos = accountService.queryAllUser();
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_OK, JSON.toJSONString(infos));
        }
        return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "user exist");
    }
}
