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
package org.apache.openejb.deployment.slsb;

import java.rmi.RemoteException;
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
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.slsb.DefaultStatelessEjbContainer;
import org.apache.openejb.StatelessEjbDeploymentFactory;
import org.apache.openejb.StatelessEjbContainer;

/**
 * @version $Revision$ $Date$
 */
public class StatelessClientContainerTest extends TestCase {
    private RpcEjbDeployment deployment;

    public void testMetadata() throws Exception {
        EJBMetaData metaData = deployment.getEjbHome().getEJBMetaData();
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
        MockHome home = (MockHome) deployment.getEjbHome();
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
        MockLocalHome home = (MockLocalHome) deployment.getEjbLocalHome();
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
        MockLocalHome localHome = (MockLocalHome) deployment.getEjbLocalHome();
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
        MockHome home = (MockHome) deployment.getEjbHome();
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
        MockLocalHome localHome = (MockLocalHome) deployment.getEjbLocalHome();
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
        MockHome home = (MockHome) deployment.getEjbHome();
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

        StatelessEjbContainer container = new DefaultStatelessEjbContainer(new GeronimoTransactionManager(),
                new ConnectionTrackingCoordinator(),
                null,
                null,
                false,
                false,
                false);

        StatelessEjbDeploymentFactory deploymentFactory = new StatelessEjbDeploymentFactory();
        deploymentFactory.setContainerId("MockEjb");
        deploymentFactory.setEjbName("MockEjb");
        deploymentFactory.setHomeInterfaceName(MockHome.class.getName());
        deploymentFactory.setRemoteInterfaceName(MockRemote.class.getName());
        deploymentFactory.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        deploymentFactory.setLocalInterfaceName(MockLocal.class.getName());
        deploymentFactory.setBeanClassName(MockEJB.class.getName());
        deploymentFactory.setClassLoader(getClass().getClassLoader());
        deploymentFactory.setEjbContainer(container);
        deployment = (RpcEjbDeployment) deploymentFactory.create();
    }
}
