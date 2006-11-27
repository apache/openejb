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
package org.openejb.deployment.mdb;

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.MDBContainerBuilder;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public class BasicMDBContainerTest extends TestCase {
    private Kernel kernel;
    private GBeanData container;

    protected void setUp() throws Exception {
        super.setUp();
        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);
        DeploymentHelper.setUpResourceAdapter(kernel);

        MDBContainerBuilder builder = new MDBContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(DeploymentHelper.CONTAINER_NAME.getCanonicalName());
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockEJB.class.getName());
        builder.setEndpointInterfaceName("javax.jms.MessageListener");
        builder.setActivationSpecName(DeploymentHelper.ACTIVATIONSPEC_NAME);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicyType getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return TransactionPolicyType.Required;
            }
        });
        builder.setComponentContext(new HashMap());
        container = builder.createConfiguration();
        container.setName(DeploymentHelper.CONTAINER_NAME);

        //start the ejb container
        container.setReferencePattern("TransactionContextManager", DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME);
        container.setReferencePattern("TrackedConnectionAssociator", DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME);
        container.setReferencePattern("Timer", DeploymentHelper.TRANSACTIONALTIMER_NAME);
        kernel.loadGBean(container, getClass().getClassLoader());
        kernel.startGBean(DeploymentHelper.CONTAINER_NAME);
    }

    protected void tearDown() throws Exception {
        DeploymentHelper.stop(kernel, DeploymentHelper.CONTAINER_NAME);
        DeploymentHelper.stop(kernel, DeploymentHelper.TRANSACTIONMANAGER_NAME);
        DeploymentHelper.stop(kernel, DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME);
        DeploymentHelper.tearDownAdapter(kernel);
        kernel.shutdown();
    }


    public void testMessage() throws Exception {
        // @todo put a wait limit in here... otherwise this can lock a build
        // Wait for 3 messages to arrive..
        System.out.println("Waiting for message 1");
        assertTrue(MockEJB.messageCounter.attempt(10000));
        System.out.println("Waiting for message 2");
        assertTrue(MockEJB.messageCounter.attempt(10000));
        System.out.println("Waiting for message 3");
        assertTrue(MockEJB.messageCounter.attempt(10000));

        System.out.println("Done.");
        assertTrue("Timer should have fired once by now...", MockEJB.timerFired);
    }
}
