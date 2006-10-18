/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.deployment;

import java.net.URI;
import java.net.URL;
import java.security.Permissions;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.jar.JarFile;

import org.apache.geronimo.axis.builder.WSDescriptorParser;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLinkType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.j2ee.PortComponentType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.xbeans.j2ee.WebserviceDescriptionType;
import org.apache.geronimo.xbeans.j2ee.WebservicesDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.openejb.StatefulEjbDeploymentGBean;
import org.apache.openejb.StatelessEjbDeploymentGBean;
import org.apache.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.apache.openejb.xbeans.ejbjar.OpenejbWebServiceSecurityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class XmlBeansSessionBuilder extends XmlBeanBuilder {
    private static final Log log = LogFactory.getLog(XmlBeansSessionBuilder.class);

    private final static String DEFAULT_AUTH_REALM_NAME = "Geronimo Web Service";

    private final String defaultStatelessEjbContainer;
    private final String defaultStatefulEjbContainer;
    private final GBeanData linkDataTemplate;

    public XmlBeansSessionBuilder(OpenEjbModuleBuilder moduleBuilder,
                                  String defaultStatelessEjbContainer,
                                  String defaultStatefulEjbContainer,
                                  GBeanData linkDataTemplate) {
        super(moduleBuilder);

        this.defaultStatelessEjbContainer = defaultStatelessEjbContainer;
        this.defaultStatefulEjbContainer = defaultStatefulEjbContainer;
        this.linkDataTemplate = linkDataTemplate;
    }

    private AbstractName createEjbName(EARContext earContext, AbstractName moduleBaseName, SessionBeanType sessionBean) {
        String ejbName = sessionBean.getEjbName().getStringValue().trim();
        String type = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim()) ? NameFactory.STATELESS_SESSION_BEAN : NameFactory.STATEFUL_SESSION_BEAN;
        return earContext.getNaming().createChildName(moduleBaseName, ejbName, type);
    }

    protected void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, ComponentPermissions componentPermissions, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, AbstractNameQuery listener, String policyContextID, Map portInfoMap) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            AbstractName sessionName = createEjbName(earContext, moduleBaseName, sessionBean);
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
        AbstractName linkName = earContext.getNaming().createChildName(sessionName, ejbName, NameFactory.WEB_SERVICE_LINK);

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

        if (listener != null) {
            linkData.setReferencePattern("WebServiceContainer", listener);
        }
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
        String ejbName = sessionBean.getEjbName().getStringValue().trim();

        XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
        Permissions toBeChecked = new Permissions();
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue().trim());

        SessionBuilder sessionBuilder;
        if (isStateless) {
            StatelessBuilder statelessBuilder = new StatelessBuilder();
            statelessBuilder.setEjbContainerName(defaultStatelessEjbContainer);
            int cacheSize = 1;

            if (openejbSessionBean != null && openejbSessionBean.isSetCacheSize()) {
                cacheSize = openejbSessionBean.getCacheSize();
                if (cacheSize < 1) {
                    log.warn("Cache Size was less than 1 for Stateless bean "+
                              openejbSessionBean.getEjbName()+" which is invalid.  Cache size changed"+
                              " to the default of 1.");
                }
            }

            statelessBuilder.setCacheSize(cacheSize);

            // Web services configuration
            String serviceEndpointName = OpenEjbModuleBuilder.getJ2eeStringValue(sessionBean.getServiceEndpoint());
            statelessBuilder.setServiceEndpointInterfaceName(serviceEndpointName);
            statelessBuilder.setHandlerInfos(createHandlerInfos(ejbModule.getModuleFile(), ejbName, cl));
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "ServiceEndpoint", serviceEndpointName, cl);
            sessionBuilder = statelessBuilder;
        } else {
            if (openejbSessionBean != null &&
                    openejbSessionBean.isSetCacheSize())
                log.warn("Cache Size was specified for Stateful bean "+openejbSessionBean.getEjbName()+
                         " and will be ignored.  Cache size is only valid for Stateless Session Beans.");
            StatefulBuilder statefulBuilder = new StatefulBuilder();
            statefulBuilder.setEjbContainerName(defaultStatefulEjbContainer);

//            if (openejbSessionBean != null && openejbSessionBean.isSetEjbClusterReference()) {
//                AbstractName clusterName;
//                try {
//                    String name = openejbSessionBean.getEjbClusterReference().getTargetName();
//                    clusterName = ObjectName.getInstance(name);
//                } catch (MalformedObjectNameException e) {
//                    throw new DeploymentException("Invalid object name for ejb-cluster-reference", e);
//                }
//                statefulBuilder.setEjbClusterManagerName(clusterName);
//            }
            sessionBuilder = statefulBuilder;
        }
        sessionBuilder.setContainerId(sessionAbstractName.toString());
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

        SecurityConfiguration securityConfiguration = (SecurityConfiguration) earContext.getSecurityConfiguration();
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
//        AbstractNameQuery tssBeanQuery = getTssBeanQuery(openejbSessionBean, ejbModule, earContext, sessionBean);
//        sessionBuilder.setTssBeanQuery(tssBeanQuery);

        // componentContext, unshareableResources and applicationManagedSecurityResources
        processEnvironmentRefs(sessionBuilder, earContext, ejbModule, sessionBean, openejbSessionBean, sessionAbstractName, cl);

        GBeanData sessionGBean;
        try {
            sessionGBean = sessionBuilder.createConfiguration();
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize ejb deployment GBean: ejbName" + ejbName, e);
        }

        try {
            earContext.removeGBean(sessionGBean.getAbstractName());
            earContext.addGBean(sessionGBean);
        } catch (Exception e) {
            throw new DeploymentException("Unable to replace ejb deployment GBean: ejbName" + ejbName, e);
        }
    }

    public void processEnvironmentRefs(SessionBuilder sessionBuilder, EARContext earContext, EJBModule ejbModule, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, AbstractName ejbAbstractName, ClassLoader cl) throws DeploymentException {
        // resource refs
        ResourceRefType[] resourceRefs = sessionBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        if (openejbSessionBean != null) {
            openejbResourceRefs = openejbSessionBean.getResourceRefArray();
        }

        Map buildingContext = new HashMap();
        buildingContext.put(NamingBuilder.JNDI_KEY, new HashMap());
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, ejbAbstractName);
        Configuration earConfiguration = earContext.getConfiguration();
        getNamingBuilders().buildNaming(sessionBean, openejbSessionBean, earConfiguration, earConfiguration, ejbModule, buildingContext);
        Map compContext = (Map) buildingContext.get(NamingBuilder.JNDI_KEY);
        sessionBuilder.setComponentContext(compContext);
        getResourceEnvironmentSetter().setResourceEnvironment(sessionBuilder, resourceRefs, openejbResourceRefs);
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

    public void initContext(EARContext earContext, AbstractName moduleJ2eeContext, URI moduleUri, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            AbstractName sessionName = createEjbName(earContext, moduleJ2eeContext, sessionBean);
            GBeanData gbean;
            if ("Stateless".equals(sessionBean.getSessionType().getStringValue().trim())) {
                gbean = new GBeanData(sessionName, StatelessEjbDeploymentGBean.GBEAN_INFO);
            } else {
                gbean = new GBeanData(sessionName, StatefulEjbDeploymentGBean.GBEAN_INFO);
            }

            String homeInterfaceName = null;
            String remoteInterfaceName = null;
            String localHomeInterfaceName = null;
            String localInterfaceName = null;

            // ejb-ref
            if (sessionBean.isSetRemote()) {
                remoteInterfaceName = sessionBean.getRemote().getStringValue().trim();
                OpenEjbRemoteRefBuilder.assureEJBObjectInterface(remoteInterfaceName, cl);

                homeInterfaceName = sessionBean.getHome().getStringValue().trim();
                OpenEjbRemoteRefBuilder.assureEJBHomeInterface(homeInterfaceName, cl);
            }

            // ejb-local-ref
            if (sessionBean.isSetLocal()) {
                localInterfaceName = sessionBean.getLocal().getStringValue().trim();
                OpenEjbLocalRefBuilder.assureEJBLocalObjectInterface(localInterfaceName, cl);

                localHomeInterfaceName = sessionBean.getLocalHome().getStringValue().trim();
                OpenEjbLocalRefBuilder.assureEJBLocalHomeInterface(localHomeInterfaceName, cl);
            }
            gbean.setAttribute("homeInterfaceName", homeInterfaceName);
            gbean.setAttribute("remoteInterfaceName", remoteInterfaceName);
            gbean.setAttribute("localHomeInterfaceName", localHomeInterfaceName);
            gbean.setAttribute("localInterfaceName", localInterfaceName);
            try {
                earContext.addGBean(gbean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add entity bean to context", e);
            }
        }
    }

}