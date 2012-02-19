package org.apache.openejb.maven.plugin.dd;

import java.net.URL;

public interface Merger<T> {
    T merge(T reference, T toMerge);
    T createEmpty();
    T read(URL url);
}
