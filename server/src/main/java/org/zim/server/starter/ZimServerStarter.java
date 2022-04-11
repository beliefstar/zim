package org.zim.server.starter;


import org.zim.server.nio.SingleReactor;

import java.io.IOException;

public class ZimServerStarter {

    public static void main(String[] args) throws IOException {
        System.out.println("welcome to zim!\nversion: 1");
        new SingleReactor("127.0.0.1", 7436).start();
    }
}
