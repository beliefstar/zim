package org.zhenxin.zim.version1.server;

import org.zhenxin.zim.common.CommandProcessor;
import org.zhenxin.zim.common.channel.ZimChannel;
import org.zhenxin.zim.common.protocol.RemoteCommand;
import org.zhenxin.zim.version1.ZimChannelImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/6 17:17
 */
public class ZimServerHandler {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    private final CommandProcessor commandProcessor = new CommandProcessor();

    private final Map<SocketChannel, ZimChannel> channelMap = new ConcurrentHashMap<>();

    public void handleRead(SocketChannel socketChannel) throws Exception {
        ZimChannel zimChannel = channelMap.computeIfAbsent(socketChannel, ZimChannelImpl::new);
        buffer.clear();
        try {
            int read = zimChannel.read(buffer);
            if (read <= 0) {
                return;
            }
        } catch (IOException e) {
            zimChannel.close();
            return;
        }
        byte[] bytes = buffer.array();
        RemoteCommand command = RemoteCommand.decode(bytes);
        commandProcessor.process(command, zimChannel);

        /*
        byte[] bytes = null;

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        Message message = null;
        while (true) {
            int read = socketChannel.read(buffer);
            System.out.println("read = " + read);
            if (read <= 0) {
                break;
            }
            buffer.flip();
            if (bytes == null) {
                bytes = new byte[read];
                buffer.get(bytes, 0, read);
            } else {
                int oldLen = bytes.length;
                byte[] t = new byte[bytes.length + read];
                System.arraycopy(bytes, 0, t, 0, bytes.length);
                bytes = t;
                buffer.get(bytes, oldLen, read);
            }

            try {
                message = messageDecoder.decode(bytes);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("decode error " +  e.getMessage());
                TimeUnit.MILLISECONDS.sleep(100);
                e.printStackTrace();
            }
            buffer.clear();
        }
        if (message == null) {
            return;
        }
        System.out.println(message);
        */
    }
}
