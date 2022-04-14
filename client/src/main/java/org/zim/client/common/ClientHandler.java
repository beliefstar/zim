package org.zim.client.common;

import org.zim.client.common.message.MessageConsumer;
import org.zim.client.common.scan.ConsoleScanner;
import org.zim.client.common.scan.pipeline.RegisterHandler;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.RemoteCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements ZimChannelHandler {
    public static ClientHandler INSTANCE;

    private ZimChannel channel;

    private Long userId;
    private String userName;

    public Map<String, ClientInfo> onlineClientInfoMap = new ConcurrentHashMap<>();

    private final RegisterHandler registerHandler;
    private final MessageConsumer messageConsumer;
    private final ConsoleScanner consoleScanner;

    public ClientHandler() {
        ClientHandler.INSTANCE = this;
        registerHandler = new RegisterHandler(this);
        messageConsumer = new MessageConsumer(this);
        consoleScanner = new ConsoleScanner(this);
    }

    public void listenScan() {
        registerHandler.echoNotice();
        consoleScanner.listen();
    }

    public ZimChannel getChannel() {
        return channel;
    }

    @Override
    public void handleRegister(ZimChannelPipelineContext ctx) throws Exception {
        this.channel = ctx.channel();
        if (registerHandler.isRegistered()) {
            registerHandler.markUnRegistered();
            registerHandler.remoteRegister();
        }
    }

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        messageConsumer.handle((RemoteCommand) msg);
    }

    public RegisterHandler getRegisterHandler() {
        return registerHandler;
    }

    public void updateOnlineUser(List<ClientInfo> list) {
        Map<String, ClientInfo> map = new ConcurrentHashMap<>();
        for (ClientInfo info : list) {
            map.put(info.getUserName(), info);
        }
        onlineClientInfoMap = map;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
