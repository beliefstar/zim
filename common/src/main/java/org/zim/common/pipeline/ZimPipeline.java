package org.zim.common.pipeline;

public class ZimPipeline<R> {

    private final PipelineContext<R> head = new PipelineContext<>(null);
    private final PipelineContext<R> tail = new PipelineContext<>(null);

    public ZimPipeline() {
        head.setNext(tail);
        tail.setPre(head);
    }

    public void fireHandle(R command) {
        head.fireHandle(command);
    }

    public synchronized ZimPipeline<R> addLast(PipelineHandler<R> handler) {
        PipelineContext<R> context = new PipelineContext<>(handler);

        PipelineContext<R> pre = tail.getPre();
        pre.setNext(context);
        tail.setPre(context);

        context.setPre(pre);
        context.setNext(tail);

        return this;
    }
}
