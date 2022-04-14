package org.zim.common.channel;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.pipeline.ZimChannelPipeline;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

@Slf4j
public abstract class AbstractZimChannel implements ZimChannel {

    private final SelectableChannel ch;
    private final ZimChannelPipeline pipeline;
    private final Unsafe unsafe;
    private SelectionKey selectionKey;

    protected final CloseFuture closeFuture;

    public AbstractZimChannel(SelectableChannel channel) {
        ch = channel;
        unsafe = newUnsafe();
        closeFuture = new CloseFuture();
        pipeline = new ZimChannelPipeline(this);
    }

    @Override
    public CloseFuture closeFuture() {
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
    public void register(Selector selector) {
        try {
            selectionKey = ch.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);

            pipeline().fireRegister();
        } catch (ClosedChannelException e) {
            close();
        }
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
}
