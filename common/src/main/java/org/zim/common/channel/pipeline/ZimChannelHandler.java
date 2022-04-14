package org.zim.common.channel.pipeline;

public interface ZimChannelHandler {

    default void handleRegister(ZimChannelPipelineContext ctx) throws Exception {
        ctx.fireRegister();
    }

    default void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        ctx.fireRead(msg);
    }

    default void handleWrite(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        ctx.fireWrite(msg);
    }

    default void handleClose(ZimChannelPipelineContext ctx) throws Exception {
        ctx.close();
    }

    default void handleExceptionCaught(ZimChannelPipelineContext ctx, Throwable e) throws Exception {
        ctx.fireExceptionCaught(e);
    }

}
