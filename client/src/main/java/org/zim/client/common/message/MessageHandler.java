package org.zim.client.common.message;

import org.zim.protocol.RemoteCommand;

public interface MessageHandler {

    void consumeMessage(RemoteCommand response);
}
