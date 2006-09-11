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
import java.security.Permissions;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.openejb.BmpEjbDeploymentGBean;
import org.openejb.CmpEjbDeploymentGBean;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;


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

        AbstractNameQuery tssBeanQuery = getTssBeanQuery(openejbEntityBean, ejbModule, earContext, entityBean);
        builder.setTssBeanQuery(tssBeanQuery);

        addSecurity(earContext, ejbName, builder, cl, ejbModule, entityBean, componentPermissions, policyContextID);

        processEnvironmentRefs(builder, earContext, ejbModule, entityBean, openejbEntityBean, cl);

        try {
            if (tssBeanQuery != null) {
                if (openejbEntityBean.getJndiNameArray().length == 0) {
                    throw new DeploymentException("Cannot expose an bean via CORBA unless a JNDI name is set (that's also used as the CORBA naming service name)");
                }
                if (!entityBean.isSetRemote() || !entityBean.isSetHome()) {
                    throw new DeploymentException("An bean without a remote interface cannot be exposed via CORBA");
                }
            }
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

    private void processEnvironmentRefs(EntityBuilder builder, EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, ClassLoader cl) throws DeploymentException {
        // env entries
        EnvEntryType[] envEntries = entityBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = entityBean.getEjbRefArray();
        GerEjbRefType[] openejbEjbRefs = null;

        EjbLocalRefType[] ejbLocalRefs = entityBean.getEjbLocalRefArray();
        GerEjbLocalRefType[] openejbEjbLocalRefs = null;

        // resource refs
        ResourceRefType[] resourceRefs = entityBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = entityBean.getResourceEnvRefArray();
        GerResourceEnvRefType[] openejbResourceEnvRefs = null;

        ServiceRefType[] serviceRefs = entityBean.getServiceRefArray();
        GerServiceRefType[] openejbServiceRefs = null;

        GerGbeanRefType[] gBeanRefs = null;

        if (openejbEntityBean != null) {
            openejbEjbRefs = openejbEntityBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbEntityBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbEntityBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbEntityBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbEntityBean.getServiceRefArray();
            builder.setJndiNames(openejbEntityBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
        } else {
            String ejbName = entityBean.getEjbName().getStringValue().trim();
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        Map componentContext = new HashMap();
        Configuration earConfiguration = earContext.getConfiguration();
        getNamingBuilders().buildNaming(entityBean, openejbEntityBean, earConfiguration, earConfiguration, ejbModule, componentContext);
        builder.setComponentContext(componentContext);
        ENCConfigBuilder.setResourceEnvironment(builder, resourceRefs, openejbResourceRefs);
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

    protected AbstractNameQuery getTssBeanQuery(OpenejbEntityBeanType openejbEntityBean, EJBModule ejbModule, EARContext earContext, EntityBeanType entityBean) throws DeploymentException {
        AbstractNameQuery tssBeanObjectName = null;
        if (openejbEntityBean != null) {
            if (openejbEntityBean.isSetTssLink()) {
                String tssBeanLink = openejbEntityBean.getTssLink().trim();
                URI moduleURI = ejbModule.getModuleURI();
                String moduleType = NameFactory.EJB_MODULE;
                tssBeanObjectName = ENCConfigBuilder.buildAbstractNameQuery(null, moduleURI == null? null: moduleURI.toString(), tssBeanLink, moduleType, NameFactory.EJB_MODULE);
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
            } else if (openejbEntityBean.isSetTss()) {
                tssBeanObjectName = ENCConfigBuilder.buildAbstractNameQuery(openejbEntityBean.getTss(), NameFactory.CORBA_TSS, NameFactory.EJB_MODULE, null);
            }
        }
        if (tssBeanObjectName != null && openejbEntityBean.getJndiNameArray().length == 0) {
            throw new DeploymentException("Cannot expose a session bean via CORBA unless a JNDI name is set (that's also used as the CORBA naming service name)");
        }
        if (tssBeanObjectName != null && (!entityBean.isSetRemote() || !entityBean.isSetHome())) {
            throw new DeploymentException("A session bean without a remote interface cannot be exposed via CORBA");
        }
        return tssBeanObjectName;
    }
}