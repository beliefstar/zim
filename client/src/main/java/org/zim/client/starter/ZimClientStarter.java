package org.zim.client.starter;

import org.zim.client.common.ChannelInit;
import org.zim.client.common.ClientHandler;
import org.zim.client.nio.single.Reactor;

public class ZimClientStarter {

    public static void main(String[] args) throws Exception {
        ClientHandler clientHandler = new ClientHandler();

        new Reactor("127.0.0.1", 7436, new ChannelInit(clientHandler, true)).start();

        clientHandler.listenScan();
    }
}
