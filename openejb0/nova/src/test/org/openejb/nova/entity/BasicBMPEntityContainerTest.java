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
package org.openejb.nova.entity;

import java.net.URI;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import junit.framework.TestCase;

import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.entity.bmp.BMPEntityContainer;
import org.openejb.nova.util.ServerUtil;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicBMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private MBeanServer mbServer;
    private EntityContainerConfiguration config;
    private BMPEntityContainer container;

    public void testSimpleConfig() throws Throwable {
//        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInvocationType.HOME, ejbClass.getIndex("ejbHomeIntMethod", new Class[]{Integer.TYPE}), new Object[]{new Integer(1)});
//        InvocationResult result = container.invoke(invocation);
//        assertEquals(new Integer(2), result.getResult());
//
//        invocation = new EJBInvocationImpl(EJBInvocationType.HOME, ejbClass.getIndex("ejbFindByPrimaryKey", new Class[]{Object.class}), new Object[]{new Integer(1)});
//        result = container.invoke(invocation);
//        EJBObject ejbObject1 = ((EJBObject) result.getResult());
//        assertEquals(new Integer(1), ejbObject1.getPrimaryKey());
//        assertTrue(ejbObject1.isIdentical(ejbObject1));
//
//        invocation = new EJBInvocationImpl(EJBInvocationType.HOME, ejbClass.getIndex("ejbFindByPrimaryKey", new Class[]{Object.class}), new Object[]{new Integer(2)});
//        result = container.invoke(invocation);
//        EJBObject ejbObject2 = ((EJBObject) result.getResult());
//        assertEquals(new Integer(2), ejbObject2.getPrimaryKey());
//        assertTrue(ejbObject2.isIdentical(ejbObject2));
//
//        assertFalse(ejbObject1.isIdentical(ejbObject2));
//        assertFalse(ejbObject2.isIdentical(ejbObject1));
    }

    public void XtestRemoteInvoke() throws Exception {
        MockHome home = (MockHome) container.getEJBHome();
        assertEquals(2, home.intMethod(1));

        MockRemote remote = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, remote.intMethod(1));
    }

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
    }

    public void testLocalCreate() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        MockLocal local = home.create(new Integer(1));
        assertEquals(new Integer(1), local.getPrimaryKey());
    }

    public void testLocalRemove() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        home.remove(new Integer(1));

        MockLocal local = home.create(new Integer(1));
        local.remove();
    }

    protected void setUp() throws Exception {
        super.setUp();
        mbServer = ServerUtil.newRemoteServer();

        config = new EntityContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockBMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();
        config.pkClassName = Integer.class.getName();

        container = new BMPEntityContainer(config);
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.start();
    }

    protected void tearDown() throws Exception {
        container.stop();
        ServerUtil.stopRemoteServer(mbServer);
        super.tearDown();
    }
}
