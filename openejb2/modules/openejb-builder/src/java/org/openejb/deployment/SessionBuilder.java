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

import java.net.URI;
import java.net.URL;
import java.security.Permissions;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.transaction.UserTransaction;

import org.apache.geronimo.axis.builder.WSDescriptorParser;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLinkType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.j2ee.PortComponentType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.xbeans.j2ee.WebserviceDescriptionType;
import org.apache.geronimo.xbeans.j2ee.WebservicesDocument;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.xmlbeans.XmlException;
import org.openejb.EJBComponentType;
import org.openejb.GenericEJBContainer;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.ProxyInfo;
import org.openejb.slsb.HandlerChainConfiguration;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.ejbjar.OpenejbWebServiceSecurityType;


class SessionBuilder extends BeanBuilder {

    private final static String DEFAULT_AUTH_REALM_NAME = "Geronimo Web Service";

    private final GBeanData linkDataTemplate;

    public SessionBuilder(OpenEJBModuleBuilder builder, GBeanData linkDataTemplate) {
        super(builder);
        this.linkDataTemplate = linkDataTemplate;
    }

    private AbstractName createEJBObjectName(EARContext earContext, AbstractName moduleBaseName, SessionBeanType sessionBean) {
        String ejbName = sessionBean.getEjbName().getStringValue().trim();
        String type = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim()) ? NameFactory.STATELESS_SESSION_BEAN : NameFactory.STATEFUL_SESSION_BEAN;
        return earContext.getNaming().createChildName(moduleBaseName, ejbName, type);
    }

    public void processEnvironmentRefs(ContainerBuilder builder, EARContext earContext, EJBModule ejbModule, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        // env entries
        EnvEntryType[] envEntries = sessionBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = sessionBean.getEjbRefArray();
        GerEjbRefType[] openejbEjbRefs = null;

        EjbLocalRefType[] ejbLocalRefs = sessionBean.getEjbLocalRefArray();
        GerEjbLocalRefType[] openejbEjbLocalRefs = null;

        // resource refs
        ResourceRefType[] resourceRefs = sessionBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = sessionBean.getResourceEnvRefArray();
        GerResourceEnvRefType[] openejbResourceEnvRefs = null;

        ServiceRefType[] serviceRefs = sessionBean.getServiceRefArray();
        GerServiceRefType[] openejbServiceRefs = null;

        GerGbeanRefType[] openejbGbeanRefs = null;

        if (openejbSessionBean != null) {
            openejbEjbRefs = openejbSessionBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbSessionBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbSessionBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbSessionBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbSessionBean.getServiceRefArray();
            openejbGbeanRefs = openejbSessionBean.getGbeanRefArray();
            builder.setJndiNames(openejbSessionBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbSessionBean.getLocalJndiNameArray());
        } else {
            String ejbName = sessionBean.getEjbName().getStringValue().trim();
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        MessageDestinationRefType[] messageDestinationRefs = sessionBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext,
                null,
                ejbModule,
                userTransaction,
                envEntries,
                ejbRefs, openejbEjbRefs,
                ejbLocalRefs, openejbEjbLocalRefs,
                resourceRefs, openejbResourceRefs,
                resourceEnvRefs, openejbResourceEnvRefs,
                messageDestinationRefs,
                serviceRefs, openejbServiceRefs,
                openejbGbeanRefs,
                cl);
        builder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(builder, resourceRefs, openejbResourceRefs);

    }

    protected void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, ComponentPermissions componentPermissions, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, AbstractNameQuery listener, String policyContextID, Map portInfoMap) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            AbstractName sessionName = createEJBObjectName(earContext, moduleBaseName, sessionBean);
            assert sessionName != null: "StatelesSessionBean object name is null";
            addEJBContainerGBean(earContext, ejbModule, componentPermissions, cl, sessionName, sessionBean, openejbSessionBean, transactionPolicyHelper, policyContextID);

            boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim());
            boolean isServiceEndpoint = sessionBean.isSetServiceEndpoint();
            if (isStateless && isServiceEndpoint) {
                addWSContainerGBean(earContext, sessionName, ejbModule, cl, portInfoMap, sessionBean, openejbSessionBean, listener);
            }
        }
    }

    private void addWSContainerGBean(EARContext earContext, AbstractName sessionName, EJBModule ejbModule, ClassLoader cl, Map portInfoMap, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, AbstractNameQuery listener) throws DeploymentException {

        String ejbName = sessionBean.getEjbName().getStringValue().trim();
        OpenejbWebServiceSecurityType webServiceSecurity = openejbSessionBean == null ? null : openejbSessionBean.getWebServiceSecurity();

        //this code belongs here
        AbstractName linkName;
        linkName = earContext.getNaming().createChildName(sessionName, ejbName, NameFactory.WEB_SERVICE_LINK);

        GBeanData linkData = new GBeanData(linkDataTemplate);
        linkData.setAbstractName(linkName);
        Object portInfo = portInfoMap.get(ejbName);
        //let the webServiceBuilder configure its part
        moduleBuilder.getWebServiceBuilder().configureEJB(linkData, ejbModule.getModuleFile(), portInfo, cl);
        //configure the security part and references
        if (webServiceSecurity != null) {
            linkData.setAttribute("securityRealmName", webServiceSecurity.getSecurityRealmName().trim());
            linkData.setAttribute("realmName", webServiceSecurity.isSetRealmName() ? webServiceSecurity.getRealmName().trim() : DEFAULT_AUTH_REALM_NAME);
            linkData.setAttribute("transportGuarantee", webServiceSecurity.getTransportGuarantee().toString());
            linkData.setAttribute("authMethod", webServiceSecurity.getAuthMethod().toString());
        }

        linkData.setReferencePattern("WebServiceContainer", listener);
        linkData.setReferencePattern("EJBContainer", sessionName);

        if (openejbSessionBean != null) {
            String[] virtualHosts = openejbSessionBean.getWebServiceVirtualHostArray();
            for (int i = 0; i < virtualHosts.length; i++) {
                virtualHosts[i] = virtualHosts[i].trim();
            }
            linkData.setAttribute("virtualHosts", virtualHosts);
        }

        try {
            earContext.addGBean(linkData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add link gbean to context", e);
        }
    }

    private void addEJBContainerGBean(EARContext earContext, EJBModule ejbModule, ComponentPermissions componentPermissions, ClassLoader cl, AbstractName sessionAbstractName, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, String policyContextID) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        ContainerBuilder builder;
        ContainerSecurityBuilder containerSecurityBuilder = new ContainerSecurityBuilder();
        Permissions toBeChecked = new Permissions();
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim());
        if (isStateless) {
            builder = new StatelessContainerBuilder();
            builder.setTransactedTimerName(earContext.getTransactedTimerName());
            builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
            builder.setServiceEndpointName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint()));
            ((StatelessContainerBuilder) builder).setHandlerChainConfiguration(createHandlerChainConfiguration(ejbModule.getModuleFile(), ejbName, cl));
            containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "ServiceEndpoint", builder.getServiceEndpointName(), cl);
        } else {
            builder = new StatefulContainerBuilder();
        }
        builder.setClassLoader(cl);
        //TODO configID need a canonical form!!
        builder.setContainerId(sessionAbstractName.toURI().toString());
        builder.setEJBName(ejbName);
        builder.setBeanClassName(sessionBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getHome()));
        builder.setRemoteInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getRemote()));
        builder.setLocalHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocalHome()));
        builder.setLocalInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(sessionBean.getLocal()));

        SecurityConfiguration securityConfiguration = earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
            containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
            containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
            containerSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);

            String defaultRole = securityConfiguration.getDefaultRole();
            containerSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    sessionBean.getSecurityRoleRefArray(), componentPermissions);

            containerSecurityBuilder.setDetails(sessionBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }

        UserTransactionImpl userTransaction;
        if ("Bean".equals(sessionBean.getTransactionType().getStringValue())) {
            userTransaction = new UserTransactionImpl();
            builder.setUserTransaction(userTransaction);
            if (isStateless) {
                builder.setTransactionPolicySource(TransactionPolicyHelper.BMTPolicySource);
            } else {
                builder.setTransactionPolicySource(new StatefulTransactionPolicySource(TransactionPolicyHelper.BMTPolicySource));
            }
        } else {
            userTransaction = null;
            TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
            if (isStateless) {
                builder.setTransactionPolicySource(transactionPolicySource);
            } else {
                builder.setTransactionPolicySource(new StatefulTransactionPolicySource(transactionPolicySource));
            }
        }
        builder.setTransactionImportPolicyBuilder(getModuleBuilder().getTransactionImportPolicyBuilder());

        processEnvironmentRefs(builder, earContext, ejbModule, sessionBean, openejbSessionBean, userTransaction, cl);

        AbstractNameQuery tssBeanObjectName;
        tssBeanObjectName = getTssBeanQuery(openejbSessionBean, ejbModule, earContext, sessionBean);

        try {
            GBeanData sessionGBean = earContext.getGBeanInstance(sessionAbstractName);
            builder.createConfiguration(earContext.getTransactionContextManagerObjectName(), earContext.getConnectionTrackerObjectName(), tssBeanObjectName, sessionGBean);
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private AbstractNameQuery getTssBeanQuery(OpenejbSessionBeanType openejbSessionBean, EJBModule ejbModule, EARContext earContext, SessionBeanType sessionBean) throws DeploymentException {
        AbstractNameQuery tssBeanObjectName = null;
        if (openejbSessionBean != null) {
            if (openejbSessionBean.isSetTssLink()) {
                String tssBeanLink = openejbSessionBean.getTssLink().trim();
                URI moduleURI = ejbModule.getModuleURI();
                String moduleType = NameFactory.EJB_MODULE;
                tssBeanObjectName = ENCConfigBuilder.buildAbstractNameQuery(null, moduleURI == null ? null : moduleURI.toString(), tssBeanLink, moduleType, NameFactory.EJB_MODULE);
                try {
                    earContext.getConfiguration().findGBean(tssBeanObjectName);
                } catch (GBeanNotFoundException e) {
                    tssBeanObjectName = ENCConfigBuilder.buildAbstractNameQuery(null, null, tssBeanLink, null, NameFactory.EJB_MODULE);
                    try {
                        earContext.getConfiguration().findGBean(tssBeanObjectName);
                    } catch (GBeanNotFoundException e1) {
                        throw new DeploymentException("No tss bean found", e);
                    }
                }
            } else if (openejbSessionBean.isSetTss()) {
                tssBeanObjectName = ENCConfigBuilder.buildAbstractNameQuery(openejbSessionBean.getTss(), NameFactory.CORBA_TSS, NameFactory.EJB_MODULE, null);
            }
        }
        if (tssBeanObjectName != null && openejbSessionBean.getJndiNameArray().length == 0) {
            throw new DeploymentException("Cannot expose a session bean via CORBA unless a JNDI name is set (that's also used as the CORBA naming service name)");
        }
        if (tssBeanObjectName != null && (!sessionBean.isSetRemote() || !sessionBean.isSetHome())) {
            throw new DeploymentException("A session bean without a remote interface cannot be exposed via CORBA");
        }
        return tssBeanObjectName;
    }

    private HandlerChainConfiguration createHandlerChainConfiguration(JarFile moduleFile, String ejbName, ClassLoader cl) throws DeploymentException {
        PortComponentHandlerType[] handlers = null;
        String webservicesdd;
        try {
            URL webservicesURL = DeploymentUtil.createJarURL(moduleFile, "META-INF/webservices.xml");
            webservicesdd = DeploymentUtil.readAll(webservicesURL);
        } catch (Exception e) {
            return null;//no ws dd
        }
        WebservicesDocument webservicesDocument;
        try {
            webservicesDocument = WebservicesDocument.Factory.parse(webservicesdd);
        } catch (XmlException e) {
            throw new DeploymentException("invalid webservicesdd", e);
        }

        WebserviceDescriptionType[] webserviceDescriptions = webservicesDocument.getWebservices().getWebserviceDescriptionArray();
        for (int i = 0; i < webserviceDescriptions.length && handlers == null; i++) {

            PortComponentType[] portComponents = webserviceDescriptions[i].getPortComponentArray();
            for (int j = 0; j < portComponents.length && handlers == null; j++) {

                EjbLinkType ejbLink = portComponents[j].getServiceImplBean().getEjbLink();
                if (ejbLink != null && ejbLink.getStringValue().trim().equals(ejbName)) {
                    handlers = portComponents[j].getHandlerArray();
                }
            }
        }

        if (handlers != null) {
            List handlerInfos = WSDescriptorParser.createHandlerInfoList(handlers, cl);
            return new HandlerChainConfiguration(handlerInfos, new String[]{});
        } else {
            return null;
        }
    }

    public void initContext(EARContext earContext, AbstractName moduleJ2eeContext, URI moduleUri, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            AbstractName sessionName = createEJBObjectName(earContext, moduleJ2eeContext, sessionBean);
            GBeanData gbean = new GBeanData(sessionName, GenericEJBContainer.GBEAN_INFO);

            Class homeInterface = null;
            Class remoteInterface = null;
            Class localHomeInterface = null;
            Class localObjectInterface = null;
            // ejb-ref
            if (sessionBean.isSetRemote()) {
                String remote = sessionBean.getRemote().getStringValue().trim();
                remoteInterface = ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = sessionBean.getHome().getStringValue().trim();
                homeInterface = ENCConfigBuilder.assureEJBHomeInterface(home, cl);
            }

            // ejb-local-ref
            if (sessionBean.isSetLocal()) {
                String local = sessionBean.getLocal().getStringValue().trim();
                localObjectInterface = ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = sessionBean.getLocalHome().getStringValue().trim();
                localHomeInterface = ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);
            }
            int componentType = sessionBean.getSessionType().getStringValue().trim().equals("Stateless") ? EJBComponentType.STATELESS : EJBComponentType.STATEFUL;
            ProxyInfo proxyInfo = new ProxyInfo(componentType,
                    //TODO configid need canonical form
                    sessionName.toString(),
                    homeInterface,
                    remoteInterface,
                    localHomeInterface,
                    localObjectInterface,
                    null,
                    null);
            gbean.setAttribute("proxyInfo", proxyInfo);
            try {
                earContext.addGBean(gbean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("duplicate session bean name", e);
            }
        }
    }

    private static class StatefulTransactionPolicySource implements TransactionPolicySource {
        private final TransactionPolicySource transactionPolicySource;

        public StatefulTransactionPolicySource(TransactionPolicySource transactionPolicySource) {
            this.transactionPolicySource = transactionPolicySource;
        }

        public TransactionPolicyType getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
            if ("Home".equals(methodIntf)) {
                return TransactionPolicyType.NotSupported;
            }
            if ("LocalHome".equals(methodIntf)) {
                return TransactionPolicyType.NotSupported;
            }
            return transactionPolicySource.getTransactionPolicy(methodIntf, signature);
        }
    }
}