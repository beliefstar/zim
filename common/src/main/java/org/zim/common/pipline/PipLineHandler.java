package org.zim.common.pipline;

public interface PipLineHandler<R> {

    void handle(R command, PipLineContext<R> context);
}
