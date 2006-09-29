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
package org.openejb.deployment.entity;

import java.util.HashMap;
import java.util.HashSet;
import javax.ejb.EJBObject;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.openejb.deployment.BMPContainerBuilder;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public class BasicBMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private Kernel kernel;
    private GBeanData container;


    public void testSimpleConfig() throws Throwable {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        assertEquals(5 + 1, home.intMethod(5));

        EJBObject ejbObject1 = home.findByPrimaryKey(new Integer(1));
        assertEquals(new Integer(1), ejbObject1.getPrimaryKey());
        assertTrue(ejbObject1.isIdentical(ejbObject1));

        EJBObject ejbObject2 = home.findByPrimaryKey(new Integer(2));
        ;
        assertEquals(new Integer(2), ejbObject2.getPrimaryKey());
        assertTrue(ejbObject2.isIdentical(ejbObject2));

        assertFalse(ejbObject1.isIdentical(ejbObject2));
        assertFalse(ejbObject2.isIdentical(ejbObject1));
    }

    public void testRemoteInvoke() throws Exception {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        assertEquals(2, home.intMethod(1));

        MockRemote remote = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, remote.intMethod(1));
    }

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");

        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
    }

    public void testLocalCreate() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = home.create(new Integer(1), null);
        assertEquals(new Integer(1), local.getPrimaryKey());
    }

    public void testTimeout() throws Exception {
        MockLocalHome localHome = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = localHome.create(new Integer(1), null);
        local.startTimer();
        Thread.sleep(400L);
        int timeoutCount = local.getTimeoutCount();
        assertEquals(1, timeoutCount);
    }

    public void testLocalRemove() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        home.remove(new Integer(1));

        MockLocal local = home.create(new Integer(1), null);
        local.remove();
    }

    protected void setUp() throws Exception {
        super.setUp();
        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);

        BMPContainerBuilder builder = new BMPContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(CONTAINER_NAME.getCanonicalName());
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockBMPEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setPrimaryKeyClassName(Integer.class.getName());
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
