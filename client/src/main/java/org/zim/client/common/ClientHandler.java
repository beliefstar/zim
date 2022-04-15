package org.zim.client.common;

import org.zim.client.common.command.impl.MessageChatCommand;
import org.zim.client.common.message.MessageConsumer;
import org.zim.client.common.scan.ConsoleScanner;
import org.zim.client.common.scan.pipeline.RegisterHandler;
import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.RemoteCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements ZimChannelHandler {
    public static ClientHandler INSTANCE;

    private final AtomicBoolean state = new AtomicBoolean(false);
    private Runnable closeAction;
    private ZimChannel channel;

    private ExecutorService executor;

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
        state.set(true);

        registerHandler.echoNotice();
        consoleScanner.listen();
    }

    public ZimChannel getChannel() {
        return channel;
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void handleActive(ZimChannelPipelineContext ctx) throws Exception {
        this.channel = ctx.channel();
        if (registerHandler.isRegistered()) {
            registerHandler.markUnRegistered();
            registerHandler.remoteRegister();
        }
        ctx.fireActive();
    }

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        messageConsumer.handle((RemoteCommand) msg);
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

    public void closeAction(Runnable action) {
        closeAction = action;
    }

    public void closeForce() {
        state.set(false);

        consoleScanner.close();
        try {
            channel.close().addListener(f -> executor.shutdown()).sync();
        } catch (InterruptedException e) {
        }
        closeAction.run();
        EchoHelper.printSystem("退出成功!");
    }

    public boolean isRunning() {
        return state.get();
    }
}
