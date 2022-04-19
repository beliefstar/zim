package org.zim.common.channel.netty;

import io.netty.channel.ChannelFuture;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelFuture;

public class ChannelFutureAdapter extends ZimChannelFuture {

    public ChannelFutureAdapter(ZimChannel channel, ChannelFuture future) {
        super(channel);
        future.addListener(f -> {
            if (f.isSuccess()) {
                this.complete();
            } else {
                this.failure();
            }
        });
    }

}
