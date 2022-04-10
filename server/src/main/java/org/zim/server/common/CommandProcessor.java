package org.zim.server.common;


import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.handler.CommandHandler;
import org.zim.server.common.handler.impl.EchoCommandHandler;
import org.zim.server.common.handler.impl.QueryAllUserCommandHandler;
import org.zim.server.common.handler.impl.RegisterCommandHandler;
import org.zim.server.common.service.AccountService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 15:06
 */
public class CommandProcessor {

    private final Map<Short, CommandHandler> COMMAND_HANDLER_MAP = new HashMap<>();

    private final AccountService accountService = new AccountService();

    public CommandProcessor() {
        init();
    }

    private void init() {
        COMMAND_HANDLER_MAP.put(CommandRequestType.REGISTER.getCode(), new RegisterCommandHandler(this));
        COMMAND_HANDLER_MAP.put(CommandRequestType.QUERY_ALL_USER.getCode(), new QueryAllUserCommandHandler(this));
        COMMAND_HANDLER_MAP.put(CommandRequestType.ECHO.getCode(), new EchoCommandHandler(this));
    }

    public void process(RemoteCommand remoteCommand, ZimChannel zimChannel) {
        CommandHandler commandHandler = COMMAND_HANDLER_MAP.get(remoteCommand.getCode());
        RemoteCommand response;
        try {
            response = commandHandler.handleCommand(remoteCommand, zimChannel);
        } catch (Exception e) {
            e.printStackTrace();
            response = RemoteCommand.createResponseCommand(CommandResponseType.ERROR, e.getMessage());
        }
        zimChannel.write(response.encode());
    }

    public AccountService getAccountService() {
        return accountService;
    }
}
