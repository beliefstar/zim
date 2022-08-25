package org.zim.reactor.channel.impl;

import lombok.extern.slf4j.Slf4j;
import org.zim.reactor.api.channel.ZimChannelFuture;
import org.zim.reactor.channel.AbstractZimChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class ZimNioServerChannel extends AbstractZimChannel {

    private static ServerSocketChannel newSocket() {
        try {
            return ServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public ZimNioServerChannel() {
        super(newSocket(), SelectionKey.OP_ACCEPT);
    }


    @Override
    protected Unsafe newUnsafe() {
        return new ZimNioServerUnsafe();
    }

    protected ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
    }

    private final class ZimNioServerUnsafe implements Unsafe {

        @Override
        public void bind(SocketAddress socketAddress, ZimChannelFuture future) {
            try {
                javaChannel().bind(socketAddress);

                future.complete();
            } catch (IOException e) {
                close();
                future.failure();
            }
        }

        @Override
        public SocketAddress localAddress() {
            try {
                return javaChannel().getLocalAddress();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        @Override
        public SocketAddress remoteAddress() {
            return null;
        }

        @Override
        public void read() {
            try {
                SocketChannel socketChannel = javaChannel().accept();

                pipeline().fireRead(new ZimNioChannel(socketChannel));
            } catch (IOException e) {
                log.error("serverSocketChannel accept error", e);
                close();
            }
        }

        @Override
        public void write(Object msg) {
            // NOOP
        }

        @Override
        public void flush() {
            // NOOP
        }

        @Override
        public void close() {
            try {
                javaChannel().close();
            } catch (IOException e) {
                log.error("close ServerSocketChannel error", e);
            } finally {
                closeFuture().complete();
            }
        }
    }
}
