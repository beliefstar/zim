package org.zim.client;

import org.zim.client.command.Command;
import org.zim.client.command.CommandHelper;
import org.zim.client.command.impl.RegisterInnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.common.model.ClientInfo;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler {

    private ZimChannel channel;
    private Selector selector;
    private volatile Thread selectorThread;
    private volatile boolean registered;
    private final RegisterInnerCommand registerInnerCommand = new RegisterInnerCommand();

    private final String host;
    private final int port;

    private Long userId;
    private String userName;

    private final Object scanWaiter = new Object();

    private static ClientHandler instance;

    public Map<String, ClientInfo> onlineClientInfoMap = new HashMap<>();

    public ClientHandler(String host, int port) {
        this.host = host;
        this.port = port;

        instance = this;
    }

    public static ClientHandler getInstance() {
        return instance;
    }

    public void start() throws Exception {
        selector = Selector.open();
        connect();

        listenScan();
    }

    private void connect() throws IOException {
        SocketChannel sc = SocketChannel.open();
        boolean b = sc.connect(new InetSocketAddress(host, port));
        sc.configureBlocking(false);
        System.out.println("connect: " + b);
        channel = new ZimChannelImpl(sc);
        channel.registerListener(new ZimChannelListener() {
            @Override
            public void onRead(ZimChannel channel, ByteBuffer buffer) throws IOException {
                handleResponse(buffer);
            }
        });
        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        if (selectorThread == null) {
            Thread t = new Thread(() -> {
                try {
                    doSelect();
                } catch (IOException e) {
                    e.printStackTrace();
                    selectorThread = null;
                }
            });
            t.start();
            selectorThread = t;
        }
    }

    private void doSelect() throws IOException {
        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isValid() && key.isReadable()) {
                    try {
                        channel.read();
                    } catch (UnCompleteException ignore) {
                    } catch (Exception e) {
                        key.cancel();
                        channel.close();
                        connect();
                    }
                }
                if (key.isValid() && key.isWritable()) {
                    channel.writeRemaining();
                }
                iterator.remove();
            }
        }
    }

    private void listenScan() throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (!registered) {
            EchoHelper.printSystem("userName:");
            if (scanner.hasNextLine()) {
                String s = scanner.nextLine();

                registerInnerCommand.handleCommand(s, channel);
                synchronized (scanWaiter) {
                        scanWaiter.wait();
                }
            }
        }
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();

            if (CommandHelper.fireCommand(s, channel) < 0) {
                break;
            }
        }
    }

    public void handleResponse(ByteBuffer buffer) throws IOException {
        RemoteCommand command = RemoteCommand.decode(buffer.array());

        if (!registered) {
            if (registerInnerCommand.handleCommandResponse(command) == 0) {
                registered = true;
            }
            synchronized (scanWaiter) {
                scanWaiter.notifyAll();
            }
            return;
        }
        if (command.getCode() == CommandResponseType.REGISTER_ERROR.getCode()) {
            registered = false;
            EchoHelper.printSystem("未注册");
            return;
        }
        if (command.isMessageResponse()) {
            MessageHandler.handle(command);
            return;
        }
        if (command.getCode() == CommandResponseType.REGISTER_BROADCAST.getCode()) {
            MessageHandler.handleOnlineNotice(command);
            return;
        }
        if (Command.CURRENT_COMMAND != null) {
            Command.CURRENT_COMMAND.handleCommandResponse(command);
        }
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
