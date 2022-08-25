package org.zim.client.starter.netty;

import io.netty.channel.ChannelFuture;
import org.zim.reactor.api.channel.ZimChannel;
import org.zim.reactor.channel.DefaultZimChannelFuture;

public class ChannelFutureAdapter extends DefaultZimChannelFuture {

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
