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
import java.security.Permissions;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.openejb.BmpEjbDeploymentGBean;
import org.apache.openejb.CmpEjbDeploymentGBean;
import org.apache.openejb.xbeans.ejbjar.OpenejbEntityBeanType;


public class XmlBeansEntityBuilder extends XmlBeanBuilder {
    protected final String defaultBmpContainerName;
    protected final String defaultCmpContainerName;

    public XmlBeansEntityBuilder(OpenEjbModuleBuilder builder, String defaultBmpContainerName, String defaultCmpContainerName) {
        super(builder);
        this.defaultBmpContainerName = defaultBmpContainerName;
        this.defaultCmpContainerName = defaultCmpContainerName;
    }

    public void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, ComponentPermissions componentPermissions, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, String policyContextID) throws DeploymentException {        // BMP Entity Beans
        EntityBeanType[] bmpEntityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < bmpEntityBeans.length; i++) {
            EntityBeanType entityBean = bmpEntityBeans[i];

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityBean.getEjbName().getStringValue().trim());
            AbstractName entityObjectName = createEJBObjectName(earContext, moduleBaseName, entityBean);

            GBeanData entityGBean = createBean(earContext, ejbModule, entityObjectName, entityBean, openejbEntityBean, componentPermissions, transactionPolicyHelper, cl, policyContextID);
            try {
                earContext.removeGBean(entityGBean.getAbstractName());
                earContext.addGBean(entityGBean);
            } catch (Exception e) {
                String ejbName = entityBean.getEjbName().getStringValue().trim();
                throw new DeploymentException("Unable to replace ejb deployment GBean: ejbName" + ejbName, e);
            }
        }
    }

    public GBeanData createBean(EARContext earContext, EJBModule ejbModule, AbstractName containerAbstractName, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, ComponentPermissions componentPermissions, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl, String policyContextID) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue().trim();

        EntityBuilder builder;
        if ("Bean".equals(entityBean.getPersistenceType().getStringValue().trim())) {
            builder = new BmpBuilder();
            builder.setEjbContainerName(defaultBmpContainerName);
        } else {
            CmpBuilder cmpBuilder = new CmpBuilder();
            cmpBuilder.setEjbContainerName(defaultCmpContainerName);
            cmpBuilder.setModuleCmpEngineName(ejbModule.getModuleCmpEngineName());
            if (entityBean.isSetCmpVersion() && "1.x".equals(getStringValue(entityBean.getCmpVersion()))) {
                cmpBuilder.setCmp2(false);
            } else {
                cmpBuilder.setCmp2(true);
            }
            builder = cmpBuilder;
        }
        builder.setContainerId(containerAbstractName.toString());
        builder.setEjbName(ejbName);

        builder.setHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getHome()));
        builder.setRemoteInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getPrimKeyClass()));
        builder.setBeanClassName(entityBean.getEjbClass().getStringValue());

        SortedMap transactionPolicies = transactionPolicyHelper.getTransactionPolicies(ejbName);
        builder.setTransactionPolicies(transactionPolicies);
        builder.setReentrant(entityBean.getReentrant().getBooleanValue());

//        AbstractNameQuery tssBeanQuery = getTssBeanQuery(openejbEntityBean, ejbModule, earContext, entityBean);
//        builder.setTssBeanQuery(tssBeanQuery);

        addSecurity(earContext, ejbName, builder, cl, ejbModule, entityBean, componentPermissions, policyContextID);

        processEnvironmentRefs(builder, earContext, ejbModule, entityBean, openejbEntityBean, containerAbstractName, cl);

        try {
//            if (tssBeanQuery != null) {
//                if (openejbEntityBean.getJndiNameArray().length == 0) {
//                    throw new DeploymentException("Cannot expose an bean via CORBA unless a JNDI name is set (that's also used as the CORBA naming service name)");
//                }
//                if (!entityBean.isSetRemote() || !entityBean.isSetHome()) {
//                    throw new DeploymentException("An bean without a remote interface cannot be exposed via CORBA");
//                }
//            }
            GBeanData gbean = builder.createConfiguration();
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
        }
    }

    public AbstractName createEJBObjectName(EARContext earContext, AbstractName moduleBaseName, EntityBeanType entityBean) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();
        return earContext.getNaming().createChildName(moduleBaseName, ejbName, NameFactory.ENTITY_BEAN);
    }

    private void processEnvironmentRefs(EntityBuilder builder, EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, AbstractName ejbAbstractName, ClassLoader cl) throws DeploymentException {
        // resource refs
        ResourceRefType[] resourceRefs = entityBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        if (openejbEntityBean != null) {
            openejbResourceRefs = openejbEntityBean.getResourceRefArray();
            builder.setJndiNames(openejbEntityBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
        } else {
            String ejbName = entityBean.getEjbName().getStringValue().trim();
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        Map buildingContext = new HashMap();
        buildingContext.put(NamingBuilder.JNDI_KEY, new HashMap());
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, ejbAbstractName);
        Configuration earConfiguration = earContext.getConfiguration();
        getNamingBuilders().buildNaming(entityBean, openejbEntityBean, earConfiguration, earConfiguration, ejbModule, buildingContext);
        Map compContext = (Map) buildingContext.get(NamingBuilder.JNDI_KEY);
        builder.setComponentContext(compContext);
        getResourceEnvironmentSetter().setResourceEnvironment(builder, resourceRefs, openejbResourceRefs);
    }

    public void initContext(EARContext earContext, AbstractName moduleBaseName, URI moduleUri, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            AbstractName entityObjectName = createEJBObjectName(earContext, moduleBaseName, entityBean);
            GBeanData gbean;
            if ("Bean".equals(entityBean.getPersistenceType().getStringValue().trim())) {
                gbean = new GBeanData(entityObjectName, BmpEjbDeploymentGBean.GBEAN_INFO);
            } else {
                gbean = new GBeanData(entityObjectName, CmpEjbDeploymentGBean.GBEAN_INFO);
            }

            String homeInterfaceName = null;
            String remoteInterfaceName = null;
            String localHomeInterfaceName = null;
            String localInterfaceName = null;

            // ejb-ref
            if (entityBean.isSetRemote()) {
                remoteInterfaceName = entityBean.getRemote().getStringValue().trim();
                OpenEjbRemoteRefBuilder.assureEJBObjectInterface(remoteInterfaceName, cl);

                homeInterfaceName = entityBean.getHome().getStringValue().trim();
                OpenEjbRemoteRefBuilder.assureEJBHomeInterface(homeInterfaceName, cl);
            }

            // ejb-local-ref
            if (entityBean.isSetLocal()) {
                localInterfaceName = entityBean.getLocal().getStringValue().trim();
                OpenEjbLocalRefBuilder.assureEJBLocalObjectInterface(localInterfaceName, cl);

                localHomeInterfaceName = entityBean.getLocalHome().getStringValue().trim();
                OpenEjbLocalRefBuilder.assureEJBLocalHomeInterface(localHomeInterfaceName, cl);
            }
            String primaryKeyClassName = entityBean.getPrimKeyClass().getStringValue().trim();
            try {
                ClassLoading.loadClass(primaryKeyClassName, cl);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load primary key class: " + primaryKeyClassName + " for entity: " + entityObjectName);
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

    protected void addSecurity(EARContext earContext, String ejbName, EntityBuilder builder, ClassLoader cl, EJBModule ejbModule, EntityBeanType entityBean, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        SecurityConfiguration securityConfiguration = (SecurityConfiguration) earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            Permissions toBeChecked = new Permissions();
            XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
            String defaultRole = securityConfiguration.getDefaultRole();
            xmlBeansSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    entityBean.getSecurityRoleRefArray(), componentPermissions);

            xmlBeansSecurityBuilder.setDetails(entityBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }
    }

}