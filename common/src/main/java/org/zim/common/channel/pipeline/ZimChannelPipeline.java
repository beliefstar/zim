package org.zim.common.channel.pipeline;

import org.zim.common.channel.ZimChannel;

import java.util.concurrent.Executor;

public class ZimChannelPipeline {

    private final ZimChannel channel;

    private final ZimChannelPipelineContext head;
    private final ZimChannelPipelineContext tail;

    public ZimChannelPipeline(ZimChannel channel) {
        this.channel = channel;

        head = new HeadPipelineContext(this);
        tail = new ZimChannelPipelineContext(this, "tail");

        head.setNext(tail);
        tail.setPre(head);
    }

    public void fireRegister() {
        head.fireRegister();
    }

    public void fireRead(Object command) {
        head.fireRead(command);
    }

    public void fireWrite(Object msg) {
        tail.fireWrite(msg);
    }

    public void fireClose() {
        tail.close();
    }

    public void fireExceptionCaught(Throwable cause) {
        head.fireExceptionCaught(cause);
    }

    public synchronized ZimChannelPipeline addLast(ZimChannelHandler handler) {
        return addLast(null, handler);
    }

    public synchronized ZimChannelPipeline addLast(Executor executor, ZimChannelHandler handler) {
        ZimChannelPipelineContext context = new ZimChannelPipelineContext(this, null, handler, executor);

        ZimChannelPipelineContext pre = tail.getPre();
        pre.setNext(context);
        tail.setPre(context);

        context.setPre(pre);
        context.setNext(tail);

        return this;
    }

    public synchronized ZimChannelPipeline addLast(Executor executor, ZimChannelHandler... handlers) {
        for (ZimChannelHandler handler : handlers) {
            addLast(executor, handler);
        }
        return this;
    }

    public ZimChannel channel() {
        return channel;
    }

    static final class HeadPipelineContext extends ZimChannelPipelineContext implements ZimChannelHandler {

        final ZimChannel.Unsafe unsafe;

        public HeadPipelineContext(ZimChannelPipeline pipeline) {
            super(pipeline, "head");
            unsafe = pipeline.channel().unsafe();
        }

        @Override
        protected ZimChannelHandler handler() {
            return this;
        }

        @Override
        public void handleWrite(ZimChannelPipelineContext ctx, Object msg) {
            unsafe.write(msg);
        }

        @Override
        public void handleClose(ZimChannelPipelineContext ctx) {
            unsafe.close();
        }
    }
}
