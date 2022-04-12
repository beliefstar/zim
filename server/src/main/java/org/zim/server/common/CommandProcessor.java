package org.zim.server.common;


import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandRequestType;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.handler.CommandHandler;
import org.zim.server.common.handler.impl.*;
import org.zim.server.common.service.AccountService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

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
        COMMAND_HANDLER_MAP.put(CommandRequestType.PRIVATE_CHAT_MESSAGE.getCode(), new PrivateChatMessageCommandHandler(this));
        COMMAND_HANDLER_MAP.put(CommandRequestType.GROUP_CHAT_MESSAGE.getCode(), new GroupChatMessageCommandHandler(this));
    }

    public void process(RemoteCommand remoteCommand, ZimChannel zimChannel) {
        CommandHandler commandHandler = COMMAND_HANDLER_MAP.get(remoteCommand.getCode());
        RemoteCommand response;
        try {
            response = commandHandler.handleCommand(remoteCommand, zimChannel);
        } catch (Exception e) {
            response = RemoteCommand.createResponseCommand(CommandResponseType.ERROR, e.getMessage());
        }
        zimChannel.write(response.encode());
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void handleRead(ByteBuffer buffer, ZimChannel channel) throws IOException {
        byte[] bytes = buffer.array();
        RemoteCommand command = RemoteCommand.decode(bytes);
        process(command, channel);
    }
}
