package org.zim.common.channel.impl;



import org.zim.common.channel.AbstractZimChannel;
import org.zim.common.channel.UnCompleteException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
    public void read() throws IOException {
        ByteBuffer buffer = this.readBuffer;

        buffer.clear();
        channel.read(buffer);
        buffer.flip();

        handleRead(buffer);
    }

    private void handleRead(ByteBuffer buffer) throws IOException {
        ByteBuffer nextFirst;
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
            nextFirst = putReadData(buffer);
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

                nextFirst = putReadData(byteBuffer);
            } else {
                nextFirst = putReadData(buffer);
            }
        }

        if (readData.position() == readSize) {
            triggerOnRead(readData);

            readData = null;
            readSize = -1;
            if (nextFirst != null) {
                handleRead(nextFirst);
            }
            return;
        }
        throw new UnCompleteException();
    }

    private ByteBuffer putReadData(ByteBuffer buffer) {
        if (buffer.remaining() > readData.remaining()) {
            byte[] remaining = new byte[readData.remaining()];
            buffer.get(remaining);
            readData.put(remaining);

            if (buffer.hasRemaining()) {
                remaining = new byte[buffer.remaining()];
                buffer.get(remaining);

                return ByteBuffer.wrap(remaining);
            }
        } else {
            readData.put(buffer);
        }
        return null;
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
        triggerOnClose();
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
