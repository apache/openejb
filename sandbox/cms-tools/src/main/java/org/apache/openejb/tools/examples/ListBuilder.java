package org.apache.openejb.tools.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public final class ListBuilder<T> {
    private List<T> internal = new ArrayList<T>();
    private Class<T> clazz;

    private ListBuilder(Class<T> cl) {
        clazz = cl;
    }

    public static <A> ListBuilder<A> newList(Class<A> clazz) {
        return new ListBuilder<A>(clazz);
    }

    public ListBuilder<T> add(T value) {
        internal.add(value);
        return this;
    }

    public List<T> list() {
        return internal;
    }

    @Override
    public String toString() {
        return "ListBuilder{" +
                "type = " + clazz +
                '}';
    }
}
