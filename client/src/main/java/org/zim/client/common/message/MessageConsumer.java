package org.zim.client.common.message;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.command.CommandHelper;
import org.zim.client.common.message.impl.PrivateChatMessageHandler;
import org.zim.common.EchoHelper;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageConsumer {

    private Map<CommandResponseType, MessageHandler> handlerMap;

    private final ClientHandler clientHandler;

    public MessageConsumer(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.init();
    }

    private void init() {
        Map<CommandResponseType, MessageHandler> map = new HashMap<>();

        map.put(CommandResponseType.ECHO_OK, (MessageHandler) CommandHelper.ECHO.getCommandHandler());

        map.put(CommandResponseType.REGISTER_OK, clientHandler.getRegisterHandler());
        map.put(CommandResponseType.REGISTER_ERROR, clientHandler.getRegisterHandler());
        map.put(CommandResponseType.REGISTER_BROADCAST, clientHandler.getRegisterHandler());

        map.put(CommandResponseType.QUERY_ALL_OK, (MessageHandler) CommandHelper.QUERY_ALL_USER.getCommandHandler());

        map.put(CommandResponseType.PRIVATE_CHAT_MSG_SEND_OK, (MessageHandler) CommandHelper.MESSAGE.getCommandHandler());

        map.put(CommandResponseType.PRIVATE_CHAT_MSG, new PrivateChatMessageHandler());

        handlerMap = Collections.unmodifiableMap(map);
    }

    public void handle(RemoteCommand command) {
        CommandResponseType commandResponseType = CommandResponseType.valueOf(command.getCode());
        if (commandResponseType == null) {
            EchoHelper.print("未识别消息码[{}]", command.getCode());
            return;
        }
        MessageHandler handler = handlerMap.get(commandResponseType);
        if (handler != null) {
            handler.consumeMessage(command);
            return;
        }
        // default
        EchoHelper.printSystem("receive: [{}]", new String(command.getBody(), StandardCharsets.UTF_8));
    }
}