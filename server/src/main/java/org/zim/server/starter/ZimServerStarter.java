package org.zim.server.starter;


import org.zim.common.EchoHelper;
import org.zim.server.common.ChannelInit;
import org.zim.server.nio.single.Reactor;

import java.io.IOException;

/**
 * 启动器
 */
public class ZimServerStarter {

    public static void main(String[] args) throws IOException {
        System.out.println("welcome to zim!\nversion: 1");

        // 单 Reactor
        new Reactor("127.0.0.1", 7436, new ChannelInit(true)).start();

        // 主从 Reactor
//        MasterReactor reactor = new MasterReactor("127.0.0.1", 7436, 5, new ChannelInit(true), new ThreadFactory() {
//            final AtomicInteger count = new AtomicInteger();
//
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread t = new Thread(r);
//                t.setName("zim-server-nio-exec-" + count.incrementAndGet());
//                return t;
//            }
//        });
//
//        reactor.start();
        EchoHelper.print("zim server: waiting accept...");
    }
}
