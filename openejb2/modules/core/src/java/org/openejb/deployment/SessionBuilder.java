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

import java.security.Permissions;
import java.util.Map;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.openejb.ContainerBuilder;
import org.openejb.sfsb.StatefulContainerBuilder;
import org.openejb.slsb.StatelessContainerBuilder;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;


class SessionBuilder extends BeanBuilder {
    public SessionBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    public GBeanMBean createBean(EARContext earContext, EJBModule ejbModule, String containerId, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        ContainerBuilder builder = null;
        Permissions toBeChecked = new Permissions();
        SecurityBuilder securityBuilder = getModuleBuilder().getSecurityBuilder();
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        if (isStateless) {
            builder = new StatelessContainerBuilder();
            builder.setTransactedTimerName(earContext.getTransactedTimerName());
            builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
            builder.setServiceEndpointName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint()));
            securityBuilder.addToPermissions(toBeChecked, ejbName, "ServiceEndpoint", builder.getServiceEndpointName(), cl);
        } else {
            builder = new StatefulContainerBuilder();
        }
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(sessionBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getHome()));
        builder.setRemoteInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getRemote()));
        builder.setLocalHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocalHome()));
        builder.setLocalInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocal()));

        securityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);

        securityBuilder.fillContainerBuilderSecurity(builder,
                toBeChecked,
                security,
                ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                sessionBean.getEjbName().getStringValue(),
                sessionBean.getSecurityIdentity(),
                sessionBean.getSecurityRoleRefArray());

        UserTransactionImpl userTransaction;
        if ("Bean".equals(sessionBean.getTransactionType().getStringValue())) {
            userTransaction = new UserTransactionImpl();
            builder.setUserTransaction(userTransaction);
            if (isStateless) {
                builder.setTransactionPolicySource(TransactionPolicyHelper.StatelessBMTPolicySource);
            } else {
                builder.setTransactionPolicySource(TransactionPolicyHelper.StatefulBMTPolicySource);
            }
        } else {
            userTransaction = null;
            TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
            builder.setTransactionPolicySource(transactionPolicySource);
        }

        try {
            ReadOnlyContext compContext = buildComponentContext(earContext, ejbModule, sessionBean, openejbSessionBean, userTransaction, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName" + ejbName, e);
        }

        if (openejbSessionBean != null) {
            setResourceEnvironment(builder, sessionBean.getResourceRefArray(), openejbSessionBean.getResourceRefArray());
            builder.setJndiNames(openejbSessionBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbSessionBean.getLocalJndiNameArray());
        } else {
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private ObjectName createEJBObjectName(EARContext earContext, String ejbModuleName, SessionBeanType sessionBean) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();
        String type = sessionBean.getSessionType().getStringValue() + "SessionBean";
        return createEJBObjectName(earContext, ejbModuleName, type, ejbName);
    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = sessionBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = sessionBean.getEjbRefArray();
        GerRemoteRefType[] openejbEjbRefs = null;
        if (openejbSessionBean != null) {
            openejbEjbRefs = openejbSessionBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = sessionBean.getEjbLocalRefArray();
        GerLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbSessionBean != null) {
            openejbEjbLocalRefs = openejbSessionBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = sessionBean.getResourceRefArray();
        GerLocalRefType[] openejbResourceRefs = null;
        if (openejbSessionBean != null) {
            openejbResourceRefs = openejbSessionBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = sessionBean.getResourceEnvRefArray();
        GerLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbSessionBean != null) {
            openejbResourceEnvRefs = openejbSessionBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = sessionBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    protected void buildBeans(EARContext earContext, Module module, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            ObjectName sessionObjectName = createEJBObjectName(earContext, module.getName(), sessionBean);

            GBeanMBean sessionGBean = createBean(earContext, ejbModule, sessionObjectName.getCanonicalName(), sessionBean, openejbSessionBean, transactionPolicyHelper, security, cl);
            earContext.addGBean(sessionObjectName, sessionGBean);
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];
            String ejbName = sessionBean.getEjbName().getStringValue();

            ObjectName sessionObjectName = createEJBObjectName(earContext, module.getName(), sessionBean);

            // ejb-ref
            if (sessionBean.isSetRemote()) {
                String remote = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getRemote());
                ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getHome());
                ENCConfigBuilder.assureEJBHomeInterface(home, cl);

                String objectName = sessionObjectName.getCanonicalName();
                earContext.getEJBRefContext().addEJBRemoteId(module.getModuleURI(), ejbName, objectName);
            }

            // ejb-local-ref
            if (sessionBean.isSetLocal()) {
                String local = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocal());
                ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocalHome());
                ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);

                String objectName = sessionObjectName.getCanonicalName();
                earContext.getEJBRefContext().addEJBLocalId(module.getModuleURI(), ejbName, objectName);
            }
        }
    }
}