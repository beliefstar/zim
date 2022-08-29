package org.zim.protocol;

import lombok.Data;
import org.zim.common.StringChecker;
import org.zim.protocol.serializer.SerializerFactory;

import java.io.Serializable;
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
public class RemoteCommand implements Serializable {
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
        return SerializerFactory.getDefault().serialize(this);
    }

    public static RemoteCommand decode(byte[] data) {
        return SerializerFactory.getDefault().deserialize(data);
    }

    public static RemoteCommand decode(ByteBuffer data) {
        return SerializerFactory.getDefault().deserialize(data);
    }

    public String getBodyString() {
        if (body == null) {
            return null;
        }
        return new String(body, StandardCharsets.UTF_8);
    }
}
