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
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.connector.ActivationSpecInfo;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.openejb.mdb.MDBContainerBuilder;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbActivationConfigPropertyType;


class MdbBuilder extends BeanBuilder {
    private Kernel kernel;

    public MdbBuilder(Kernel kernel, OpenEJBModuleBuilder builder) {
        super(builder);
        this.kernel = kernel;
    }

    protected void buildBeans(EARContext earContext, Module module, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, Security security, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Message Driven Beans
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];

            OpenejbMessageDrivenBeanType openejbMessageDrivenBean = (OpenejbMessageDrivenBeanType) openejbBeans.get(messageDrivenBean.getEjbName().getStringValue());
            if (openejbMessageDrivenBean == null) {
                throw new DeploymentException("No openejb deployment descriptor for mdb: " + messageDrivenBean.getEjbName().getStringValue() + ". Known beans: " + openejbBeans.keySet().toArray());
            }
            ObjectName messageDrivenObjectName = createEJBObjectName(earContext, module.getName(), messageDrivenBean);
            ObjectName activationSpecName = createActivationSpecObjectName(earContext, module.getName(), messageDrivenBean);

            String containerId = messageDrivenObjectName.getCanonicalName();
            GBeanMBean activationSpecGBean = createActivationSpecWrapperGBean(earContext,
                    openejbMessageDrivenBean.isSetActivationConfig() ? openejbMessageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : null,
                    messageDrivenBean.isSetActivationConfig() ? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : new ActivationConfigPropertyType[]{},
                    openejbMessageDrivenBean.getResourceAdapterName(),
                    openejbMessageDrivenBean.getActivationSpecClass(),
                    containerId,
                    cl);
            GBeanMBean messageDrivenGBean = createBean(earContext, ejbModule, containerId, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, security, cl);
            earContext.addGBean(activationSpecName, activationSpecGBean);
            earContext.addGBean(messageDrivenObjectName, messageDrivenGBean);
        }
    }

    public void initContext(ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = OpenEJBModuleBuilder.getJ2eeStringValue(messageDrivenBean.getMessagingType());
            ENCConfigBuilder.assureEJBObjectInterface(messagingType, cl);
        }
    }

    private GBeanMBean createBean(EARContext earContext,
                                  EJBModule ejbModule,
                                  String containerId,
                                  MessageDrivenBeanType messageDrivenBean,
                                  OpenejbMessageDrivenBeanType openejbMessageDrivenBean,
                                  ObjectName activationSpecWrapperName,
                                  TransactionPolicyHelper transactionPolicyHelper,
                                  Security security,
                                  ClassLoader cl) throws DeploymentException {

        if (openejbMessageDrivenBean == null) {
            throw new DeploymentException("openejb-jar.xml required to deploy an mdb");
        }

        String ejbName = messageDrivenBean.getEjbName().getStringValue();

        MDBContainerBuilder builder = new MDBContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(messageDrivenBean.getEjbClass().getStringValue());
        builder.setEndpointInterfaceName(OpenEJBModuleBuilder.getJ2eeStringValue(messageDrivenBean.getMessagingType()));
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        Permissions toBeChecked = new Permissions();
        getModuleBuilder().getSecurityBuilder().fillContainerBuilderSecurity(builder,
                toBeChecked,
                security,
                ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                messageDrivenBean.getEjbName().getStringValue(),
                messageDrivenBean.getSecurityIdentity(),
                null);

        UserTransactionImpl userTransaction;
        //TODO this is probably wrong???

        if ("Bean".equals(messageDrivenBean.getTransactionType().getStringValue())) {
            userTransaction = new UserTransactionImpl();
            builder.setUserTransaction(userTransaction);
            builder.setTransactionPolicySource(TransactionPolicyHelper.StatelessBMTPolicySource);
        } else {
            userTransaction = null;
            TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
            builder.setTransactionPolicySource(transactionPolicySource);
        }

        try {
            ReadOnlyContext compContext = buildComponentContext(earContext, ejbModule, messageDrivenBean, openejbMessageDrivenBean, userTransaction, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName" + ejbName, e);
        }

        setResourceEnvironment(builder, messageDrivenBean.getResourceRefArray(), openejbMessageDrivenBean.getResourceRefArray());

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            gbean.setReferencePattern("ActivationSpecWrapper", activationSpecWrapperName);
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private GBeanMBean createActivationSpecWrapperGBean(EARContext earContext,
                                                        OpenejbActivationConfigPropertyType[] openejbActivationConfigProperties,
                                                        ActivationConfigPropertyType[] activationConfigProperties,
                                                        String resourceAdapterName,
                                                        String activationSpecClass,
                                                        String containerId,
                                                        ClassLoader cl) throws DeploymentException {
        ObjectName resourceAdapterObjectName = null;
        String resourceAdapterModule = earContext.getResourceAdapterModule(resourceAdapterName);
        ActivationSpecInfo activationSpecInfo;
        if (resourceAdapterModule != null) {
            resourceAdapterObjectName = createResourceAdapterObjectName(earContext, resourceAdapterModule, resourceAdapterName);
            activationSpecInfo = (ActivationSpecInfo) earContext.getActivationSpecInfo(resourceAdapterName, activationSpecClass);
        } else {
            Set names = kernel.listGBeans(createResourceAdapterQueryName(earContext, resourceAdapterName));
            if (names.size() != 1) {
                throw new DeploymentException("Unknown or ambiguous resource adapter reference: " + resourceAdapterName + " match count: " + names.size());
            }
            resourceAdapterObjectName = (ObjectName) names.iterator().next();
            Map activationSpecInfos = null;
            try {
                activationSpecInfos = (Map) kernel.getAttribute(resourceAdapterObjectName, "activationSpecInfoMap");
            } catch (Exception e) {
                throw new DeploymentException("Could not get activation spec infos for resource adapter named: " + resourceAdapterObjectName, e);
            }
            activationSpecInfo = (ActivationSpecInfo) activationSpecInfos.get(activationSpecClass);
        }

        GBeanInfo activationSpecGBeanInfo = activationSpecInfo.getActivationSpecGBeanInfo();
        GBeanMBean activationSpecGBean = new GBeanMBean(activationSpecGBeanInfo, cl);
        try {
            activationSpecGBean.setAttribute("activationSpecClass", activationSpecInfo.getActivationSpecClass());
            activationSpecGBean.setAttribute("containerId", containerId);
            activationSpecGBean.setReferencePattern("ResourceAdapterWrapper", resourceAdapterObjectName);
        } catch (ReflectionException e) {
            throw new DeploymentException(e);
        } catch (AttributeNotFoundException e) {
            throw new DeploymentException(e);
        }
        if (openejbActivationConfigProperties != null) {
            for (int i = 0; i < openejbActivationConfigProperties.length; i++) {
                OpenejbActivationConfigPropertyType activationConfigProperty = openejbActivationConfigProperties[i];
                String propertyName = activationConfigProperty.getActivationConfigPropertyName();
                String propertyValue = activationConfigProperty.getActivationConfigPropertyValue();
                try {
                    activationSpecGBean.setAttribute(propertyName, propertyValue);
                } catch (Exception e) {
                    throw new DeploymentException("Could not set property: " + propertyName + " to value: " + propertyValue + " on activationSpec: " + activationSpecClass, e);
                }
            }

        } else {
            for (int i = 0; i < activationConfigProperties.length; i++) {
                ActivationConfigPropertyType activationConfigProperty = activationConfigProperties[i];
                String propertyName = activationConfigProperty.getActivationConfigPropertyName().getStringValue();
                String propertyValue = activationConfigProperty.getActivationConfigPropertyValue().isNil() ? null : activationConfigProperty.getActivationConfigPropertyValue().getStringValue();
                try {
                    activationSpecGBean.setAttribute(propertyName, propertyValue);
                } catch (Exception e) {
                    throw new DeploymentException("Could not set property: " + propertyName + " to value: " + propertyValue + " on activationSpec: " + activationSpecClass, e);
                }
            }
        }
        return activationSpecGBean;
    }

    protected ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = messageDrivenBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = messageDrivenBean.getEjbRefArray();
        GerRemoteRefType[] openejbEjbRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbEjbRefs = openejbMessageDrivenBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = messageDrivenBean.getEjbLocalRefArray();
        GerLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbEjbLocalRefs = openejbMessageDrivenBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = messageDrivenBean.getResourceRefArray();
        GerLocalRefType[] openejbResourceRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbResourceRefs = openejbMessageDrivenBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = messageDrivenBean.getResourceEnvRefArray();
        GerLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbResourceEnvRefs = openejbMessageDrivenBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = messageDrivenBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    private ObjectName createActivationSpecObjectName(EARContext earContext, String moduleName, MessageDrivenBeanType messageDrivenBean) throws DeploymentException {
        String ejbName = messageDrivenBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "ActivationSpec", ejbName);
    }

    private ObjectName createEJBObjectName(EARContext earContext, String moduleName, MessageDrivenBeanType messageDrivenBean) throws DeploymentException {
        String ejbName = messageDrivenBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "MessageDrivenBean", ejbName);
    }

    private ObjectName createResourceAdapterObjectName(EARContext earContext, String moduleName, String resourceAdapterName) throws DeploymentException {
        Properties nameProps = new Properties();
        nameProps.put("j2eeType", "ResourceAdapter");
        nameProps.put("name", resourceAdapterName);
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
        nameProps.put("ResourceAdapterModule", moduleName);

        try {
            return new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }
    }

    ObjectName createResourceAdapterQueryName(EARContext earContext, String resourceAdapterName) throws DeploymentException {
        StringBuffer buffer = new StringBuffer(earContext.getJ2EEDomainName())
                .append(":j2eeType=ResourceAdapter,J2EEServer=")
                .append(earContext.getJ2EEServerName())
                .append(",*,name=").append(resourceAdapterName);

        try {
            return new ObjectName(buffer.toString());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }
    }

}