package org.zim.reactor.channel.pipeline;


import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipeline;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;

import java.util.concurrent.Executor;

public class DefaultZimChannelPipeline implements ZimChannelPipeline {

    private final ZimChannel channel;

    private final DefaultZimChannelPipelineContext head;
    private final DefaultZimChannelPipelineContext tail;

    public DefaultZimChannelPipeline(ZimChannel channel) {
        this.channel = channel;

        head = new HeadPipelineContext(this);
        tail = new DefaultZimChannelPipelineContext(this, "tail");

        head.setNext(tail);
        tail.setPre(head);
    }

    @Override
    public void fireRegister() {
        head.fireRegister();
    }

    @Override
    public void fireActive() {
        head.fireActive();
    }

    @Override
    public void fireRead(Object command) {
        head.fireRead(command);
    }

    @Override
    public void fireWrite(Object msg) {
        tail.fireWrite(msg);
    }

    @Override
    public void fireClose() {
        tail.close();
    }

    @Override
    public void fireExceptionCaught(Throwable cause) {
        head.fireExceptionCaught(cause);
    }

    @Override
    public ZimChannelPipeline addLast(ZimChannelHandler handler) {
        return addLast0(null, handler);
    }

    @Override
    public ZimChannelPipeline addLast(Executor executor, ZimChannelHandler handler) {
        addLast0(executor, handler);
        return this;
    }

    @Override
    public ZimChannelPipeline addLast(Executor executor, ZimChannelHandler... handlers) {
        for (ZimChannelHandler handler : handlers) {
            addLast0(executor, handler);
        }
        return this;
    }

    private synchronized ZimChannelPipeline addLast0(Executor executor, ZimChannelHandler handler) {
        DefaultZimChannelPipelineContext context = new DefaultZimChannelPipelineContext(this, null, handler, executor);

        DefaultZimChannelPipelineContext pre = tail.getPre();
        pre.setNext(context);
        tail.setPre(context);

        context.setPre(pre);
        context.setNext(tail);

        return this;
    }

    @Override
    public ZimChannel channel() {
        return channel;
    }

    static final class HeadPipelineContext extends DefaultZimChannelPipelineContext implements ZimChannelHandler {

        final ZimChannel.Unsafe unsafe;

        public HeadPipelineContext(DefaultZimChannelPipeline pipeline) {
            super(pipeline, "head");
            unsafe = pipeline.channel().unsafe();
        }

        @Override
        protected ZimChannelHandler handler() {
            return this;
        }

        @Override
        public void handleActive(ZimChannelPipelineContext ctx) throws Exception {
            ctx.fireActive();
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
