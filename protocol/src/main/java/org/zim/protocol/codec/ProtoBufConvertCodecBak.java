package org.zim.protocol.codec;

import com.google.protobuf.ByteString;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.RemoteCommandFactory;
import org.zim.protocol.protobuf.entity.RemoteCommandProtocol;
import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;

import java.util.HashMap;

public class ProtoBufConvertCodecBak implements ZimChannelHandler {

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        if (msg instanceof RemoteCommandProtocol) {
            RemoteCommandProtocol rcp = (RemoteCommandProtocol) msg;

            byte flag = (byte) rcp.getFlag();
            short code = (short) rcp.getCode();

            RemoteCommand rc = RemoteCommandFactory.create(flag, code);
            if (!rcp.getExtendFieldsMap().isEmpty()) {
                rc.setExtendFields(new HashMap<>(rcp.getExtendFieldsMap()));
            }
            rc.setBody(rcp.getBody().toByteArray());

            ctx.fireRead(rc);
        }
    }

    @Override
    public void handleWrite(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        if (msg instanceof RemoteCommand) {
            RemoteCommand rc = (RemoteCommand) msg;

            RemoteCommandProtocol.Builder protocol = RemoteCommandProtocol.newBuilder()
                    .setFlag(rc.getFlag())
                    .setCode(rc.getCode())
                    .putAllExtendFields(rc.getExtendFields())
                    .setBody(ByteString.copyFrom(rc.getBody()));

            ctx.fireWrite(protocol);
        }
    }
}
