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
package org.openejb.security;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.Policy;
import java.util.Collections;
import java.util.HashSet;
import javax.ejb.EJBException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.openejb.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.SecurityService;
import org.apache.geronimo.security.jacc.GeronimoPolicy;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm;
import org.apache.geronimo.security.realm.providers.PropertiesFileUserPrincipal;

import junit.framework.TestCase;
import org.openejb.EJBContainerConfiguration;
import org.openejb.MockTransactionManager;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.MethodSignature;
import org.openejb.slsb.MockEJB;
import org.openejb.slsb.MockHome;
import org.openejb.slsb.MockLocal;
import org.openejb.slsb.MockLocalHome;
import org.openejb.slsb.MockRemote;
import org.openejb.slsb.StatelessContainer;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;


/**
 *
 * @version $Revision$ $Date$
 */
public class EJBSecurityInterceptorTest extends TestCase {

    private final String CONTEXT_ID = "Foo Deployment Id";
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private StatelessContainer container;
    private org.openejb.EJBContainerConfiguration config;
    SecurityService securityService;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        Policy.setPolicy(new GeronimoPolicy(factory));

        securityService = new SecurityService();

        PropertiesFileSecurityRealm securityRealm = new PropertiesFileSecurityRealm();
        securityRealm.setRealmName("UnionSquare");
        securityRealm.setUsersURI((new File(new File("."), "src/test-data/data/users.properties")).toURI());
        securityRealm.setGroupsURI((new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        securityRealm.doStart();

        securityService.setRealms(Collections.singleton(securityRealm));

        config = new org.openejb.EJBContainerConfiguration();
        //config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.ejbName = "MockSession";
        config.beanClassName = MockEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }
        };
        config.contextId = CONTEXT_ID;
        config.setSecurityInterceptor = true;

        container = new StatelessContainer(config, new MockTransactionManager(), new ConnectionTrackingCoordinator());
        container.doStart();
    }

    public void tearDown() throws Exception {
        Policy.setPolicy(null);
    }

    public void testEjbName() throws Exception {
//        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
//        PolicyConfiguration configuration = factory.getPolicyConfiguration(CONTEXT_ID, true);
//        configuration.addToRole("LocalRole", new EJBMethodPermission("MockSession", "intMethod,Local,int"));
//        configuration.addToRole("RemoteRole", new EJBMethodPermission("MockSession", "intMethod,Remote,int"));
//        ((RoleMappingConfiguration) configuration).addRoleMapping("LocalRole", Collections.singletonList(new RealmPrincipal("UnionSquare", new PropertiesFileUserPrincipal("izumi"))));
//        ((RoleMappingConfiguration) configuration).addRoleMapping("RemoteRole", Collections.singletonList(new RealmPrincipal("UnionSquare", new PropertiesFileUserPrincipal("alan"))));
//        configuration.commit();
//
//        LoginContext context = new LoginContext("UnionSquare", new UserPwCallbackHandler("izumi", "violin"));
//        context.login();
//        Subject localSubject = context.getSubject();
//
//        context = new LoginContext("UnionSquare", new UserPwCallbackHandler("alan", "starcraft"));
//        context.login();
//        Subject remoteSubject = context.getSubject();
//        Serializable remoteSubjectId = ContextManager.getSubjectId(remoteSubject);
//
//        ContextManager.setCurrentCallerId(remoteSubjectId);
//
//        MockHome home = (MockHome) container.getEJBHome();
//        MockRemote remote = home.create();
//        assertEquals(2, remote.intMethod(1));
//
//        ContextManager.setCurrentCallerId(ContextManager.getSubjectId(localSubject));
//        try {
//            remote.intMethod(1);
//            if (config.setSecurityInterceptor) fail("Should have thrown a exception");
//        } catch (EJBException ee) {
//        }
//
//        MockLocalHome localHome = (MockLocalHome) container.getEJBLocalHome();
//        MockLocal local = localHome.create();
//
//        ContextManager.setNextCaller(remoteSubject);
//        try {
//            local.intMethod(1);
//            if (config.setSecurityInterceptor) fail("Should have thrown a exception");
//        } catch (EJBException ee) {
//        }
//
//        ContextManager.setNextCaller(localSubject);
//        local.intMethod(1);
//
//        int COUNT = 100000;
//        for (int i = 0; i < COUNT; i++) {
//            ContextManager.setNextCaller(localSubject);
//            local.intMethod(1);
//        }
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < COUNT; i++) {
//            ContextManager.setNextCaller(localSubject);
//            local.intMethod(1);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("Per local call w/ security: " + ((end - start) * 1000000.0 / COUNT) + "ns!");
//
//        COUNT = 10000;
//        for (int i = 0; i < COUNT; i++) {
//            ContextManager.setCurrentCallerId(remoteSubjectId);
//            remote.intMethod(1);
//        }
//        start = System.currentTimeMillis();
//        for (int i = 0; i < COUNT; i++) {
//            ContextManager.setCurrentCallerId(remoteSubjectId);
//            remote.intMethod(1);
//        }
//        end = System.currentTimeMillis();
//        System.out.println("Per remote cal w/ security: " + ((end - start) * 1000000.0 / COUNT) + "ns!");
//
//        ContextManager.unregisterSubject(localSubject);
//        ContextManager.unregisterSubject(remoteSubject);
    }

    class UserPwCallbackHandler implements CallbackHandler {
        private final String username;
        private final String password;

        UserPwCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                } else if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback) callbacks[i]).setName(username);
                }
            }
        }
    }
}
