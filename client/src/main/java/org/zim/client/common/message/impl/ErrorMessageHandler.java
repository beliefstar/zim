package org.zim.client.common.message.impl;

import org.zim.client.common.message.MessageHandler;
import org.zim.common.EchoHelper;
import org.zim.protocol.RemoteCommand;

public class ErrorMessageHandler implements MessageHandler {
    @Override
    public void consumeMessage(RemoteCommand response) {
        EchoHelper.printSystemError(response.getBodyString());
    }
}
