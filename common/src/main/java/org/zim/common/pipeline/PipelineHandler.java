package org.zim.common.pipeline;

public interface PipelineHandler<R> {

    void handle(R command, PipelineContext<R> context);
}
