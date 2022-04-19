package org.zim.client.common;

import lombok.extern.slf4j.Slf4j;
import org.zim.client.common.command.impl.MessageChatCommand;
import org.zim.client.common.message.MessageConsumer;
import org.zim.client.common.scan.ConsoleScanner;
import org.zim.client.common.scan.pipeline.RegisterHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.model.ClientInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientHandler implements ZimChannelHandler {
    public static ClientHandler INSTANCE;

    private final AtomicBoolean state = new AtomicBoolean(false);
    private ZimChannel channel;

    private final ExecutorService executor;

    private Long userId;
    private String userName;

    public Map<String, ClientInfo> onlineClientInfoMap = new ConcurrentHashMap<>();

    private final RegisterHandler registerHandler;
    private final MessageConsumer messageConsumer;
    private final ConsoleScanner consoleScanner;

    private final MessageChatCommand messageChatCommand = new MessageChatCommand();

    public ClientHandler() {
        ClientHandler.INSTANCE = this;
        registerHandler = new RegisterHandler(this);
        messageConsumer = new MessageConsumer(this);
        consoleScanner = new ConsoleScanner(this);

        executor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            final AtomicInteger count = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("zim-client-handler-exec-" + count.incrementAndGet());
                return t;
            }
        });
    }

    public void listenScan() {
        if (state.compareAndSet(false, true)) {

            registerHandler.echoNotice();
            consoleScanner.listen();
        }
    }

    public ZimChannel getChannel() {
        return channel;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setChannel(ZimChannel channel) {
        this.channel = channel;
        if (registerHandler.isRegistered()) {
            registerHandler.markUnRegistered();
            registerHandler.remoteRegister();
        }
    }

    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public RegisterHandler getRegisterHandler() {
        return registerHandler;
    }

    public MessageChatCommand getMessageChatCommand() {
        return messageChatCommand;
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

    public ZimChannelFuture closeForce() {
        if (!state.compareAndSet(true, false)) {
            throw new RuntimeException("client already closed");
        }

        consoleScanner.close();

        return channel.close().addListener(f -> {
            executor.shutdown();
            EchoHelper.printSystem("退出成功!");
        });
    }

    public boolean isRunning() {
        return state.get();
    }
}
