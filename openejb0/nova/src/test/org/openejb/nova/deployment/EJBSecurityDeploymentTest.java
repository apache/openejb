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

import javax.jms.MessageListener;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import java.util.Set;

import org.apache.geronimo.deployment.model.ejb.SecurityIdentity;
import org.apache.geronimo.deployment.model.geronimo.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.j2ee.Principal;
import org.apache.geronimo.deployment.model.geronimo.j2ee.Realm;
import org.apache.geronimo.deployment.model.geronimo.j2ee.Role;
import org.apache.geronimo.deployment.model.geronimo.j2ee.RoleMappings;
import org.apache.geronimo.deployment.model.geronimo.j2ee.Security;
import org.apache.geronimo.deployment.model.geronimo.j2ee.BeanSecurity;
import org.apache.geronimo.deployment.model.j2ee.RunAs;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.util.ContextManager;

import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.mdb.mockra.MockActivationSpec;
import org.openejb.nova.security.TestPrincipal;
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
public class EJBSecurityDeploymentTest extends ContextBuilderTest {

    private static final String SESSION_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockSession";
    private static final String MDB_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockMDB";
    private static final String RESOURCE_ADAPTER_NAME = "MockRA";

    private EJBModuleDeploymentPlanner planner;
    private ObjectName ejbObjectName;
    private MBeanMetadata ejbMetadata;
    private EjbJar ejbJar;

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
        ejbJar = new EjbJar();

        Security security = new Security();
        RoleMappings roleMappings = new RoleMappings();
        Role[] roles = new Role[1];
        Realm[] realms = new Realm[1];
        Principal[] principals = new Principal[2];

        principals[0] = new Principal();
        principals[0].setClassName("org.openejb.nova.security.TestPrincipal");
        principals[0].setName("alan");
        principals[1] = new Principal();
        principals[1].setClassName("org.openejb.nova.security.TestPrincipal");
        principals[1].setName("admin");

        realms[0] = new Realm();
        realms[0].setRealmName("FooRealm");
        realms[0].setPrincipal(principals);

        roles[0] = new Role();
        roles[0].setRoleName("Administrator");
        roles[0].setRealm(realms);

        roleMappings.setRole(roles);
        security.setRoleMappings(roleMappings);
        security.setUseContextHandler(true);
        ejbJar.setSecurity(security);
        ejbJar.setModuleName("FooModule");
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

        SecurityIdentity securityIdentity = new SecurityIdentity();
        RunAs runAs = new RunAs();

        runAs.setRoleName("Administrator");
        securityIdentity.setRunAs(runAs);
        session.setSecurityIdentity(securityIdentity);

        BeanSecurity beanSecurity = new BeanSecurity();
        beanSecurity.setUseIdentity(true);
        session.setBeanSecurity(beanSecurity);

        ejbObjectName = ObjectName.getInstance(SESSION_NAME);
        ejbMetadata = new MBeanMetadata(ejbObjectName);
    }


    public void testSessionConfigTranslation() throws Exception {
        buildSession();

        EJBContainerConfiguration config = planner.getSessionConfig((Session) ejb, ejbJar, ejbMetadata, transactionPolicySource);

        assertEquals("Context ID must be set", "FooModule", config.contextId);
        assertTrue("Security interceptor to be used", config.setSecurityInterceptor);
        assertTrue("Policy context handlers are to be used", config.setPolicyContextHandlerDataEJB);
        assertTrue("Bean calls are run under the caller's subject", config.setIdentity);
        assertTrue("Run as subject is filled", config.runAs != null);

        // make sure that the subject was registered
//        ContextManager.getSubjectId(config.runAs);

        Subject subject = config.runAs;

        Set principals = subject.getPrincipals();

        assertTrue("Principals were registered in Subject", principals.size() == 2);
        assertTrue("Contains alan from FooRealm", principals.contains(new RealmPrincipal("FooRealm", new TestPrincipal("alan"))));
        assertTrue("Contains admin from FooRealm", principals.contains(new RealmPrincipal("FooRealm", new TestPrincipal("admin"))));
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

        SecurityIdentity securityIdentity = new SecurityIdentity();
        RunAs runAs = new RunAs();

        runAs.setRoleName("Administrator");
        securityIdentity.setRunAs(runAs);
        messageDriven.setSecurityIdentity(securityIdentity);

        BeanSecurity beanSecurity = new BeanSecurity();
        beanSecurity.setUseIdentity(true);
        messageDriven.setBeanSecurity(beanSecurity);

        ejbObjectName = ObjectName.getInstance(MDB_NAME);
    }


    public void testMDBConfigTranslation() throws Exception {
        buildMDB();

        EJBContainerConfiguration config = planner.getMessageDrivenConfig((MessageDriven) ejb, ejbJar, transactionPolicySource);

        assertEquals("Context ID must be set", "FooModule", config.contextId);
        assertTrue("Security interceptor to be used", config.setSecurityInterceptor);
        assertTrue("Policy context handlers are to be used", config.setPolicyContextHandlerDataEJB);
        assertTrue("Bean calls are run under the caller's subject", config.setIdentity);
        assertTrue("Run as subject is filled", config.runAs != null);

        // make sure that the subject was registered
//        ContextManager.getSubjectId(config.runAs);

        Subject subject = config.runAs;

        Set principals = subject.getPrincipals();

        assertTrue("Principals were registered in Subject", principals.size() == 2);
        assertTrue("Contains alan from FooRealm", principals.contains(new RealmPrincipal("FooRealm", new TestPrincipal("alan"))));
        assertTrue("Contains admin from FooRealm", principals.contains(new RealmPrincipal("FooRealm", new TestPrincipal("admin"))));
    }
}
