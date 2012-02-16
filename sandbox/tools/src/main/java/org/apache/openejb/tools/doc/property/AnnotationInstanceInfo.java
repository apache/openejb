package org.apache.openejb.tools.doc.property;

import java.util.Map;

public class AnnotationInstanceInfo {
    private final Map<String, String> info;
    private final String where;

    public AnnotationInstanceInfo(Map<String, String> info, String where) {
        this.info = info;
        this.where = where;
    }

    public String getWhere() {
        return where;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "AnnotationInstanceInfo{" +
                "info='" + info + '\'' +
                ", where='" + where + '\'' +
            '}';
    }
}
