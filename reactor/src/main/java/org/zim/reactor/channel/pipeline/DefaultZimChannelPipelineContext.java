package org.zim.reactor.channel.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;
import org.zim.reactor.api.eventloop.EventLoop;

import java.util.concurrent.Executor;

@Slf4j
public class DefaultZimChannelPipelineContext implements ZimChannelPipelineContext {

    private final DefaultZimChannelPipeline pipeline;

    private final ZimChannelHandler handler;
    private final Executor executor;

    private final String name;

    private DefaultZimChannelPipelineContext pre;
    private DefaultZimChannelPipelineContext next;

    public DefaultZimChannelPipelineContext(DefaultZimChannelPipeline pipeline) {
        this(pipeline, null);
    }

    public DefaultZimChannelPipelineContext(DefaultZimChannelPipeline pipeline, String name) {
        this(pipeline, name, null);
    }

    public DefaultZimChannelPipelineContext(DefaultZimChannelPipeline pipeline, String name, ZimChannelHandler handler) {
        this(pipeline, name,handler, null);
    }

    public DefaultZimChannelPipelineContext(DefaultZimChannelPipeline pipeline, String name, ZimChannelHandler handler, Executor executor) {
        this.pipeline = pipeline;
        this.handler = handler;
        this.executor = executor;
        this.name = name;
    }

    private Executor executor() {
        if (executor != null) {
            return executor;
        }
        return channel().eventLoop();
    }

    private boolean inEventLoop() {
        Executor executor = executor();
        if (executor instanceof EventLoop) {
            return ((EventLoop) executor).inEventLoop();
        }
        return false;
    }

    @Override
    public void fireRead(Object msg) {
        if (inEventLoop()) {
            next.invokeRead(msg);
        } else {
            executor().execute(() -> next.invokeRead(msg));
        }
    }

    public void invokeRead(Object msg) {
        if (handler() != null) {
            try {
                handler().handleRead(this, msg);
            } catch (Exception e) {
                invokeExceptionCaught(e);
            }
        } else {
            if (next != null) {
                next.invokeRead(msg);
            }
        }
    }

    @Override
    public void fireWrite(Object msg) {
        if (inEventLoop()) {
            pre.invokeWrite(msg);
        } else {
            executor().execute(() -> pre.invokeWrite(msg));
        }
    }

    public void invokeWrite(Object msg) {
        if (handler() != null) {
            try {
                handler().handleWrite(this, msg);
            } catch (Exception e) {
                notifyHandlerException(e);
            }
        } else {
            if (pre != null) {
                pre.invokeWrite(msg);
            }
        }
    }

    @Override
    public void close() {
        if (inEventLoop()) {
            pre.invokeClose();
        } else {
            executor().execute(() -> pre.invokeClose());
        }
    }

    public void invokeClose() {
        if (handler() != null) {
            try {
                handler().handleClose(this);
            } catch (Exception e) {
                notifyHandlerException(e);
            }
        } else {
            if (pre != null) {
                pre.invokeClose();
            }
        }
    }

    @Override
    public void fireRegister() {
        if (inEventLoop()) {
            next.invokeRegister();
        } else {
            executor().execute(() -> next.invokeRegister());
        }
    }

    public void invokeRegister() {
        if (handler() != null) {
            try {
                handler().handleRegister(this);
            } catch (Exception e) {
                invokeExceptionCaught(e);
            }
        } else {
            if (next != null) {
                next.invokeRegister();
            }
        }
    }

    @Override
    public void fireActive() {
        if (inEventLoop()) {
            next.invokeActive();
        } else {
            executor().execute(() -> next.invokeActive());
        }
    }

    public void invokeActive() {
        if (handler() != null) {
            try {
                handler().handleActive(this);
            } catch (Exception e) {
                invokeExceptionCaught(e);
            }
        } else {
            if (next != null) {
                next.invokeActive();
            }
        }
    }

    @Override
    public void fireExceptionCaught(Throwable e) {
        if (inEventLoop()) {
            next.invokeExceptionCaught(e);
        } else {
            executor().execute(() -> next.invokeExceptionCaught(e));
        }
    }

    public void invokeExceptionCaught(Throwable e) {
        if (handler() != null) {
            try {
                handler().handleExceptionCaught(this, e);
            } catch (Exception exception) {
                log.error("caught error: " + exception.getMessage());
            }
        } else {
            if (next != null) {
                next.invokeExceptionCaught(e);
            }
        }
    }

    private void notifyHandlerException(Throwable e) {
        log.error("caught error ", e);
    }

    @Override
    public void write(Object msg) {
        channel().write(msg);
    }

    public void setPre(DefaultZimChannelPipelineContext pre) {
        this.pre = pre;
    }

    public void setNext(DefaultZimChannelPipelineContext next) {
        this.next = next;
    }

    public DefaultZimChannelPipelineContext getPre() {
        return pre;
    }

    public DefaultZimChannelPipelineContext getNext() {
        return next;
    }

    @Override
    public ZimChannel channel() {
        return pipeline.channel();
    }

    @Override
    public DefaultZimChannelPipeline pipeline() {
        return pipeline;
    }

    protected ZimChannelHandler handler() {
        return handler;
    }
}
