/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */

package org.openejb.nova.deployment;

import java.net.URI;

import javax.jms.MessageListener;
import javax.management.ObjectName;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.connector.BootstrapContext;
import org.apache.geronimo.connector.deployment.ConnectorDeploymentPlanner;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.deployment.model.connector.ResourceAdapter;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoResourceAdapter;
import org.apache.geronimo.deployment.model.geronimo.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.service.GeronimoMBean;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.mdb.mockra.MockActivationSpec;
import org.openejb.nova.mdb.mockra.MockResourceAdapter;
import org.openejb.nova.slsb.MockEJB;
import org.openejb.nova.slsb.MockHome;
import org.openejb.nova.slsb.MockLocal;
import org.openejb.nova.slsb.MockLocalHome;
import org.openejb.nova.slsb.MockRemote;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.transaction.TxnPolicy;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class BasicEJBDeploymentTest extends ContextBuilderTest {

    private static final String SESSION_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockSession";
    private static final String MDB_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockMDB";
    private static final String RESOURCE_ADAPTER_NAME="MockRA";
    private static final String BOOTSTRAP_CONTEXT_NAME = "geronimo.test:service=BootstrapContext";

    private EJBModuleDeploymentPlanner planner;
    private ObjectName ejbObjectName;
    private MBeanMetadata ejbMetadata;
    private ClassSpaceMetadata csMetadata;
    private URI baseURI;

    private TransactionPolicySource transactionPolicySource = new TransactionPolicySource() {
        public TxnPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
            return ContainerPolicy.Required;
        }
    };

    protected void setUp() throws Exception {
        setUpKernel();
        kernel.getMBeanServer().createMBean("org.apache.geronimo.kernel.service.DependencyService2", new ObjectName("geronimo.boot:role=DependencyService2"));

        GeronimoMBeanContext context = new GeronimoMBeanContext(kernel.getMBeanServer(), null, null);

        planner = new EJBModuleDeploymentPlanner();
        planner.setMBeanContext(context);
        csMetadata = new ClassSpaceMetadata();
        baseURI = new URI("");
    }

    private void buildSession() throws Exception {
        Session session = new Session();
        ejb = session;
        setUpContext();

        session.setEJBClass(MockEJB.class.getName());
        session.setEJBName("MockSession");
        session.setTransactionType(TransactionDemarcation.CONTAINER.toString());
        session.setHome(MockHome.class.getName());
        session.setRemote(MockRemote.class.getName());
        session.setLocalHome(MockLocalHome.class.getName());
        session.setLocal(MockLocal.class.getName());
        session.setSessionType("Stateless");
        ejbObjectName = ObjectName.getInstance(SESSION_NAME);
        ejbMetadata = new MBeanMetadata(ejbObjectName);
    }


    public void testSessionConfigTranslation() throws Exception {
        buildSession();
        EJBContainerConfiguration config = planner.getSessionConfig((Session)ejb, ejbMetadata, transactionPolicySource);
        assertTrue("expected config", config != null);
        assertEquals("EJBClass", MockEJB.class.getName(), config.beanClassName);
        //assertEquals("EJBName", "MockSession", config.beanClassName);
        assertEquals("TxDemarcation", TransactionDemarcation.CONTAINER, config.txnDemarcation);
        assertEquals("Home", MockHome.class.getName(), config.homeInterfaceName);
        assertEquals("Remote", MockRemote.class.getName(), config.remoteInterfaceName);
        assertEquals("LocalHome", MockLocalHome.class.getName(), config.localHomeInterfaceName);
        assertEquals("Local", MockLocal.class.getName(), config.localInterfaceName);
        assertEquals("MessageEndpoint", null, config.messageEndpointInterfaceName);
        assertTrue("ReadOnlyContext null", null != config.componentContext);
    }

    public void testPlanSession() throws Exception {
        buildSession();
        //null is no parent.
        DeploymentPlan plan = new DeploymentPlan();
        planner.planSession(plan, (Session)ejb, null, csMetadata, baseURI, transactionPolicySource);
        assertTrue("plan exists", null != plan);
        plan.execute();
        assertTrue("Expected session container mbean ", kernel.getMBeanServer().isRegistered(ejbObjectName));
    }


    private void buildMDB() throws Exception {
        MessageDriven messageDriven = new MessageDriven();
        ejb = messageDriven;
        setUpContext();

        messageDriven.setEJBClass(org.openejb.nova.mdb.MockEJB.class.getName());
        messageDriven.setEJBName("MockMDB");
        messageDriven.setTransactionType(TransactionDemarcation.CONTAINER.toString());
        messageDriven.setMessagingType(MessageListener.class.getName());
        ActivationConfig activationConfig = new ActivationConfig();
        activationConfig.setActivationSpecClass(MockActivationSpec.class.getName());
        activationConfig.setResourceAdapterName(RESOURCE_ADAPTER_NAME);
        messageDriven.setActivationConfig(activationConfig);
        ejbObjectName = ObjectName.getInstance(MDB_NAME);
    }


    public void testMDBConfigTranslation() throws Exception {
        buildMDB();
        EJBContainerConfiguration config = planner.getMessageDrivenConfig((MessageDriven)ejb, transactionPolicySource);
        assertTrue("expected config", config != null);
        assertEquals("EJBClass", org.openejb.nova.mdb.MockEJB.class.getName(), config.beanClassName);
        //assertEquals("EJBName", "MockSession", config.beanClassName);
        assertEquals("TxDemarcation", TransactionDemarcation.CONTAINER, config.txnDemarcation);
        assertEquals("Home", null, config.homeInterfaceName);
        assertEquals("Remote", null, config.remoteInterfaceName);
        assertEquals("LocalHome", null, config.localHomeInterfaceName);
        assertEquals("Local", null, config.localInterfaceName);
        assertEquals("MessageEndpoint", MessageListener.class.getName(), config.messageEndpointInterfaceName);
        assertTrue("ReadOnlyContext null", null != config.componentContext);
    }

    public void testPlanMDB() throws Exception {
        deployResourceAdapter();
        buildMDB();
        //null is no parent.
        DeploymentPlan plan = new DeploymentPlan();
        planner.planMessageDriven(plan, (MessageDriven)ejb, null, csMetadata, baseURI, transactionPolicySource);
        assertTrue("plan exists", null != plan);
        assertTrue("Expected plan to be runnable", plan.canRun());
        plan.execute();
        assertTrue("Expected mdb container mbean ", kernel.getMBeanServer().isRegistered(ejbObjectName));
    }

    private void deployResourceAdapter() throws Exception {
        WorkManager workManager = new GeronimoWorkManager();
        BootstrapContext bootstrapContext = new BootstrapContext(workManager, null);
        GeronimoMBeanInfo info = new GeronimoMBeanInfo();
        info.setTargetClass(BootstrapContext.class);
        info.addOperationsDeclaredIn(javax.resource.spi.BootstrapContext.class);
        info.setTarget(bootstrapContext);
        GeronimoMBean mbean = new GeronimoMBean();
        mbean.setMBeanInfo(info);
        kernel.getMBeanServer().registerMBean(mbean, ObjectName.getInstance(BOOTSTRAP_CONTEXT_NAME));
        kernel.getMBeanServer().invoke(ObjectName.getInstance(BOOTSTRAP_CONTEXT_NAME), "start", null, null);

        GeronimoMBeanContext context = new GeronimoMBeanContext(kernel.getMBeanServer(), null, null);
        ConnectorDeploymentPlanner connectorPlanner = new ConnectorDeploymentPlanner();
        connectorPlanner.setMBeanContext(context);
        DeploymentPlan plan = new DeploymentPlan();
        ResourceAdapter ra = new ResourceAdapter();
        ra.setResourceAdapterClass(MockResourceAdapter.class.getName());
        GeronimoResourceAdapter gra = new GeronimoResourceAdapter(ra);
        gra.setName(RESOURCE_ADAPTER_NAME);
        gra.setBootstrapContext(BOOTSTRAP_CONTEXT_NAME);
        connectorPlanner.planResourceAdapter(plan, gra, csMetadata, null, baseURI);
        plan.execute();
        assertTrue("expected ResourceAdapter mbean", kernel.getMBeanServer().isRegistered(ObjectName.getInstance("geronimo.j2ee:J2eeType=ResourceAdapter,name=" + RESOURCE_ADAPTER_NAME)));

    }

}
