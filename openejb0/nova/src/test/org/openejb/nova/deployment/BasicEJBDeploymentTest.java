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

package org.openejb.nova.deployment;

import java.net.URI;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
//import org.apache.geronimo.naming.java.ContextBuilderTest; //copy now in this directory
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.slsb.MockEJB;
import org.openejb.nova.slsb.MockHome;
import org.openejb.nova.slsb.MockRemote;
import org.openejb.nova.slsb.MockLocalHome;
import org.openejb.nova.slsb.MockLocal;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class BasicEJBDeploymentTest extends ContextBuilderTest {

    private static final String SESSION_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockSession";

    private EJBModuleDeploymentPlanner planner;
    private ObjectName sessionName;
    private ClassSpaceMetadata csMetadata;
    private URI baseURI;

    protected void setUp() throws Exception {
        super.setUp();

        kernel.getMBeanServer().createMBean("org.apache.geronimo.kernel.service.DependencyService2", new ObjectName("geronimo.boot:role=DependencyService2"));

        GeronimoMBeanContext context = new GeronimoMBeanContext(kernel.getMBeanServer(), null, null);

        planner = new EJBModuleDeploymentPlanner();
        planner.setMBeanContext(context);
        buildSession();
        sessionName = ObjectName.getInstance(SESSION_NAME);
        csMetadata = new ClassSpaceMetadata();
        baseURI = new URI("");
    }

    private void buildSession() {
        session.setEJBClass(MockEJB.class.getName());
        session.setEJBName("MockSession");
        session.setTransactionType(TransactionDemarcation.CONTAINER.toString());
        session.setHome(MockHome.class.getName());
        session.setRemote(MockRemote.class.getName());
        session.setLocalHome(MockLocalHome.class.getName());
        session.setLocal(MockLocal.class.getName());
        session.setSessionType("Stateless");
    }


    public void testConfigTranslation() throws Exception {
        EJBContainerConfiguration config = planner.getSessionConfig(session);
        assertTrue("expected config", config != null);
        assertEquals("EJBClass", MockEJB.class.getName(), config.beanClassName);
        //assertEquals("EJBName", "MockSession", config.beanClassName);
        assertEquals("TxDemarcation", TransactionDemarcation.CONTAINER, config.txnDemarcation);
        assertEquals("Home", MockHome.class.getName(), config.homeInterfaceName);
        assertEquals("Remote", MockRemote.class.getName(), config.remoteInterfaceName);
        assertEquals("LocalHome", MockLocalHome.class.getName(), config.localHomeInterfaceName);
        assertEquals("Local", MockLocal.class.getName(), config.localInterfaceName);
        assertTrue("ReadOnlyContext null", null != config.componentContext);
    }

    public void testPlanSession() throws Exception {
        //null is no parent.
        DeploymentPlan plan = planner.planSession(session, null, csMetadata, baseURI);
        assertTrue("plan exists", null != plan);
        plan.execute();
        assertTrue("Expected session container mbean ", kernel.getMBeanServer().isRegistered(sessionName));
    }

}
