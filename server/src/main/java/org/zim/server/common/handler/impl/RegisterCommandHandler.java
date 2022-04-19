package org.zim.server.common.handler.impl;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;
import org.zim.server.common.model.ServerClientInfo;
import org.zim.server.common.service.AccountService;

import java.util.List;

import static org.zim.protocol.MessageConstants.USER_ID;

@Slf4j
public class RegisterCommandHandler extends AbstractCommandHandler {

    public RegisterCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        RegisterCommand registerCommand = (RegisterCommand) command;

        ServerClientInfo serverClientInfo = new ServerClientInfo();
        serverClientInfo.setUserId(registerCommand.getUserId());
        serverClientInfo.setUserName(registerCommand.getUserName());
        serverClientInfo.setZimChannel(channel);

        if (CommandRequestType.REGISTER.getCode() == registerCommand.getCode()) {
            return register(serverClientInfo);
        }

        if (CommandRequestType.RENAME.getCode() == registerCommand.getCode()) {
            return rename(serverClientInfo);
        }
        return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "user exist");
    }

    private RemoteCommand register(ServerClientInfo serverClientInfo) {
        AccountService accountService = commandProcessor.getAccountService();

        if (!accountService.register(serverClientInfo)) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "user exist");
        }
        // online broadcast
        broadcastOnline(serverClientInfo);

        serverClientInfo.getZimChannel().closeFuture().addListener(future -> {
            ServerClientInfo info = accountService.queryByChannel(future.channel());
            if (info != null) {
                log.info("user [{}] offline", info.getUserName());
                accountService.removeClient(info);

                // offline broadcast
                broadcastOffline(info);
            }
        });

        List<ServerClientInfo> infos = accountService.queryAllUser();
        RemoteCommand response = RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_OK, JSON.toJSONString(infos));

        response.putExtendField(USER_ID, Long.toString(serverClientInfo.getUserId()));

        log.info("user [{}] online", serverClientInfo.getUserName());
        return response;
    }

    private RemoteCommand rename(ServerClientInfo serverClientInfo) {
        AccountService accountService = commandProcessor.getAccountService();

        ServerClientInfo clientInfo = accountService.queryById(serverClientInfo.getUserId());
        if (clientInfo == null) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "unregistered");
        }

        if (clientInfo.getZimChannel() != serverClientInfo.getZimChannel()) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "client error");
        }

        String oldUserName = clientInfo.getUserName();

        if (oldUserName.equals(serverClientInfo.getUserName())) {
            return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR, "same name");
        }

        if (accountService.rename(serverClientInfo)) {

            broadcastRename(clientInfo, oldUserName);
            return RemoteCommand.createResponseCommand(CommandResponseType.RENAME_OK);
        }

        return RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_ERROR);
    }

    private void broadcastOnline(ServerClientInfo serverClientInfo) {
        RemoteCommand remoteCommand = RemoteCommand.createResponseCommand(CommandResponseType.BROADCAST_ONLINE);
        remoteCommand.putExtendField(MessageConstants.TO, Long.toString(serverClientInfo.getUserId()));
        remoteCommand.putExtendField(MessageConstants.TO_NAME, serverClientInfo.getUserName());

        commandProcessor.getAccountService().broadcast(remoteCommand, serverClientInfo);
    }

    private void broadcastOffline(ServerClientInfo serverClientInfo) {
        RemoteCommand command = RemoteCommand.createResponseCommand(CommandResponseType.BROADCAST_OFFLINE);
        command.putExtendField(MessageConstants.TO, Long.toString(serverClientInfo.getUserId()));
        command.putExtendField(MessageConstants.TO_NAME, serverClientInfo.getUserName());
        commandProcessor.getAccountService().broadcast(command, serverClientInfo);
    }

    private void broadcastRename(ServerClientInfo serverClientInfo, String oldUserName) {
        RemoteCommand command = RemoteCommand.createResponseCommand(CommandResponseType.BROADCAST_RENAME);
        command.putExtendField(MessageConstants.TO, Long.toString(serverClientInfo.getUserId()));
        command.putExtendField(MessageConstants.FROM_NAME, oldUserName);
        command.putExtendField(MessageConstants.TO_NAME, serverClientInfo.getUserName());

        commandProcessor.getAccountService().broadcast(command, serverClientInfo);
    }
}
