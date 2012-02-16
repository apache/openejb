package org.apache.openejb.tools.doc.property;

public class AnnotationInfo {
    private String title;
    private String description;

    public AnnotationInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "AnnotationInfo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
            '}';
    }
}
