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
package org.openejb.nova.slsb;

import java.net.URI;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.common.StopWatch;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import junit.framework.TestCase;

import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.util.ServerUtil;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicStatelessContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private MBeanServer mbServer;
    private EJBContainerConfiguration config;
    private StatelessContainer container;

    public void testRemoteInvocation() throws Throwable {
        MockHome home = (MockHome) container.getEJBHome();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
    }

    public void testLocalInvocation() throws Throwable {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        MockLocal remote = home.create();
        assertEquals(2, remote.intMethod(1));
        assertEquals(2, remote.intMethod(1));
        remote.remove();
    }

    public void testRemoteSpeed() throws Throwable {
        MockHome home = (MockHome) container.getEJBHome();
        MockRemote remote = home.create();
        remote.intMethod(1);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 1000; i++) {
            remote.intMethod(1);
        }
        stopWatch.stop();
        System.out.println("Remote: " + stopWatch.getTime());
    }

    public void XtestLocalSpeed() throws Throwable {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        MockLocal remote = home.create();
        Integer integer = new Integer(1);
        remote.integerMethod(integer);
        for (int i = 0; i < 10000000; i++) {
            remote.integerMethod(integer);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            remote.integerMethod(integer);
        }
        long end = System.currentTimeMillis();
        System.out.println("Local: " + (end - start) / 10);
    }

    public void XtestLocalSpeed2() throws Throwable {
        int index = 0;
        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInvocationType.REMOTE, index, new Object[]{new Integer(1)});
        InvocationResult result = container.invoke(invocation);
        assertEquals(new Integer(2), result.getResult());

        for (int i = 0; i < 1000000; i++) {
            container.invoke(invocation);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            container.invoke(invocation);
        }
        long end = System.currentTimeMillis();
        System.out.println("Local Direct: " + (end - start));
    }

    protected void setUp() throws Exception {
        super.setUp();
        mbServer = ServerUtil.newRemoteServer();

        config = new EJBContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();

        container = new StatelessContainer(config);
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.start();

    }

    protected void tearDown() throws Exception {
        container.stop();
        ServerUtil.stopRemoteServer(mbServer);
        super.tearDown();
    }
}
