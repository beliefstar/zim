package org.zhenxin.zim.common.handler.impl;

import com.alibaba.fastjson.JSON;
import org.zhenxin.zim.common.CommandProcessor;
import org.zhenxin.zim.common.channel.ZimChannel;
import org.zhenxin.zim.common.handler.AbstractCommandHandler;
import org.zhenxin.zim.common.protocol.RemoteCommand;
import org.zhenxin.zim.common.protocol.command.RegisterCommand;

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
        RemoteCommand response = RegisterCommand.createResponseCommand();
        response.setBody(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8));
        return response;
    }
}
