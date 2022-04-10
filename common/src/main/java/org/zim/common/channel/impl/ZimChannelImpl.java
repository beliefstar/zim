package org.zim.common.channel.impl;



import org.zim.common.channel.AbstractZimChannel;
import org.zim.common.channel.UnCompleteException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:29
 */
public class ZimChannelImpl extends AbstractZimChannel {

    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer readData;
    private ByteBuffer writeBuffer;

    private int readSize = -1;

    public ZimChannelImpl(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public ByteBuffer read() throws IOException {
        ByteBuffer buffer = this.readBuffer;

        buffer.clear();
        int read = channel.read(buffer);
        buffer.flip();

        return handleRead(read, buffer);
    }

    private ByteBuffer handleRead(int read, ByteBuffer buffer) {
        ByteBuffer nextFirst = null;
        if (readData == null) {
            int size;
            try {
                size = buffer.getInt();
            } catch (Exception e) {
                readData = ByteBuffer.allocate(buffer.limit());
                readData.put(buffer);
                throw new UnCompleteException();
            }
            readSize = size;
            readData = ByteBuffer.allocate(readSize);
            readData.put(buffer);
        } else {
            if (readSize == -1) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(readData.capacity() + buffer.limit());
                readData.flip();
                byteBuffer.put(readData);
                byteBuffer.put(buffer);

                int size;
                try {
                    size = byteBuffer.getInt();
                } catch (Exception e) {
                    readData = ByteBuffer.allocate(byteBuffer.limit());
                    readData.put(byteBuffer);
                    throw new UnCompleteException();
                }
                readSize = size;
                readData = ByteBuffer.allocate(readSize);
                readData.put(byteBuffer);
            } else {
                int already = readData.position() + read;
                if (already > readSize) {
                    int overflow = already - readSize;
                    int tailSize = read - overflow;
                    byte[] bytes = new byte[tailSize];
                    buffer.get(bytes);
                    readData.put(bytes);

                    byte[] next = new byte[buffer.remaining()];
                    buffer.get(next);
                    nextFirst = ByteBuffer.wrap(next);
                } else if (already < readSize) {
                    readData.put(buffer);
                    throw new UnCompleteException();
                } else {
                    readData.put(buffer);
                }
            }
        }

        if (readData.position() == readSize) {
            ByteBuffer res = readData;

            readData = null;
            readSize = -1;
            if (nextFirst != null) {
                handleRead(nextFirst.limit(), nextFirst);
            }
            return res;
        }
        throw new UnCompleteException();
    }

    @Override
    public void write(byte[] data) {
        write(ByteBuffer.wrap(data));
    }

    @Override
    public void write(ByteBuffer buffer) {
        synchronized (channel) {
            if (writeBuffer == null) {
                writeBuffer = buffer;
            } else {
                ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.limit() + writeBuffer.limit());
                byteBuffer.put(writeBuffer);
                byteBuffer.put(buffer);
                byteBuffer.flip();
                writeBuffer = byteBuffer;
            }
        }
    }

    @Override
    public void writeRemaining() {
        if (writeBuffer != null) {
            synchronized (channel) {
                if (writeBuffer != null && writeBuffer.hasRemaining()) {
                    try {
                        int write = channel.write(writeBuffer);
                        if (write < writeBuffer.limit()) {
                            writeBuffer = writeBuffer.slice();
                        } else {
                            writeBuffer = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        close();
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        triggerOnClose(this);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ZimChannelImpl{" +
                "readBuffer=" + readBuffer +
                ", readData=" + readData +
                ", readSize=" + readSize +
                '}';
    }
}
