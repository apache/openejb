package org.openejb.deployment;

import javax.naming.Reference;

import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.openejb.client.naming.RemoteEJBRefAddr;
import org.openejb.client.naming.RemoteEJBObjectFactory;

/**
 */
public class RemoteEJBReferenceBuilder implements EJBReferenceBuilder {

    public Reference createEJBLocalReference(String objectName, boolean isSession, String localHome, String local) {
        throw new UnsupportedOperationException("Application client cannot have a local ejb ref");
    }

    public Reference createEJBRemoteReference(String objectName, boolean isSession, String home, String remote) {
        RemoteEJBRefAddr addr = new RemoteEJBRefAddr(objectName);
        Reference reference = new Reference(null, addr, RemoteEJBObjectFactory.class.getName(), null);
        return reference;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(RemoteEJBReferenceBuilder.class);
        infoFactory.addInterface(EJBReferenceBuilder.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
