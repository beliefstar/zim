package org.zim.common.channel.impl;



import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.AbstractZimChannel;
import org.zim.common.channel.UnCompleteException;
import org.zim.common.channel.ZimChannelFuture;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Slf4j
public class ZimNioChannel extends AbstractZimChannel {

    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer readData;
    private ByteBuffer writeBuffer;

    private int readSize = -1;

    private static SocketChannel newSocket() {
        try {
            return SocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public ZimNioChannel() {
        this(newSocket());
    }

    public ZimNioChannel(SocketChannel channel) {
        super(channel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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

        private ZimChannelFuture connectFuture;

        @Override
        public void connect(SocketAddress socketAddress, ZimChannelFuture future) {
            try {
                boolean connect = javaChannel().connect(socketAddress);

                if (!connect) {
                    selectionKey().interestOps(SelectionKey.OP_CONNECT);
                    connectFuture = future;
                } else {
                    future.complete();
                }
            } catch (IOException e) {
                close();
                future.failure();
            }
        }

        @Override
        public void finishConnect() {
            if (connectFuture != null) {
                boolean connectSuccess = false;
                try {
                    if (javaChannel().finishConnect()) {
                        beginRead();

                        pipeline().fireActive();

                        connectSuccess = true;
                    }
                } catch (IOException e) {
                    close();
                }
                if (connectSuccess) {
                    connectFuture.complete();
                } else {
                    connectFuture.failure();
                }
            }
        }

        @Override
        public SocketAddress localAddress() {
            return null;
        }

        @Override
        public SocketAddress remoteAddress() {
            try {
                return javaChannel().getRemoteAddress();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

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

                closeFuture().complete();
            } catch (IOException e) {
                log.error("close socket error: [{}]", e.getMessage());
                closeFuture().failure();
            }
        }
    }
}
