package org.openejb.deployment;

import javax.naming.Reference;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.openejb.client.naming.RemoteEJBObjectFactory;
import org.openejb.client.naming.RemoteEJBRefAddr;
import org.openejb.corba.CORBAHandleDelegate;

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

    public Object createHandleDelegateReference() {
        return new CORBAHandleDelegate.HandleDelegateReference();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(RemoteEJBReferenceBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoFactory.addInterface(EJBReferenceBuilder.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
