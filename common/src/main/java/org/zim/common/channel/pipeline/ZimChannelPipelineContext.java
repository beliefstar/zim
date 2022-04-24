package org.zim.common.channel.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.zim.common.channel.ZimChannel;
import org.zim.common.reactor.EventLoop;

import java.util.concurrent.Executor;

@Slf4j
public class ZimChannelPipelineContext {

    private final ZimChannelPipeline pipeline;

    private final ZimChannelHandler handler;
    private final Executor executor;

    private final String name;

    private ZimChannelPipelineContext pre;
    private ZimChannelPipelineContext next;

    public ZimChannelPipelineContext(ZimChannelPipeline pipeline) {
        this(pipeline, null);
    }

    public ZimChannelPipelineContext(ZimChannelPipeline pipeline, String name) {
        this(pipeline, name, null);
    }

    public ZimChannelPipelineContext(ZimChannelPipeline pipeline, String name, ZimChannelHandler handler) {
        this(pipeline, name,handler, null);
    }

    public ZimChannelPipelineContext(ZimChannelPipeline pipeline, String name, ZimChannelHandler handler, Executor executor) {
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

    public void fireRead(Object command) {
        if (inEventLoop()) {
            next.invokeRead(command);
        } else {
            executor().execute(() -> next.invokeRead(command));
        }
    }

    public void invokeRead(Object command) {
        if (handler() != null) {
            try {
                handler().handleRead(this, command);
            } catch (Exception e) {
                invokeExceptionCaught(e);
            }
        } else {
            if (next != null) {
                next.invokeRead(command);
            }
        }
    }

    public void fireWrite(Object command) {
        if (inEventLoop()) {
            pre.invokeWrite(command);
        } else {
            executor().execute(() -> pre.invokeWrite(command));
        }
    }

    public void invokeWrite(Object command) {
        if (handler() != null) {
            try {
                handler().handleWrite(this, command);
            } catch (Exception e) {
                notifyHandlerException(e);
            }
        } else {
            if (pre != null) {
                pre.invokeWrite(command);
            }
        }
    }

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

    public void write(Object msg) {
        channel().write(msg);
    }

    public void setPre(ZimChannelPipelineContext pre) {
        this.pre = pre;
    }

    public void setNext(ZimChannelPipelineContext next) {
        this.next = next;
    }

    public ZimChannelPipelineContext getPre() {
        return pre;
    }

    public ZimChannelPipelineContext getNext() {
        return next;
    }

    public ZimChannel channel() {
        return pipeline.channel();
    }

    public ZimChannelPipeline pipeline() {
        return pipeline;
    }

    protected ZimChannelHandler handler() {
        return handler;
    }
}
