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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.rmi.PortableRemoteObject;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.EJBContainer;
import org.openejb.deployment.MockTransactionManager;
import org.openejb.deployment.StatelessContainerBuilder;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessClientContainerTest extends TestCase {
    private EJBContainer container;

    public void testMetadata() throws Exception {
        EJBMetaData metaData = container.getEjbHome().getEJBMetaData();
        assertTrue(metaData.isSession());
        assertTrue(metaData.isStatelessSession());
        assertEquals(MockHome.class, metaData.getHomeInterfaceClass());
        assertEquals(MockRemote.class, metaData.getRemoteInterfaceClass());
        EJBHome home = metaData.getEJBHome();
        assertTrue(home instanceof MockHome);
        try {
            PortableRemoteObject.narrow(home, MockHome.class);
        } catch (ClassCastException e) {
            fail("Unable to narrow home interface");
        }
        try {
            metaData.getPrimaryKeyClass();
            fail("Expected EJBException, but no exception was thrown");
        } catch (EJBException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected EJBException, but got " + t.getClass().getName());
        }
    }

    public void testHomeInterface() throws Exception {
        MockHome home = (MockHome) container.getEjbHome();
        assertTrue(home.create() instanceof MockRemote);
        try {
            home.remove(new Integer(1));
            fail("Expected RemoveException, but no exception was thrown");
        } catch (RemoveException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoveException, but got " + t.getClass().getName());
        }
        try {
            home.remove(new Handle() {
                public EJBObject getEJBObject() {
                    return null;
                }
            });
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoteException, but got " + t.getClass().getName());
        }
    }

    public MockEJB mockEJB1;
    public MockEJB mockEJB2;

    public void testRemove() throws Throwable {
        MockLocalHome home = (MockLocalHome) container.getEjbLocalHome();
        final MockLocal mock1 = home.create();
        Thread waiter = new Thread("Waiter") {
            public void run() {
                mockEJB1 = mock1.waitForSecondThread(10000);
            }
        };
        waiter.start();

        MockLocal mock2 = home.create();
        mockEJB2 = mock2.waitForSecondThread(10000);
        waiter.join();

        assertTrue("We should have two different EJB instances", mockEJB1 != mockEJB2);
        assertTrue("ejbCreate should have been called on the first instance", mockEJB1.createCalled);
        assertTrue("ejbCreate should have been called on the second instance", mockEJB2.createCalled);
        assertTrue("ejbRemove should have been called on either instance since the pool size is one",
                mockEJB1.removeCalled || mockEJB2.removeCalled);
    }

    public void testLocalHomeInterface() {
        MockLocalHome localHome = (MockLocalHome) container.getEjbLocalHome();
        try {
            localHome.remove(new Integer(1));
            fail("Expected RemoveException, but no exception was thrown");
        } catch (RemoveException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoveExceptions, but got " + t.getClass().getName());
        }
    }

    public void testObjectInterface() throws Exception {
        MockHome home = (MockHome) container.getEjbHome();
        MockRemote remote = home.create();
        assertTrue(remote.isIdentical(remote));
        assertTrue(remote.isIdentical(home.create()));
        try {
            remote.getPrimaryKey();
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoteException, but got " + t.getClass().getName());
        }
        remote.remove();
    }

    public void testLocalInterface() throws Exception {
        MockLocalHome localHome = (MockLocalHome) container.getEjbLocalHome();
        MockLocal local = localHome.create();
        assertTrue(local.isIdentical(local));
        assertTrue(local.isIdentical(localHome.create()));
        try {
            local.getPrimaryKey();
            fail("Expected EJBException, but no exception was thrown");
        } catch (EJBException e) {
             //OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected EJBException, but got " + t.getClass().getName());
        }
        local.remove();
    }

    public void testInvocation() throws Exception {
        MockHome home = (MockHome) container.getEjbHome();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
        try {
            remote.appException();
            fail("Expected AppException, but no exception was thrown");
        } catch (AppException e) {
            // OK
        }
        try {
            remote.sysException();
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId("MockEJB");
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
        builder.setTransactionContextManager(new TransactionContextManager(new MockTransactionManager(), null));
        builder.setTrackedConnectionAssociator(new ConnectionTrackingCoordinator());
        container = builder.createContainer();
    }
}
