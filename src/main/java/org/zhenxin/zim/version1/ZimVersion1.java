package org.zhenxin.zim.version1;

import org.zhenxin.zim.version1.server.ZimServer;

import java.io.IOException;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 15:20
 */
public class ZimVersion1 {

    public static void main(String[] args) throws IOException {
        System.out.println("welcome to zim!\nversion: 1");
        new ZimServer().start();
    }
}
