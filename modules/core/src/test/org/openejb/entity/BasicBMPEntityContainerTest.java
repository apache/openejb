/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.entity;

import java.net.URI;
import java.util.HashSet;
import java.util.Collections;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.openejb.TransactionDemarcation;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.transaction.TransactionManagerProxy;

import junit.framework.TestCase;
import org.openejb.MockTransactionManager;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.MethodSignature;
import org.openejb.entity.bmp.BMPEntityContainer;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicBMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private static final ObjectName TM_NAME = JMXUtil.getObjectName("geronimo.test:role=TransactionManager");
    private static final ObjectName TCA_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    private org.openejb.EJBContainerConfiguration config;
    private Kernel kernel;
    private GBeanMBean container;
    private MBeanServer mbServer;


    public void testSimpleConfig() throws Throwable {
//        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInterfaceType.HOME, ejbClass.getIndex("ejbHomeIntMethod", new Class[]{Integer.TYPE}), new Object[]{new Integer(1)});
//        InvocationResult result = container.invoke(invocation);
//        assertEquals(new Integer(2), result.getResult());
//
//        invocation = new EJBInvocationImpl(EJBInterfaceType.HOME, ejbClass.getIndex("ejbFindByPrimaryKey", new Class[]{Object.class}), new Object[]{new Integer(1)});
//        result = container.invoke(invocation);
//        EJBObject ejbObject1 = ((EJBObject) result.getResult());
//        assertEquals(new Integer(1), ejbObject1.getPrimaryKey());
//        assertTrue(ejbObject1.isIdentical(ejbObject1));
//
//        invocation = new EJBInvocationImpl(EJBInterfaceType.HOME, ejbClass.getIndex("ejbFindByPrimaryKey", new Class[]{Object.class}), new Object[]{new Integer(2)});
//        result = container.invoke(invocation);
//        EJBObject ejbObject2 = ((EJBObject) result.getResult());
//        assertEquals(new Integer(2), ejbObject2.getPrimaryKey());
//        assertTrue(ejbObject2.isIdentical(ejbObject2));
//
//        assertFalse(ejbObject1.isIdentical(ejbObject2));
//        assertFalse(ejbObject2.isIdentical(ejbObject1));
    }

    public void XtestRemoteInvoke() throws Exception {
        MockHome home = (MockHome) mbServer.invoke(CONTAINER_NAME, "getEJBHome", null, null);
        assertEquals(2, home.intMethod(1));

        MockRemote remote = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, remote.intMethod(1));
    }

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);

        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
    }

    public void testLocalCreate() throws Exception {
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);
        MockLocal local = home.create(new Integer(1), null);
        assertEquals(new Integer(1), local.getPrimaryKey());
    }

    public void testLocalRemove() throws Exception {
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);
        home.remove(new Integer(1));

        MockLocal local = home.create(new Integer(1), null);
        local.remove();
    }

    protected void setUp() throws Exception {
        super.setUp();

        config = new org.openejb.EJBContainerConfiguration();
        //config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockBMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.pkClassName = Integer.class.getName();
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }
        };

        kernel = new Kernel("BeanManagedPersistenceTest");
        kernel.boot();
        mbServer = kernel.getMBeanServer();

        GBeanMBean transactionManager = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
        transactionManager.setAttribute("Delegate", new MockTransactionManager());
        start(TM_NAME, transactionManager);

        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(TCA_NAME, trackedConnectionAssociator);

        container = new GBeanMBean(BMPEntityContainer.GBEAN_INFO);
        container.setAttribute("EJBContainerConfiguration", config);
        container.setReferencePatterns("TransactionManager", Collections.singleton(TM_NAME));
        container.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(TCA_NAME));
        start(CONTAINER_NAME, container);
    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }


    protected void tearDown() throws Exception {
        stop(CONTAINER_NAME);
        stop(TCA_NAME);
        stop(TM_NAME);
        kernel.shutdown();
    }
}
