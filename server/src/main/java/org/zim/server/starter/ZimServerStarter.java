package org.zim.server.starter;


import org.zim.server.nio.single.Reactor;

import java.io.IOException;

/**
 * 启动器
 */
public class ZimServerStarter {

    public static void main(String[] args) throws IOException {
        System.out.println("welcome to zim!\nversion: 1");
        new Reactor("127.0.0.1", 7436, true).start();
    }
}
