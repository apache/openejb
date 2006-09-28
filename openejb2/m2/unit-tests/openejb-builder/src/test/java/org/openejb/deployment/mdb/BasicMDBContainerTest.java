/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
