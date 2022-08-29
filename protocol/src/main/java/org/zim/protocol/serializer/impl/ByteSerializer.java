package org.zim.protocol.serializer.impl;

import org.zim.protocol.RemoteCommand;
import org.zim.protocol.RemoteCommandFactory;
import org.zim.protocol.serializer.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ByteSerializer implements Serializer {

    @Override
    public byte[] serialize(RemoteCommand remoteCommand) {
        if (remoteCommand == null) {
            return new byte[0];
        }
        byte[] body = remoteCommand.getBody();
        Map<String, String> extendFields = remoteCommand.getExtendFields();
        byte flag = remoteCommand.getFlag();
        short code = remoteCommand.getCode();

        int bodyLength = body == null ? 0 : body.length;

        byte[] extendFieldsBytes = encodeExtendFields(extendFields);

        // total = MAGIC_NUMBER(4) + flag(1) + code(2) + extendFieldsBytesLen(4) + extendFieldsBytesContent(x) + bodyLen(4) + body(x)
        int length = 4 + 1 + 2 + 4 + extendFieldsBytes.length + 4 + bodyLength;

        ByteBuffer buffer = ByteBuffer.allocate(4 + length);
        buffer.putInt(length);
        buffer.putInt(RemoteCommand.MAGIC_NUMBER);
        buffer.put(flag);
        buffer.putShort(code);
        buffer.putInt(extendFieldsBytes.length);
        if (extendFieldsBytes.length > 0) {
            buffer.put(extendFieldsBytes);
        }
        buffer.putInt(bodyLength);
        if (bodyLength > 0) {
            buffer.put(body);
        }
        return buffer.array();
    }

    private byte[] encodeExtendFields(Map<String, String> extendFields) {
        if (extendFields.isEmpty()) {
            return new byte[0];
        }
        int size = 0;
        for (Map.Entry<String, String> entry : extendFields.entrySet()) {
            int keySize = entry.getKey().getBytes(StandardCharsets.UTF_8).length;
            int valueSize = entry.getValue().getBytes(StandardCharsets.UTF_8).length;
            size += 2 + keySize  + 4 + valueSize;
        }
        ByteBuffer buffer = ByteBuffer.allocate(size);
        byte[] keyBytes;
        byte[] valueBytes;
        for (Map.Entry<String, String> entry : extendFields.entrySet()) {
            keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            valueBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
            buffer.putShort((short) keyBytes.length);
            buffer.put(keyBytes);
            buffer.putInt(valueBytes.length);
            buffer.put(valueBytes);
        }
        return buffer.array();
    }


    @Override
    public RemoteCommand deserialize(byte[] data) {
        return decode(ByteBuffer.wrap(data));
    }

    @Override
    public RemoteCommand deserialize(ByteBuffer data) {
        return decode(data);
    }

    private RemoteCommand decode(ByteBuffer buffer) {
        int magicNumber = buffer.getInt();
        if (magicNumber != RemoteCommand.MAGIC_NUMBER) {
            throw new IllegalArgumentException();
        }
        byte flag = buffer.get();
        short code = buffer.getShort();


        RemoteCommand rc = RemoteCommandFactory.create(flag, code);

        int extendFieldLength = buffer.getInt();
        if (extendFieldLength > 0) {
            byte[] extendFieldBytes = new byte[extendFieldLength];
            buffer.get(extendFieldBytes);
            Map<String, String> extendFields = decodeExtendFields(extendFieldBytes);
            rc.setExtendFields(extendFields);
        }

        int bodyLength = buffer.getInt();
        if (bodyLength > 0) {
            byte[] body = new byte[bodyLength];
            buffer.get(body);
            rc.setBody(body);
        }

        return rc;
    }

    private static Map<String, String> decodeExtendFields(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        Map<String, String> map = new HashMap<>();

        while (buffer.hasRemaining()) {
            short keyLength = buffer.getShort();
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);

            int valueLength = buffer.getInt();
            byte[] valueBytes = new byte[valueLength];
            buffer.get(valueBytes);

            map.put(new String(keyBytes, StandardCharsets.UTF_8), new String(valueBytes, StandardCharsets.UTF_8));
        }

        return map;
    }
}
