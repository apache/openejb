package org.openejb.nova.deployment;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public interface TestObjectMBean {
    Object getEJBHome();
    Object getEJBLocalHome();
    Object getConnectionFactory();
}
