package org.zim.client;

import org.zim.common.EchoHelper;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;

import java.nio.charset.StandardCharsets;

public class MessageHandler {

    public static void handle(RemoteCommand command) {
        String sender = command.getExtendField(MessageConstants.FROM_NAME);
        EchoHelper.printMessage(sender, new String(command.getBody(), StandardCharsets.UTF_8));
    }
}
