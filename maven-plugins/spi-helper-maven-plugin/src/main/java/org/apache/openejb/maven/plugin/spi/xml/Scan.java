package org.apache.openejb.maven.plugin.spi.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Scan {
    @XmlElementWrapper(name = "classes")
    @XmlElement(name = "class")
    private Set<String> classname = new HashSet<String>();

    @XmlElementWrapper(name = "packages")
    @XmlElement(name = "package")
    private Set<String> packagename = new HashSet<String>();

    public Set<String> getClassname() {
        return classname;
    }

    public void setClassname(Set<String> classname) {
        this.classname = classname;
    }

    public Set<String> getPackagename() {
        return packagename;
    }

    public void setPackagename(Set<String> packagename) {
        this.packagename = packagename;
    }
}
