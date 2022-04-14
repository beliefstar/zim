package org.zim.common.channel.impl;



import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.AbstractZimChannel;
import org.zim.common.channel.UnCompleteException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class ZimChannelImpl extends AbstractZimChannel {

    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer readData;
    private ByteBuffer writeBuffer;

    private int readSize = -1;

    public ZimChannelImpl(SocketChannel channel) {
        super(channel);
    }

    private void handleRead(ByteBuffer buffer) throws IOException {
        ByteBuffer nextFirst;
        if (readData == null) {
            nextFirst = handleReadSize(buffer);
        } else {
            if (readSize == -1) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(readData.capacity() + buffer.limit());
                readData.flip();
                byteBuffer.put(readData);
                byteBuffer.put(buffer);

                nextFirst = handleReadSize(byteBuffer);
            } else {
                nextFirst = putReadData(buffer);
            }
        }

        if (readData.position() == readSize) {
            pipeline().fireRead(readData);

            readData = null;
            readSize = -1;
            if (nextFirst != null) {
                handleRead(nextFirst);
            }
            return;
        }
        throw new UnCompleteException();
    }

    private ByteBuffer handleReadSize(ByteBuffer buffer) {
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
        return putReadData(buffer);
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
    public String toString() {
        return "ZimChannelImpl{" +
                "readBuffer=" + readBuffer +
                ", readData=" + readData +
                ", readSize=" + readSize +
                '}';
    }

    @Override
    protected Unsafe newUnsafe() {
        return new ZimUnsafeImpl();
    }

    protected SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }

    private final class ZimUnsafeImpl implements Unsafe {

        @Override
        public void read() {
            ByteBuffer buffer = readBuffer;

            boolean close = false;
            try {
                buffer.clear();
                int read = javaChannel().read(buffer);
                if (read > 0) {
                    buffer.flip();

                    try {
                        handleRead(buffer);
                    } catch (UnCompleteException ignore) {
                    }
                } else {
                    close = true;
                }
            } catch (IOException e) {
                close = true;
            } finally {
                if (close) {
                    close();
                }
            }
        }

        @Override
        public void write(Object msg) {
            if (msg instanceof ByteBuffer) {
                ByteBuffer buffer = ((ByteBuffer) msg);
                synchronized (this) {
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
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void flush() {
            if (writeBuffer != null) {
                synchronized (this) {
                    if (writeBuffer != null && writeBuffer.hasRemaining()) {
                        boolean close = false;
                        try {
                            int write = javaChannel().write(writeBuffer);
                            if (write < 0) {
                                close = true;
                            } else {
                                if (write < writeBuffer.limit()) {
                                    writeBuffer = writeBuffer.slice();
                                } else {
                                    writeBuffer = null;
                                }
                            }
                        } catch (IOException e) {
                            close = true;
                        } finally {
                            if (close) {
                                close();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void close() {
            try {
                selectionKey().cancel();
                javaChannel().close();
            } catch (IOException e) {
                log.error("close socket error: [{}]", e.getMessage());
            } finally {
                closeFuture().closeComplete();
            }
        }
    }
}
