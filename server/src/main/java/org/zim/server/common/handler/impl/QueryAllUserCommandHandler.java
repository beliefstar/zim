package org.zim.server.common.handler.impl;

import com.alibaba.fastjson.JSON;
import org.zim.common.channel.ZimChannel;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;
import org.zim.server.common.CommandProcessor;
import org.zim.server.common.handler.AbstractCommandHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:05
 */
public class QueryAllUserCommandHandler extends AbstractCommandHandler {

    public QueryAllUserCommandHandler(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public RemoteCommand handleCommand(RemoteCommand command, ZimChannel channel) {
        List<String> list = commandProcessor.getAccountService().queryAllUser();
        RemoteCommand response = RegisterCommand.createResponseCommand(CommandResponseType.QUERY_ALL_OK);
        response.setBody(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8));
        return response;
    }
}
