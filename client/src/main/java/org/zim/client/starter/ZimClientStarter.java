package org.zim.client.starter;

import org.zim.client.nio.single.Reactor;

public class ZimClientStarter {

    public static void main(String[] args) throws Exception {
        new Reactor("127.0.0.1", 7436).start();
        System.out.println(" === done === ");
    }
}
