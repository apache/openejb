package org.apache.openejb.tools.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public final class ListBuilder {
    private List<Object> internal = new ArrayList<Object>();

    private ListBuilder() {
        // no-op
    }

    public static ListBuilder newList() {
        return new ListBuilder();
    }

    public ListBuilder add(Object value) {
        internal.add(value);
        return this;
    }

    public List<Object> list() {
        return internal;
    }
}
