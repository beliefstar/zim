package org.zim.protocol.netty;

import com.google.protobuf.ByteString;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.RemoteCommandFactory;
import org.zim.protocol.protobuf.entity.RemoteCommandProtocol;

import java.util.HashMap;

@Slf4j
public class ProtoBufConvertCodec extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RemoteCommandProtocol) {
            RemoteCommandProtocol rcp = (RemoteCommandProtocol) msg;

            byte flag = (byte) rcp.getFlag();
            short code = (short) rcp.getCode();

            RemoteCommand rc = RemoteCommandFactory.create(flag, code);
            if (!rcp.getExtendFieldsMap().isEmpty()) {
                rc.setExtendFields(new HashMap<>(rcp.getExtendFieldsMap()));
            }
            rc.setBody(rcp.getBody().toByteArray());

            ctx.fireChannelRead(rc);
            return;
        }

        super.channelRead(ctx, msg);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RemoteCommand) {
            RemoteCommand rc = (RemoteCommand) msg;

            RemoteCommandProtocol.Builder builder = RemoteCommandProtocol.newBuilder()
                    .setFlag(rc.getFlag())
                    .setCode(rc.getCode())
                    .putAllExtendFields(rc.getExtendFields());
            if (rc.getBody() != null && rc.getBody().length > 0) {
                builder.setBody(ByteString.copyFrom(rc.getBody()));
            }

            ctx.write(builder.build());
            return;
        }

        super.write(ctx, msg, promise);
    }
}
