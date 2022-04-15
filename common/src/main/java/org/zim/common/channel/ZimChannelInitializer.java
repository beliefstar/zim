package org.zim.common.channel;

import org.zim.common.channel.pipeline.ZimChannelHandler;
import org.zim.common.channel.pipeline.ZimChannelPipelineContext;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ZimChannelInitializer implements ZimChannelHandler {

    private final Set<ZimChannel> initSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public abstract void init(ZimChannel ch);

    @Override
    public void handleRegister(ZimChannelPipelineContext ctx) {
        ZimChannel channel = ctx.channel();
        if (initSet.add(channel)) {
            try {
                init(channel);

                ctx.pipeline().fireRegister();
            } catch (Exception e) {
                ctx.close();
            }
        } else {
            ctx.fireRegister();
        }
    }

    @Override
    public void handleExceptionCaught(ZimChannelPipelineContext ctx, Throwable e) {
        ctx.close();
    }
}
