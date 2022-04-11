package org.zim.server.common.handler;


import org.zim.common.channel.ZimChannel;
import org.zim.protocol.RemoteCommand;

public interface CommandHandler {

    RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel);
}
