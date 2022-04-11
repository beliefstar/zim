package org.zim.client.common.message.impl;

import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;

import java.nio.charset.StandardCharsets;

public class PrivateChatMessageHandler implements MessageHandler {
    @Override
    public void consumeMessage(RemoteCommand command) {
        String sender = command.getExtendField(MessageConstants.FROM_NAME);
        EchoHelper.printMessage(sender, new String(command.getBody(), StandardCharsets.UTF_8));
    }
}
