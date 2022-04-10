package org.zim.client;

import org.zim.client.command.Command;
import org.zim.client.command.CommandHelper;
import org.zim.client.command.impl.RegisterInnerCommand;
import org.zim.common.EchoHelper;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.impl.ZimChannelImpl;
import org.zim.protocol.RemoteCommand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class ClientHandler {

    private ZimChannel channel;
    private Selector selector;
    private volatile Thread selectorThread;
    private volatile boolean registered;
    private final RegisterInnerCommand registerInnerCommand = new RegisterInnerCommand();

    private final String host;
    private final int port;

    private final Object scanWaiter = new Object();

    public ClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        selector = Selector.open();
        connect();

        handleRegister();

        synchronized (scanWaiter) {
            while (!registered) {
                scanWaiter.wait();
            }
        }
        listenScan();
    }

    private void connect() throws IOException {
        SocketChannel sc = SocketChannel.open();
        boolean b = sc.connect(new InetSocketAddress(host, port));
        sc.configureBlocking(false);
        System.out.println("connect: " + b);
        channel = new ZimChannelImpl(sc);
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
                    handleResponse();
                }
                if (key.isValid() && key.isWritable()) {
                    channel.writeRemaining();
                }
                iterator.remove();
            }
        }
    }

    public void handleRegister() throws IOException {
        registerInnerCommand.handleCommand("", channel);
    }

    private void listenScan() {
        Scanner scanner = EchoHelper.getScanner();
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();

            if (CommandHelper.fireCommand(s, channel) < 0) {
                break;
            }
        }
    }

    public void handleResponse() throws IOException {
        ByteBuffer buffer;
        try {
             buffer = channel.read();
        } catch (UnCompleteException e) {
            return;
        } catch (Exception e) {
            channel.close();
            connect();
            return;
        }
        RemoteCommand command = RemoteCommand.decode(buffer.array());

        if (!registered) {
            if (registerInnerCommand.handleCommandResponse(command) == 0) {
                registered = true;

                synchronized (scanWaiter) {
                    scanWaiter.notifyAll();
                }
            }
            return;
        }
        if (Command.CURRENT_COMMAND != null) {
            Command.CURRENT_COMMAND.handleCommandResponse(command);
        }
    }


}
