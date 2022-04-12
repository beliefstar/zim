package org.zim.common;

import org.junit.Test;
import org.zim.common.pipeline.PipelineContext;
import org.zim.common.pipeline.PipelineHandler;
import org.zim.common.pipeline.ZimPipeline;

public class ZimPipelineTest {

    @Test
    public void test() {
        PipelineHandler<String> p1 = new PipelineHandler<String>() {
            @Override
            public void handle(String command, PipelineContext<String> context) {
                System.out.println("p1");
                context.fireHandle(command + "1");
            }
        };
        PipelineHandler<String> p2 = new PipelineHandler<String>() {
            @Override
            public void handle(String command, PipelineContext<String> context) {
                System.out.println("p2");
                context.fireHandle(command + "2");
            }
        };

        ZimPipeline<String> zimPipeline = new ZimPipeline<>();
        zimPipeline
                .addLast(p1)
                .addLast(p2);

        zimPipeline.fireHandle("0");
    }
}
