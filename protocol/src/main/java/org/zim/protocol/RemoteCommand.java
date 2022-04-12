package org.zim.protocol;

import lombok.Data;
import org.zim.common.StringChecker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * protocol:
 *
 * +--------+--------------+--------+--------+-----------------------+---------------------+---------------+----------------+
 * | Length | MAGIC_NUMBER | FLAG   | CODE   | EXTEND_FIELD_LENGTH   | EXTEND_FIELD        | BODY_LENGTH   | BODY CONTENT   |
 * | 0x0004 | 0x0004       | 0x0001 | 0x0002 | 0x0004                | EXTEND_FIELD_LENGTH | 0x0004        | BODY_LENGTH    |
 * +--------+--------------+--------+--------+-----------------------+---------------------+---------------+----------------+
 *
 */
@Data
public class RemoteCommand {
    public static final int MAGIC_NUMBER = 8085888;
    public static final byte REQUEST_FLAG = 0;
    public static final byte RESPONSE_FLAG = 1;

    // 0 request/ 1 response
    private byte flag = 0;

    private short code;

    private Map<String, String> extendFields = new HashMap<>();

    private byte[] body;

    public void putExtendField(String key, String val) {
        extendFields.put(key, val);
    }

    public String getExtendField(String key) {
        return extendFields.get(key);
    }


    public void markResponse() {
        flag = RESPONSE_FLAG;
    }

    public static RemoteCommand createResponseCommand() {
        return createResponseCommand(CommandResponseType.ERROR);
    }

    public static RemoteCommand createResponseCommand(CommandResponseType responseType) {
        return createResponseCommand(responseType, null);
    }

    public static RemoteCommand createResponseCommand(CommandResponseType responseType, String msg) {
        RemoteCommand rc = new RemoteCommand();
        rc.setFlag(RESPONSE_FLAG);
        rc.setCode(responseType.getCode());
        if (StringChecker.isNotEmpty(msg)) {
            rc.setBody(msg.getBytes(StandardCharsets.UTF_8));
        }
        else if (StringChecker.isNotEmpty(responseType.getInfo())){
            rc.setBody(responseType.getInfo().getBytes(StandardCharsets.UTF_8));
        }
        return rc;
    }

    public byte[] encode() {
        int bodyLength = body == null ? 0 : body.length;

        byte[] extendFieldsBytes = encodeExtendFields(extendFields);

        int length = 4 + 1 + 2 + 4 + extendFieldsBytes.length + 4 + bodyLength;
        ByteBuffer buffer = ByteBuffer.allocate(4 + length);
        buffer.putInt(length);
        buffer.putInt(MAGIC_NUMBER);
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

    public static RemoteCommand decode(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return decode(buffer);
    }
    public static RemoteCommand decode(ByteBuffer buffer) {
        int magicNumber = buffer.getInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new IllegalArgumentException();
        }
        byte flag = buffer.get();
        short code = buffer.getShort();

        RemoteCommand rc;
        if (flag == REQUEST_FLAG) {
            CommandRequestType commandType = CommandRequestType.valueOf(code);
            if (commandType == null) {
                throw new IllegalArgumentException();
            }
            rc = commandType.getSupplier().get();
        } else {
            rc = new RemoteCommand();
        }

        rc.setFlag(flag);
        rc.setCode(code);

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

    public String getBodyString() {
        if (body == null) {
            return null;
        }
        return new String(body, StandardCharsets.UTF_8);
    }
}
