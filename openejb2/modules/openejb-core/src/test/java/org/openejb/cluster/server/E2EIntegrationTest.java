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
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.cluster.server;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;
import org.activemq.store.vm.VMPersistenceAdapter;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.openejb.EjbDeployment;
import org.openejb.sfsb.DefaultStatefulEjbContainer;
import org.openejb.StatefulEjbDeployment;
import org.openejb.StatefulEjbDeploymentFactory;
import org.openejb.cache.SimpleInstanceCache;
import org.openejb.cluster.sfsb.ClusteredSFInstanceContextFactory;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.openejb.sfsb.StatefulInstanceContext;
import org.openejb.StatefulEjbContainer;

/**
 * TODO remove this end-to-end test, usefull for lightweight end-to-end testing,
 * when proper integration tests have been implemented.
 * 
 * @version $Revision$ $Date$
 */
public class E2EIntegrationTest extends TestCase {

    private NodeInfo node1;
    private NodeInfo node2;

    public void testBasic() throws Exception {
        node1.start();
        node2.start();

        StatefulInstanceContext ctx = (StatefulInstanceContext) node2.factory.newInstance();
        SFSB bean = (SFSB) ctx.getInstance();
        bean.name = "Name";

        StatefulInstanceContext otherCtx = (StatefulInstanceContext) node1.cache.get(ctx.getId());
        SFSB otherBean = (SFSB) otherCtx.getInstance();

        assertEquals(bean.name, otherBean.name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        String clusterName = "OPENEJB_CLUSTER";
        String clusterUri = "vm://clusterName?marshal=false&broker.persistent=false";

        BrokerContainer broker = new BrokerContainerImpl(clusterName);
        broker.addConnector(clusterUri);
        broker.setPersistenceAdapter(new VMPersistenceAdapter());
        broker.start();

        node1 = setUpNode(clusterName, clusterUri, "node1");
        node2 = setUpNode(clusterName, clusterUri, "node2");
    }

    private NodeInfo setUpNode(String clusterName, String clusterUri, String node) throws Exception {
        DefaultEJBClusterManager manager = new DefaultEJBClusterManager(clusterName,
                clusterUri,
                node,
                "127.0.0.1",
                1234,
                10);
        ClusteredInstanceCache cache = new DefaultClusteredInstanceCache(new SimpleInstanceCache());
        StatefulEjbContainer container = new DefaultStatefulEjbContainer(null, null, false, false, false);

        StatefulEjbDeploymentFactory deploymentFactory = new StatefulEjbDeploymentFactory();
        deploymentFactory.setContainerId("containerId");
        deploymentFactory.setEjbName("test");
        deploymentFactory.setBeanClassName(SFSB.class.getName());
        deploymentFactory.setClassLoader(getClass().getClassLoader());
        deploymentFactory.setEjbContainer(container);
        deploymentFactory.setClusterManager(manager);
        StatefulEjbDeployment deployment = (StatefulEjbDeployment) deploymentFactory.create();

        EJBProxyFactory proxyFactory = new EJBProxyFactory(deployment.getProxyInfo());
        ClusteredSFInstanceContextFactory factory = new ClusteredSFInstanceContextFactory(deployment,
                container,
                proxyFactory
        );
        factory.setClusterManager(manager);
        return new NodeInfo(manager, cache, factory);
    }

    private static class NodeInfo {
        DefaultEJBClusterManager clusterManager;
        ClusteredInstanceCache cache;
        ClusteredSFInstanceContextFactory factory;
        public NodeInfo(DefaultEJBClusterManager clusterManager,
                ClusteredInstanceCache cache,
                ClusteredSFInstanceContextFactory factory) {
            this.clusterManager = clusterManager;
            this.cache = cache;
            this.factory = factory;
        }
        public void start() throws Exception {
            clusterManager.doStart();
            ClusteredEjbDeployment container1 = new MockContainer(cache, factory, "containerId");
            clusterManager.addEJBContainer(container1);
        }
    }

    public static class SFSB implements SessionBean {
        private String name;

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
        }
    }

    private static class MockContainer implements ClusteredEjbDeployment {
        private final ClusteredInstanceCache instanceCache;
        private final ClusteredInstanceContextFactory contextFactory;
        private final String containerID;

        public MockContainer(ClusteredInstanceCache instanceCache, ClusteredInstanceContextFactory contextFactory, String containerID) {
            this.instanceCache = instanceCache;
            this.contextFactory = contextFactory;
            this.containerID = containerID;
        }

        public ClusteredInstanceCache getInstanceCache() {
            return instanceCache;
        }

        public ClusteredInstanceContextFactory getInstanceContextFactory() {
            return contextFactory;
        }

        public String getContainerId() {
            return containerID;
        }

        public String getEjbName() {
            throw new UnsupportedOperationException();
        }

        public EJBHome getEjbHome() {
            throw new UnsupportedOperationException();
        }

        public EJBObject getEjbObject(Object primaryKey) {
            throw new UnsupportedOperationException();
        }

        public EJBLocalHome getEjbLocalHome() {
            throw new UnsupportedOperationException();
        }

        public EJBLocalObject getEjbLocalObject(Object primaryKey) {
            throw new UnsupportedOperationException();
        }

        public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
            throw new UnsupportedOperationException();
        }

        public String[] getJndiNames() {
            throw new UnsupportedOperationException();
        }

        public String[] getLocalJndiNames() {
            throw new UnsupportedOperationException();
        }

        public int getMethodIndex(Method method) {
            throw new UnsupportedOperationException();
        }

        public ClassLoader getClassLoader() {
            throw new UnsupportedOperationException();
        }

        public EjbDeployment getUnmanagedReference() {
            throw new UnsupportedOperationException();
        }

        public InterfaceMethodSignature[] getSignatures() {
            throw new UnsupportedOperationException();
        }

        public ProxyInfo getProxyInfo() {
            throw new UnsupportedOperationException();
        }

        public Subject getDefaultSubject() {
            throw new UnsupportedOperationException();
        }

        public Serializable getHomeTxPolicyConfig() {
            throw new UnsupportedOperationException();
        }

        public Serializable getRemoteTxPolicyConfig() {
            throw new UnsupportedOperationException();
        }

        public InvocationResult invoke(Invocation arg0) throws Throwable {
            throw new UnsupportedOperationException();
        }
    }
}
