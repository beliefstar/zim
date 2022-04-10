package org.zim.client;


/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 17:32
 */
public class MyClient {

    public static void main(String[] args) throws Exception {
        new ClientHandler("127.0.0.1", 7436).start();
        System.out.println(" === done === ");
    }
}
