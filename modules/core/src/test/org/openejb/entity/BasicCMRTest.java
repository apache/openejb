/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.entity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.openejb.TransactionDemarcation;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.transaction.TransactionManagerProxy;

import junit.framework.TestCase;
import org.hsqldb.jdbcDataSource;
import org.openejb.MockTransactionManager;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.MethodSignature;
import org.openejb.entity.cmp.CMPConfiguration;
import org.openejb.entity.cmp.CMPEntityContainer;
import org.openejb.entity.cmp.CMPQuery;
import org.openejb.entity.cmp.CMRelation;
import org.openejb.entity.cmp.SimpleCommandFactory;
import org.openejb.persistence.jdbc.Binding;
import org.openejb.persistence.jdbc.binding.IntBinding;
import org.openejb.persistence.jdbc.binding.StringBinding;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;

/**
 * @version $Revision$ $Date$
 */
public class BasicCMRTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private org.openejb.EJBContainerConfiguration config;
    private Kernel kernel;
    private GBeanMBean container;
    private MBeanServer mbServer;

    static {
        new org.hsqldb.jdbcDriver();
    }

    private final jdbcDataSource ds = new jdbcDataSource();
    private ObjectName tmName;

    public void testDummy() {
        // JUnit requires one test
    }

    protected void setUp() throws Exception {
        super.setUp();

        ds.setDatabase(".");
        ds.setUser("sa");
        ds.setPassword("");

        config = new org.openejb.EJBContainerConfiguration();
        //config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockCMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.pkClassName = Integer.class.getName();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }

            public TransactionPolicy getTransactionPolicy(String methodIntf, String methodName, String[] parameterTypes) {
                return ContainerPolicy.Required;
            }
        };

        SimpleCommandFactory persistenceFactory = new SimpleCommandFactory(ds);
        ArrayList queries = new ArrayList();
        MethodSignature signature;

        signature = new MethodSignature("ejbFindByPrimaryKey", new String[]{"java.lang.Object"});
        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
        queries.add(new CMPQuery("Mock", false, signature, false, null));
        signature = new MethodSignature("ejbLoad", new String[]{});
        persistenceFactory.defineQuery(signature, "SELECT ID,VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0), new StringBinding(2, 1)});
        queries.add(new CMPQuery(signature, false, null));
        signature = new MethodSignature("ejbSelectSingleValue", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
        queries.add(new CMPQuery(signature, false, null));
        signature = new MethodSignature("ejbSelectMultiValue", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
        queries.add(new CMPQuery(signature, true, null));
        signature = new MethodSignature("ejbSelectMultiObject", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
        queries.add(new CMPQuery("Mock", true, signature, true, null));

        signature = new MethodSignature("ejbCreate", new String[]{"java.lang.Integer", "java.lang.String"});
        persistenceFactory.defineUpdate(signature, "INSERT INTO MOCK(ID, VALUE) VALUES(?,?)", new Binding[]{new IntBinding(1, 0), new StringBinding(2, 1)});
        signature = new MethodSignature("ejbRemove", new String[0]);
        persistenceFactory.defineUpdate(signature, "DELETE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)});
        signature = new MethodSignature("ejbStore", new String[0]);
        persistenceFactory.defineUpdate(signature, "UPDATE MOCK SET VALUE = ? WHERE ID=?", new Binding[]{new StringBinding(1, 1), new IntBinding(2, 0)});

        CMPConfiguration cmpConfig = new CMPConfiguration();
        cmpConfig.persistenceFactory = persistenceFactory;
        cmpConfig.queries = (CMPQuery[]) queries.toArray(new CMPQuery[0]);
        cmpConfig.cmpFieldNames = new String[]{"id", "value"};
        cmpConfig.relations = new CMRelation[]{};
        cmpConfig.schema = "Mock";


        kernel = new Kernel("BeanManagedPersistenceTest");
        kernel.boot();
        mbServer = kernel.getMBeanServer();

        GBeanMBean transactionManager = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
        transactionManager.setAttribute("Delegate", new MockTransactionManager());
        tmName = JMXUtil.getObjectName("geronimo.test:role=TransactionManager");
        start(tmName, transactionManager);

        container = new GBeanMBean(CMPEntityContainer.GBEAN_INFO);
        container.setAttribute("EJBContainerConfiguration", config);
        container.setAttribute("CMPConfiguration", cmpConfig);
        container.setReferencePatterns("TransactionManager", Collections.singleton(tmName));
        start(CONTAINER_NAME, container);

    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }


    protected void tearDown() throws Exception {
        stop(CONTAINER_NAME);
        stop(tmName);
        kernel.shutdown();
    }

}
