package org.zim.protocol.serializer;

import org.zim.protocol.serializer.impl.ByteSerializer;
import org.zim.protocol.serializer.impl.HessianSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    private static final Map<String, Serializer> SERIALIZERS = new HashMap<>();

    private static final String defaultType = "hessian";

    static {
        SERIALIZERS.put("byte", new ByteSerializer());
        SERIALIZERS.put("hessian", new HessianSerializer());
    }

    public static Serializer getDefault() {
        return SERIALIZERS.get(defaultType);
    }

    public static Serializer getInstance(String type) {
        return SERIALIZERS.getOrDefault(type, getDefault());
    }
}
