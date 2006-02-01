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
import java.util.SortedMap;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.axis.builder.WSDescriptorParser;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
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
import org.apache.xmlbeans.XmlException;
import org.openejb.EJBComponentType;
import org.openejb.StatelessEjbDeployment;
import org.openejb.proxy.ProxyInfo;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.ejbjar.OpenejbTssType;
import org.openejb.xbeans.ejbjar.OpenejbWebServiceSecurityType;


public class XmlBeansSessionBuilder extends XmlBeanBuilder {
    private final static String DEFAULT_AUTH_REALM_NAME = "Geronimo Web Service";

    private final ObjectName defaultStatelessEjbContainer;
    private final ObjectName defaultStatefulEjbContainer;
    private final GBeanData linkDataTemplate;
    private final WebServiceBuilder webServiceBuilder;

    public XmlBeansSessionBuilder(OpenEjbModuleBuilder moduleBuilder,
            ObjectName defaultStatelessEjbContainer,
            ObjectName defaultStatefulEjbContainer,
            GBeanData linkDataTemplate,
            WebServiceBuilder webServiceBuilder) {
        super(moduleBuilder);
        this.defaultStatelessEjbContainer = defaultStatelessEjbContainer;
        this.defaultStatefulEjbContainer = defaultStatefulEjbContainer;
        this.webServiceBuilder = webServiceBuilder;
        this.linkDataTemplate = linkDataTemplate;
    }

    private ObjectName createEJBObjectName(J2eeContext moduleJ2eeContext, SessionBeanType sessionBean) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue().trim();
        String type = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim())? NameFactory.STATELESS_SESSION_BEAN: NameFactory.STATEFUL_SESSION_BEAN;
        try {
            return NameFactory.getEjbComponentName(null, null, null, null, ejbName, type, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct ejb object name: " + ejbName, e);
        }
    }


    public void buildBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ClassLoader cl, EJBModule ejbModule, ComponentPermissions componentPermissions, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, ObjectName listener, String policyContextID, Map portInfoMap) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            ObjectName sessionObjectName = createEJBObjectName(moduleJ2eeContext, sessionBean);
            assert sessionObjectName != null: "StatelesSessionBean object name is null";
            addEJBContainerGBean(earContext, ejbModule, componentPermissions, cl, sessionObjectName, sessionBean, openejbSessionBean, transactionPolicyHelper, policyContextID);

            boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim());
            boolean isServiceEndpoint = sessionBean.isSetServiceEndpoint();
            if (isStateless && isServiceEndpoint) {
                addWSContainerGBean(earContext, ejbModule, cl, portInfoMap, sessionObjectName, sessionBean, openejbSessionBean, listener);
            }
        }
    }

    private void addWSContainerGBean(EARContext earContext, EJBModule ejbModule, ClassLoader cl, Map portInfoMap, ObjectName sessionObjectName, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, ObjectName listener) throws DeploymentException {

        String ejbName = sessionBean.getEjbName().getStringValue().trim();
        J2eeContext j2eeContext = earContext.getJ2eeContext();
        OpenejbWebServiceSecurityType webServiceSecurity = openejbSessionBean == null ? null : openejbSessionBean.getWebServiceSecurity();

        //this code belongs here
        ObjectName linkName = null;
        try {
            linkName = NameFactory.getComponentName(null, null, null, null, null, ejbName, NameFactory.WEB_SERVICE_LINK, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct web service link name", e);
        }

        GBeanData linkData = new GBeanData(linkDataTemplate);
        linkData.setName(linkName);
        Object portInfo = portInfoMap.get(ejbName);
        //let the webServiceBuilder configure its part
        webServiceBuilder.configureEJB(linkData, ejbModule.getModuleFile(), portInfo, cl);
        //configure the security part and references
        if (webServiceSecurity != null) {
            linkData.setAttribute("securityRealmName", webServiceSecurity.getSecurityRealmName().trim());
            linkData.setAttribute("realmName", webServiceSecurity.isSetRealmName()? webServiceSecurity.getRealmName().trim(): DEFAULT_AUTH_REALM_NAME);
            linkData.setAttribute("transportGuarantee", webServiceSecurity.getTransportGuarantee().toString());
            linkData.setAttribute("authMethod", webServiceSecurity.getAuthMethod().toString());
        }

        linkData.setReferencePattern("WebServiceContainer", listener);
        linkData.setReferencePattern("EJBContainer", sessionObjectName);

        if (openejbSessionBean != null) {
            String[] virtualHosts = openejbSessionBean.getWebServiceVirtualHostArray();
            for (int i = 0; i < virtualHosts.length; i++) {
                virtualHosts[i] = virtualHosts[i].trim();
            }
            linkData.setAttribute("virtualHosts", virtualHosts);
        }

        earContext.addGBean(linkData);
    }

    private void addEJBContainerGBean(EARContext earContext, EJBModule ejbModule, ComponentPermissions componentPermissions, ClassLoader cl, ObjectName sessionObjectName, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, String policyContextID) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue().trim();

        XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
        Permissions toBeChecked = new Permissions();
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim());

        SessionBuilder sessionBuilder;
        if (isStateless) {
            StatelessBuilder statelessBuilder = new StatelessBuilder();
            statelessBuilder.setEjbContainerName(defaultStatelessEjbContainer);

            // Web services configuration
            String serviceEndpointName = OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint());
            statelessBuilder.setServiceEndpointInterfaceName(serviceEndpointName);
            statelessBuilder.setHandlerInfos(createHandlerInfos(ejbModule.getModuleFile(), ejbName, cl));
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "ServiceEndpoint", serviceEndpointName, cl);
            sessionBuilder = statelessBuilder;
        } else {
            StatefulBuilder statefulBuilder = new StatefulBuilder();
            statefulBuilder.setEjbContainerName(defaultStatefulEjbContainer);

            if (openejbSessionBean != null && openejbSessionBean.isSetEjbClusterReference()) {
                ObjectName clusterObjectName;
                try {
                    String name = openejbSessionBean.getEjbClusterReference().getTargetName();
                    clusterObjectName = ObjectName.getInstance(name);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid object name for ejb-cluster-reference", e);
                }
                statefulBuilder.setEjbClusterManagerName(clusterObjectName);
            }
            sessionBuilder = statefulBuilder;
        }
        sessionBuilder.setContainerId(sessionObjectName);
        sessionBuilder.setEjbName(ejbName);

        sessionBuilder.setHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getHome()));
        sessionBuilder.setRemoteInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getRemote()));
        sessionBuilder.setLocalHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getLocalHome()));
        sessionBuilder.setLocalInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getLocal()));
        sessionBuilder.setBeanClassName(sessionBean.getEjbClass().getStringValue());

        // jndiNames
        // localJndiNames
        if (openejbSessionBean != null) {
            sessionBuilder.setJndiNames(openejbSessionBean.getJndiNameArray());
            sessionBuilder.setLocalJndiNames(openejbSessionBean.getLocalJndiNameArray());
        } else {
            sessionBuilder.setJndiNames(new String[]{ejbName});
            sessionBuilder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        SecurityConfiguration securityConfiguration = earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Home", sessionBuilder.getHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", sessionBuilder.getLocalHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", sessionBuilder.getRemoteInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Local", sessionBuilder.getLocalInterfaceName(), cl);

            String defaultRole = securityConfiguration.getDefaultRole();
            xmlBeansSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    sessionBean.getSecurityRoleRefArray(),
                    componentPermissions);

            xmlBeansSecurityBuilder.setDetails(sessionBean.getSecurityIdentity(), securityConfiguration, policyContextID, sessionBuilder);
        }

        // transaction
        boolean beanManagedTransactions = "Bean".equals(sessionBean.getTransactionType().getStringValue());
        sessionBuilder.setBeanManagedTransactions(beanManagedTransactions);
        if (!beanManagedTransactions) {
            SortedMap transactionPolicies = transactionPolicyHelper.getTransactionPolicies(ejbName);
            sessionBuilder.setTransactionPolicies(transactionPolicies);
        }

        // tssBean
        ObjectName tssBeanName = getTssBeanName(openejbSessionBean, ejbModule, earContext);
        sessionBuilder.setTssBeanName(tssBeanName);

        // componentContext, unshareableResources and applicationManagedSecurityResources
        processEnvironmentRefs(sessionBuilder, earContext, ejbModule, sessionBean, openejbSessionBean, cl);

        GBeanData sessionGBean;
        try {
            sessionGBean = sessionBuilder.createConfiguration();
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
        earContext.addGBean(sessionGBean);
    }

    private void processEnvironmentRefs(SessionBuilder sessionBuilder, EARContext earContext, EJBModule ejbModule, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, ClassLoader cl) throws DeploymentException {
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

        if (openejbSessionBean != null) {
            openejbEjbRefs = openejbSessionBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbSessionBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbSessionBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbSessionBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbSessionBean.getServiceRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = sessionBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext, null, ejbModule, null, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, serviceRefs, openejbServiceRefs, cl);
        sessionBuilder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(earContext, ejbModule.getModuleURI(), sessionBuilder, resourceRefs, openejbResourceRefs);
    }

    private List createHandlerInfos(JarFile moduleFile, String ejbName, ClassLoader cl) throws DeploymentException {
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
        PortComponentHandlerType[] handlers = null;
        for (int i = 0; i < webserviceDescriptions.length && handlers == null; i++) {

            PortComponentType[] portComponents = webserviceDescriptions[i].getPortComponentArray();
            for (int j = 0; j < portComponents.length && handlers == null; j++) {

                EjbLinkType ejbLink = portComponents[j].getServiceImplBean().getEjbLink();
                if (ejbLink != null && ejbLink.getStringValue().trim().equals(ejbName)) {
                    handlers = portComponents[j].getHandlerArray();
                }
            }
        }

        if (handlers == null) {
            return null;
        }

        List handlerInfos = WSDescriptorParser.createHandlerInfoList(handlers, cl);
        return handlerInfos;
    }

    public void initContext(EARContext earContext, J2eeContext moduleJ2eeContext, URI moduleUri, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            ObjectName sessionObjectName = createEJBObjectName(moduleJ2eeContext, sessionBean);
            GBeanData gbean = new GBeanData(sessionObjectName, StatelessEjbDeployment.GBEAN_INFO);

            Class homeInterface = null;
            Class remoteInterface = null;
            Class localHomeInterface = null;
            Class localObjectInterface = null;
            // ejb-ref
            if (sessionBean.isSetRemote()) {
                String remote =  sessionBean.getRemote().getStringValue().trim();
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
            int componentType = sessionBean.getSessionType().getStringValue().trim().equals("Stateless")? EJBComponentType.STATELESS: EJBComponentType.STATEFUL;
            ProxyInfo proxyInfo = new ProxyInfo(componentType,
                    sessionObjectName.getCanonicalName(),
                    homeInterface,
                    remoteInterface,
                    localHomeInterface,
                    localObjectInterface,
                    null,
                    null);
            gbean.setAttribute("proxyInfo", proxyInfo);
            earContext.addGBean(gbean);
        }
    }

    private ObjectName getTssBeanName(OpenejbSessionBeanType openejbSessionBean, EJBModule ejbModule, EARContext earContext) throws DeploymentException {
        ObjectName tssBeanObjectName = null;
        if (openejbSessionBean != null) {
            if (openejbSessionBean.isSetTssTargetName()) {
                String tssName = openejbSessionBean.getTssTargetName().trim();
                try {
                    tssBeanObjectName = ObjectName.getInstance(tssName);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid object name for tss bean", e);
                }
            } else if (openejbSessionBean.isSetTssLink()) {
                String tssBeanLink = openejbSessionBean.getTssLink().trim();
                URI moduleURI = ejbModule.getModuleURI();
                String moduleType = NameFactory.EJB_MODULE;
                tssBeanObjectName = earContext.getRefContext().locateComponentName(tssBeanLink, moduleURI, moduleType, NameFactory.CORBA_TSS, earContext.getJ2eeContext(), earContext, "TSS GBean");
            } else if (openejbSessionBean.isSetTss()) {
                OpenejbTssType tss = openejbSessionBean.getTss();
                try {
                    tssBeanObjectName = NameFactory.getComponentName(getStringValue(tss.getDomain()),
                            getStringValue(tss.getServer()),
                            getStringValue(tss.getApplication()),
                            getStringValue(tss.getModule()),
                            getStringValue(tss.getName()),
                            NameFactory.CORBA_TSS,
                            earContext.getJ2eeContext());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid object name for tss bean", e);
                }

            }
        }
        return tssBeanObjectName;
    }
}