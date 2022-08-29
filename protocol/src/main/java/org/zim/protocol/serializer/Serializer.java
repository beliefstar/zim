package org.zim.protocol.serializer;

import org.zim.protocol.RemoteCommand;

import java.nio.ByteBuffer;

public interface Serializer {

    byte[] serialize(RemoteCommand remoteCommand);

    RemoteCommand deserialize(byte[] data);

    RemoteCommand deserialize(ByteBuffer data);
}
