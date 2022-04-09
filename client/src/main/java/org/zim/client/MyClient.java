package org.zim.client;


import org.zim.client.command.CommandHelper;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.RegisterCommand;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 17:32
 */
public class MyClient {

    public static void main(String[] args) throws Exception {
        SocketChannel sc = SocketChannel.open();
        boolean b = sc.connect(new InetSocketAddress("127.0.0.1", 7436));
        System.out.println("connect: " + b);


        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("userName:");
            String userName = scanner.nextLine();

            RegisterCommand registerMsg = new RegisterCommand();
            registerMsg.setUserId(System.currentTimeMillis());
            registerMsg.setUserName(userName);

            byte[] encode = registerMsg.encode();
            sc.write(ByteBuffer.wrap(encode));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            sc.read(buffer);
            RemoteCommand response = RemoteCommand.decode(buffer.array());
            if (response.getCode() == CommandResponseType.OK.getCode()) {
                System.out.println("online success! username is [" + userName + "], welcome to zim!");
                break;
            } else {
                EchoHelper.print(new String(response.getBody(), StandardCharsets.UTF_8));
            }
        }

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            if (CommandHelper.fireCommand(s, sc) < 0) {
                break;
            }
        }
        sc.close();
        System.out.println(" === done === ");
    }


}
