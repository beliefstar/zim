package org.zim.client.common.message.impl;

import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;

public class ChatMessageHandler implements MessageHandler {
    @Override
    public void consumeMessage(RemoteCommand command) {
        String sender = command.getExtendField(MessageConstants.FROM_NAME);

        CommandResponseType type = CommandResponseType.valueOf(command.getCode());
        if (type == null) {
            EchoHelper.printSystemError("unknown message [{}]", command);
            return;
        }
        switch (type) {
            case PRIVATE_CHAT_MSG:
                EchoHelper.printMessage(sender, command.getBodyString());
                break;
            case GROUP_CHAT_MSG:
                EchoHelper.printGroupMessage(sender, command.getBodyString());
                break;
            default:
                EchoHelper.printSystemError("unknown message type [{}]", type.getCode());
        }
    }
}
