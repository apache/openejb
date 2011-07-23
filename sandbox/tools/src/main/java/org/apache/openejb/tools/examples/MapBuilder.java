package org.apache.openejb.tools.examples;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Romain Manni-Bucau
 */
public final class MapBuilder<A, B> {
    private Map<A, B> internal = new HashMap<A, B>();
    private Class<A> kClass;
    private Class<B> vClass;

    private MapBuilder(Class<A> a, Class<B> b) {
        kClass = a;
        vClass = b;
    }

    private MapBuilder(Map<A, B> init) {
        internal = init;
    }

    public static <T1, T2> MapBuilder<T1, T2> newMap(Class<T1> kClass, Class<T2> vClass) {
        return new MapBuilder<T1, T2>(kClass, vClass);
    }

    public static <T1, T2> MapBuilder<T1, T2> newMap(Map<T1, T2> init) {
        return new MapBuilder<T1, T2>(init);
    }

    public MapBuilder<A, B> add(A key, B value) {
        internal.put(key, value);
        return this;
    }

    public Map<A, B> map() {
        return internal;
    }

    @Override
    public String toString() {
        return "MapBuilder{" +
                "key = " + kClass +
                ", value = " + vClass +
                '}';
    }
}
