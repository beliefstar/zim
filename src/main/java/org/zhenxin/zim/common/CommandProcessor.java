package org.zhenxin.zim.common;

import org.zhenxin.zim.common.channel.ZimChannel;
import org.zhenxin.zim.common.handler.CommandHandler;
import org.zhenxin.zim.common.handler.impl.QueryAllUserCommandHandler;
import org.zhenxin.zim.common.handler.impl.RegisterCommandHandler;
import org.zhenxin.zim.common.protocol.CommandRequestType;
import org.zhenxin.zim.common.protocol.CommandResponseType;
import org.zhenxin.zim.common.protocol.RemoteCommand;
import org.zhenxin.zim.common.service.AccountService;

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
