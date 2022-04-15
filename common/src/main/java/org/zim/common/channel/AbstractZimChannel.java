package org.zim.common.channel;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.pipeline.ZimChannelPipeline;
import org.zim.common.reactor.EventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

@Slf4j
public abstract class AbstractZimChannel implements ZimChannel {

    private final SelectableChannel ch;
    private final ZimChannelPipeline pipeline;
    private final Unsafe unsafe;
    private SelectionKey selectionKey;
    private final int interestOps;
    private EventLoop eventLoop;


    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;

    protected final ZimChannelFuture closeFuture;

    public AbstractZimChannel(SelectableChannel channel, int interestOps) {
        this.ch = channel;

        try {
            ch.configureBlocking(false);
        } catch (IOException e) {
            log.error("set channel blocking false error: ", e);
            try {
                ch.close();
            } catch (IOException ignore) {
            }
        }

        this.unsafe = newUnsafe();
        this.closeFuture = new ZimChannelFuture(this);
        this.pipeline = new ZimChannelPipeline(this);
        this.interestOps = interestOps;
    }

    @Override
    public ZimChannelFuture closeFuture() {
        return closeFuture;
    }

    @Override
    public ZimChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public Unsafe unsafe() {
        return unsafe;
    }

    protected abstract Unsafe newUnsafe();

    @Override
    public void register(EventLoop eventLoop, ZimChannelFuture regFuture) {
        this.eventLoop = eventLoop;

        eventLoop.execute(() -> this.doRegister(regFuture));
    }

    @Override
    public void write(Object msg) {
        pipeline.fireWrite(msg);
    }

    @Override
    public void close() {
        pipeline().fireClose();
    }

    public SelectionKey selectionKey() {
        return selectionKey;
    }

    protected SelectableChannel javaChannel() {
        return ch;
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    private void doRegister(ZimChannelFuture regFuture) {
        try {
            selectionKey = ch.register(eventLoop.selector(), this.interestOps, this);

            pipeline().fireRegister();

            regFuture.complete();
        } catch (ClosedChannelException e) {
            close();
            regFuture.failure();
        }
    }

    protected void beginRead() {
        selectionKey().interestOps(this.interestOps);
    }

    @Override
    public SocketAddress localAddress() {
        if (localAddress == null) {
            localAddress = unsafe.localAddress();
        }
        return localAddress;
    }

    @Override
    public SocketAddress remoteAddress() {
        if (remoteAddress == null) {
            remoteAddress = unsafe.remoteAddress();
        }
        return remoteAddress;
    }
}
