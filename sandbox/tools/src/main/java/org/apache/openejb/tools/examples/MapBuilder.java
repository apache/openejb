package org.apache.openejb.tools.examples;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Romain Manni-Bucau
 */
public final class MapBuilder {
    private Map<String, Object> internal = new HashMap<String, Object>();

    private MapBuilder() {
        // no-op
    }

    public static MapBuilder newMap() {
        return new MapBuilder();
    }

    public MapBuilder add(String key, Object value) {
        internal.put(key, value);
        return this;
    }

    public Map<String, Object> map() {
        return internal;
    }
}
