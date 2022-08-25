package org.zim.server.common.handler;


import org.zim.protocol.RemoteCommand;
import org.zim.reactor.api.channel.ZimChannel;

public interface CommandHandler {

    RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel);
}
