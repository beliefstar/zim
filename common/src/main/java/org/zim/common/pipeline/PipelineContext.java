package org.zim.common.pipeline;


public class PipelineContext<R> {

    private final PipelineHandler<R> handler;

    private PipelineContext<R> pre;
    private PipelineContext<R> next;

    public PipelineContext(PipelineHandler<R> handler) {
        this(handler, null);
    }

    public PipelineContext(PipelineHandler<R> handler, PipelineContext<R> next) {
        this.handler = handler;
        this.next = next;
    }

    public void fireHandle(R command) {
        next.invokeHandle(command);
    }

    public void invokeHandle(R command) {
        if (handler != null) {
            handler.handle(command, this);
        } else {
            if (next != null) {
                next.invokeHandle(command);
            }
        }
    }

    public void setPre(PipelineContext<R> pre) {
        this.pre = pre;
    }

    public void setNext(PipelineContext<R> next) {
        this.next = next;
    }

    public PipelineContext<R> getPre() {
        return pre;
    }

    public PipelineContext<R> getNext() {
        return next;
    }
}
