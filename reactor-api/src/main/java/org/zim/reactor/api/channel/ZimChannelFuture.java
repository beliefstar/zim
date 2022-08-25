package org.zim.reactor.api.channel;

public interface ZimChannelFuture {

    ZimChannelFuture addListener(ZimChannelFutureListener listener);

    void complete();

    void failure();

    ZimChannelFuture sync() throws InterruptedException;

    boolean isDone();

    boolean isSuccess();

    ZimChannel channel();
}
