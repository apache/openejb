/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
