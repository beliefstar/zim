package org.zim.common;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static <K, V> Map<K, V> clone(Map<K, V> map) {
        return new HashMap<>(map);
    }
}
