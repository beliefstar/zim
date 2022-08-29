package org.zim.protocol.codec;

import org.zim.reactor.api.channel.pipeline.ZimChannelHandler;
import org.zim.reactor.api.channel.pipeline.ZimChannelPipelineContext;

import java.nio.ByteBuffer;

/**
 * 粘包拆包处理
 */
public class LengthByteFrameDecoder implements ZimChannelHandler {

    private ByteBuffer readData;
    private int readSize = -1;

    @Override
    public void handleRead(ZimChannelPipelineContext ctx, Object msg) throws Exception {
        ByteBuffer buffer = (ByteBuffer) msg;

        ByteBuffer nextFirst;
        if (readData == null) {
            nextFirst = handleReadSize(buffer);
        } else {
            if (readSize == -1) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(readData.capacity() + buffer.limit());
                readData.flip();
                byteBuffer.put(readData);
                byteBuffer.put(buffer);

                nextFirst = handleReadSize(byteBuffer);
            } else {
                nextFirst = putReadData(buffer);
            }
        }

        if (readData.position() == readSize) {
            readData.flip();
            ctx.fireRead(readData);

            readData = null;
            readSize = -1;
            if (nextFirst != null) {
                handleRead(ctx, nextFirst);
            }
        }
    }

    private ByteBuffer handleReadSize(ByteBuffer buffer) {
        int size;
        try {
            size = buffer.getInt();
        } catch (Exception e) {
            readData = ByteBuffer.allocate(buffer.limit());
            readData.put(buffer);
            return null;
        }
        readSize = size;
        readData = ByteBuffer.allocate(readSize);
        return putReadData(buffer);
    }

    private ByteBuffer putReadData(ByteBuffer buffer) {
        if (buffer.remaining() > readData.remaining()) {
            byte[] remaining = new byte[readData.remaining()];
            buffer.get(remaining);
            readData.put(remaining);

            if (buffer.hasRemaining()) {
                remaining = new byte[buffer.remaining()];
                buffer.get(remaining);

                return ByteBuffer.wrap(remaining);
            }
        } else {
            readData.put(buffer);
        }
        return null;
    }
}
