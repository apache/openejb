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
package org.openejb.deployment.slsb;

import java.util.HashMap;
import java.util.HashSet;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.openejb.ContainerIndex;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.StatelessContainerBuilder;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.EJBProxyReference;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public class BasicStatelessContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private Kernel kernel;
    private GBeanData container;

    public void testCrossClInvocation() throws Throwable {
        EJBProxyReference proxyReference = EJBProxyReference.createRemote(CONTAINER_NAME.getCanonicalName(),
                        true,
                MockHome.class.getName(), MockRemote.class.getName());
        proxyReference.setKernel(kernel);
        proxyReference.setClassLoader(this.getClass().getClassLoader());
        MockHome home = (MockHome) proxyReference.getContent();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
    }

    public void testRemoteInvocation() throws Throwable {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
    }

    public void testLocalInvocation() throws Throwable {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal remote = home.create();
        assertEquals(2, remote.intMethod(1));
        assertEquals(2, remote.intMethod(1));
        remote.remove();
    }

    public void testTimeout() throws Exception {
        MockLocalHome localHome = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = localHome.create();
        local.startTimer();
        Thread.sleep(200L);
        int timeoutCount = local.getTimeoutCount();
        assertEquals(1, timeoutCount);
    }

    public void testRemoteSpeed() throws Throwable {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        MockRemote remote = home.create();
        remote.intMethod(1);
        for (int i = 0; i < 1000; i++) {
            remote.intMethod(1);
        }
    }

    public void testLocalSpeed() throws Throwable {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");

        MockLocal local = home.create();
        Integer integer = new Integer(1);
        local.integerMethod(integer);
        int COUNT = 10000;
        for (int i = 0; i < COUNT; i++) {
            local.integerMethod(integer);
        }

        COUNT = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            local.integerMethod(integer);
        }
        long end = System.currentTimeMillis();
        System.out.println("Per local call w/out security: " + ((end - start) * 1000000.0 / COUNT) + "ns");
    }

/*
    public void testLocalSpeed2() throws Throwable {
        int index = 0;
        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInterfaceType.REMOTE, index, new Object[]{new Integer(1)});
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
*/

    protected void setUp() throws Exception {
        super.setUp();

        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);

        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(CONTAINER_NAME.getCanonicalName());
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicyType getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return TransactionPolicyType.Required;
            }
        });
        builder.setComponentContext(new HashMap());
        container = builder.createConfiguration(CONTAINER_NAME, DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME, DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME, null);

        //start the ejb container
        container.setReferencePattern("Timer", DeploymentHelper.TRANSACTIONALTIMER_NAME);
        start(CONTAINER_NAME, container);

        ObjectName containerIndexname = JMXUtil.getObjectName("geronimo.test:type=ConatainerIndex");
        GBeanData containerIndex = new GBeanData(containerIndexname, ContainerIndex.GBEAN_INFO);
        containerIndex.setReferencePattern("EJBContainers", CONTAINER_NAME);
        kernel.loadGBean(containerIndex, this.getClass().getClassLoader());
        kernel.startGBean(containerIndexname);
    }

    protected void tearDown() throws Exception {
        stop(CONTAINER_NAME);
        kernel.shutdown();
    }

    private void start(ObjectName name, GBeanData instance) throws Exception {
        instance.setName(name);
        kernel.loadGBean(instance, this.getClass().getClassLoader());
        kernel.startGBean(name);
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }
}
