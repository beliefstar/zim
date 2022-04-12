package org.zim.client.common;

import org.zim.client.common.message.MessageConsumer;
import org.zim.client.common.scan.ConsoleScanner;
import org.zim.client.common.scan.pipeline.RegisterHandler;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler {
    public static ClientHandler INSTANCE;

    private ZimChannel channel;

    private Long userId;
    private String userName;

    public Map<String, ClientInfo> onlineClientInfoMap = new HashMap<>();

    private final RegisterHandler registerHandler;
    private final MessageConsumer messageConsumer;
    private final ConsoleScanner consoleScanner;

    public ClientHandler(ZimChannel channel) {
        setChannel(channel);

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

    public void resetChannel(ZimChannel channel) {
        setChannel(channel);
        registerHandler.markUnRegistered();
        registerHandler.remoteRegister();
    }

    public void setChannel(ZimChannel channel) {
        this.channel = channel;
        this.channel.registerListener(new ZimChannelListener() {
            @Override
            public void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {
                handleResponse(buffer);
            }
        });
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

    public void handleResponse(ByteBuffer buffer) {
        RemoteCommand command = RemoteCommand.decode(buffer.array());

        messageConsumer.handle(command);
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
