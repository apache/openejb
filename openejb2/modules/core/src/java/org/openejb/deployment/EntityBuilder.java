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
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.openejb.entity.bmp.BMPContainerBuilder;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;


class EntityBuilder extends BeanBuilder {
    public EntityBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    public void buildBeans(EARContext earContext, Module module, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // BMP Entity Beans
        EntityBeanType[] bmpEntityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < bmpEntityBeans.length; i++) {
            EntityBeanType entityBean = bmpEntityBeans[i];

            if (!"Bean".equals(entityBean.getPersistenceType().getStringValue())) {
                continue;
            }

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityBean.getEjbName().getStringValue());
            ObjectName entityObjectName = createEJBObjectName(earContext, module.getName(), entityBean);

            GBeanMBean entityGBean = createBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, transactionPolicyHelper, security, cl);
            earContext.addGBean(entityObjectName, entityGBean);
        }
    }

    public GBeanMBean createBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();

        BMPContainerBuilder builder = new BMPContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(entityBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getHome()));
        builder.setRemoteInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getPrimKeyClass()));
        TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
        builder.setTransactionPolicySource(transactionPolicySource);
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        Permissions toBeChecked = new Permissions();
        SecurityBuilder securityBuilder = getModuleBuilder().getSecurityBuilder();
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        securityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
        securityBuilder.fillContainerBuilderSecurity(builder,
                toBeChecked,
                security,
                ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                entityBean.getEjbName().getStringValue(),
                entityBean.getSecurityIdentity(),
                entityBean.getSecurityRoleRefArray());

        try {
            ReadOnlyContext compContext = buildComponentContext(earContext, ejbModule, entityBean, openejbEntityBean, null, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName=" + ejbName, e);
        }

        if (openejbEntityBean != null) {
            setResourceEnvironment(builder, entityBean.getResourceRefArray(), openejbEntityBean.getResourceRefArray());
            builder.setJndiNames(openejbEntityBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
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
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
        }
    }

    public ObjectName createEJBObjectName(EARContext earContext, String moduleName, EntityBeanType entityBean) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "EntityBean", ejbName);
    }

    public ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = entityBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = entityBean.getEjbRefArray();
        GerRemoteRefType[] openejbEjbRefs = null;
        if (openejbEntityBean != null) {
            openejbEjbRefs = openejbEntityBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = entityBean.getEjbLocalRefArray();
        GerLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbEntityBean != null) {
            openejbEjbLocalRefs = openejbEntityBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = entityBean.getResourceRefArray();
        GerLocalRefType[] openejbResourceRefs = null;
        if (openejbEntityBean != null) {
            openejbResourceRefs = openejbEntityBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = entityBean.getResourceEnvRefArray();
        GerLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbEntityBean != null) {
            openejbResourceEnvRefs = openejbEntityBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = entityBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            String ejbName = entityBean.getEjbName().getStringValue();

            ObjectName entityObjectName = createEJBObjectName(earContext, module.getName(), entityBean);

            // ejb-ref
            if (entityBean.isSetRemote()) {
                String remote = OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getRemote());
                ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getHome());
                ENCConfigBuilder.assureEJBHomeInterface(home, cl);

                String objectName = entityObjectName.getCanonicalName();
                earContext.getEJBRefContext().addEJBRemoteId(module.getModuleURI(), ejbName, objectName);
            }

            // ejb-local-ref
            if (entityBean.isSetLocal()) {
                String local = OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocal());
                ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = OpenEJBModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome());
                ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);

                String objectName = entityObjectName.getCanonicalName();
                earContext.getEJBRefContext().addEJBLocalId(module.getModuleURI(), ejbName, objectName);
            }
        }
    }
}