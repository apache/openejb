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

package org.openejb.deployment;

import javax.jms.MessageListener;
import javax.management.ObjectName;

import org.openejb.TransactionDemarcation;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.xbeans.j2ee.SecurityIdentityType;
import org.apache.geronimo.xbeans.j2ee.RunAsType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigType;
import org.openejb.dispatch.MethodSignature;
import org.openejb.mdb.mockra.MockActivationSpec;
import org.openejb.slsb.MockEJB;
import org.openejb.slsb.MockHome;
import org.openejb.slsb.MockLocal;
import org.openejb.slsb.MockLocalHome;
import org.openejb.slsb.MockRemote;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;


/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class EJBSecurityDeploymentTest extends AbstractContextBuilderTest {

    private static final String SESSION_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockSession";
    private static final String MDB_NAME = "geronimo.j2ee:J2eeType=SessionBean,name=MockMDB";
    private static final String RESOURCE_ADAPTER_NAME = "MockRA";

    private ObjectName ejbObjectName;

    private EjbJarType ejbJar;
    private GerSecurityType security;

    private TransactionPolicySource transactionPolicySource = new TransactionPolicySource() {
        public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
            return ContainerPolicy.Required;
        }
    };

    protected void setUp() throws Exception {
        ejbJar = EjbJarType.Factory.newInstance();

        security = GerSecurityType.Factory.newInstance();
        GerRoleMappingsType roleMappings = security.addNewRoleMappings();
        GerRoleType role = roleMappings.addNewRole();

        role.setRoleName("Administrator");
        GerRealmType realm = role.addNewRealm();
        realm.setRealmName("FooRealm");
        GerPrincipalType principal = realm.addNewPrincipal();
        principal.setClass1("org.openejb.security.TestPrincipal");
        principal.setName("alan");
        principal = realm.addNewPrincipal();
        principal.setClass1("org.openejb.security.TestPrincipal");
        principal.setName("admin");

        security.setUseContextHandler(true);
    }

    private void buildSession() throws Exception {
        SessionBeanType session = SessionBeanType.Factory.newInstance();
        setUpContext(null);

        session.addNewEjbClass().setStringValue(MockEJB.class.getName());
        session.addNewEjbName().setStringValue("MockSession");
        session.addNewTransactionType().setStringValue(TransactionDemarcation.CONTAINER.toString());
        session.addNewHome().setStringValue(MockHome.class.getName());
        session.addNewRemote().setStringValue(MockRemote.class.getName());
        session.addNewLocalHome().setStringValue(MockLocalHome.class.getName());
        session.addNewLocal().setStringValue(MockLocal.class.getName());
        session.addNewSessionType().setStringValue("Stateless");

        SecurityIdentityType securityIdentity = session.addNewSecurityIdentity();
        RunAsType runAs = securityIdentity.addNewRunAs();

        runAs.addNewRoleName().setStringValue("Administrator");

        /*
        BeanSecurityType beanSecurity = new BeanSecurity();
        beanSecurity.setUseIdentity(true);
        session.setBeanSecurity(beanSecurity);
        */
        ejbObjectName = ObjectName.getInstance(SESSION_NAME);
        //ejbMetadata = new MBeanMetadata(ejbObjectName);
    }


    public void testSessionConfigTranslation() throws Exception {
        buildSession();
        /*
        org.openejb.EJBContainerConfiguration config = planner.getSessionConfig((Session) ejb, ejbJar, ejbMetadata, transactionPolicySource);

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
        */
    }

    private void buildMDB() throws Exception {
        MessageDrivenBeanType messageDriven = MessageDrivenBeanType.Factory.newInstance();
        setUpContext(null);

        messageDriven.addNewEjbClass().setStringValue(org.openejb.mdb.MockEJB.class.getName());
        messageDriven.addNewEjbName().setStringValue("MockMDB");
        messageDriven.addNewTransactionType().setStringValue(TransactionDemarcation.CONTAINER.toString());
        messageDriven.addNewMessagingType().setStringValue(MessageListener.class.getName());
        ActivationConfigType activationConfig = messageDriven.addNewActivationConfig();
        //activationConfig.addNewActivationSpecClass().setStringValue(MockActivationSpec.class.getName());
        //activationConfig.addNewResourceAdapterName().setStringValue(RESOURCE_ADAPTER_NAME);
        messageDriven.setActivationConfig(activationConfig);

        SecurityIdentityType securityIdentity = messageDriven.addNewSecurityIdentity();
        RunAsType runAs = securityIdentity.addNewRunAs();

        runAs.addNewRoleName().setStringValue("Administrator");
        messageDriven.setSecurityIdentity(securityIdentity);
        /*
        BeanSecurity beanSecurity = new BeanSecurity();
        beanSecurity.setUseIdentity(true);
        messageDriven.setBeanSecurity(beanSecurity);
        */
        ejbObjectName = ObjectName.getInstance(MDB_NAME);
    }


    public void testMDBConfigTranslation() throws Exception {
        buildMDB();
        /*
        org.openejb.EJBContainerConfiguration config = planner.getMessageDrivenConfig((MessageDriven) ejb, ejbJar, transactionPolicySource);

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
        */
    }
}
