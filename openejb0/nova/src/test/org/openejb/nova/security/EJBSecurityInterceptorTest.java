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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.security;

import javax.ejb.EJBException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import java.net.URI;
import java.security.Policy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.security.GeronimoPolicy;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.RoleMappingConfiguration;
import org.apache.geronimo.security.util.ContextManager;
import junit.framework.TestCase;

import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.deployment.TransactionPolicySource;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.slsb.MockEJB;
import org.openejb.nova.slsb.MockHome;
import org.openejb.nova.slsb.MockLocal;
import org.openejb.nova.slsb.MockLocalHome;
import org.openejb.nova.slsb.MockRemote;
import org.openejb.nova.slsb.StatelessContainer;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.transaction.TxnPolicy;


/**
 *
 * @version $Revision$ $Date$
 */
public class EJBSecurityInterceptorTest extends TestCase {

    private final String CONTEXT_ID = "Foo Deployment Id";
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private StatelessContainer container;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.GeronimoPolicyConfigurationFactory");

        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        Policy.setPolicy(new GeronimoPolicy(factory));

        EJBContainerConfiguration config = new EJBContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.ejbName = "MockSession";
        config.beanClassName = MockEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();
        config.trackedConnectionAssociator = new ConnectionTrackingCoordinator();
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TxnPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }
        };
        config.contextId = CONTEXT_ID;
        config.setSecurityInterceptor = true;

        container = new StatelessContainer(config);
        container.doStart();
    }

    public void tearDown() throws Exception {
        Policy.setPolicy(null);
    }

    public void testEjbName() throws Exception {
        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        PolicyConfiguration configuration = factory.getPolicyConfiguration(CONTEXT_ID, true);
        configuration.addToRole("GoodRole", new EJBMethodPermission("MockSession", "intMethod,Remote,int"));
        configuration.addToRole("BadRole", new EJBMethodPermission("MockSession", "intMethod,Local,int"));
        ((RoleMappingConfiguration) configuration).addRoleMapping("GoodRole", Collections.singletonList(new RealmPrincipal("Oz", new TestPrincipal("Wizard"))));
        ((RoleMappingConfiguration) configuration).addRoleMapping("BadRole", Collections.singletonList(new RealmPrincipal("Oz", new TestPrincipal("Witch"))));
        configuration.commit();

        Subject goodSubject = new Subject();
        goodSubject.getPrincipals().add(new RealmPrincipal("Oz", new TestPrincipal("Wizard")));
        goodSubject.setReadOnly();
        ContextManager.registerSubject(goodSubject);

        Subject badSubject = new Subject();
        badSubject.getPrincipals().add(new RealmPrincipal("Oz", new TestPrincipal("Witch")));
        badSubject.setReadOnly();
        ContextManager.registerSubject(badSubject);

        ContextManager.setNextCaller(goodSubject);
        ContextManager.setCurrentCaller(goodSubject);

        MockHome home = (MockHome) container.getEJBHome();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));

        ContextManager.setNextCaller(badSubject);
        try {
            remote.intMethod(1);
            fail("Should have thrown a exception");
        } catch (EJBException ee) {
        }

        MockLocalHome localHome = (MockLocalHome) container.getEJBLocalHome();
        MockLocal local = localHome.create();

        ContextManager.setNextCaller(goodSubject);
        try {
            local.intMethod(1);
            fail("Should have thrown a exception");
        } catch (EJBException ee) {
        }

        ContextManager.setNextCaller(badSubject);
        local.intMethod(1);

        Hashtable test = new Hashtable();
        test.put(badSubject, badSubject);
        final int COUNT = 100000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            local.intMethod(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Per call: " + ((end - start) * 1000000.0 / COUNT) + "ns YUCK!");

        ContextManager.unregisterSubject(goodSubject);
        ContextManager.unregisterSubject(badSubject);
    }
}
