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

import java.beans.Introspector;
import java.net.URI;
import java.security.Permissions;
import java.util.Map;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
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
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.xbeans.ejbjar.OpenejbActivationConfigPropertyType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;


class MdbBuilder extends BeanBuilder {

    public MdbBuilder(OpenEJBModuleBuilder builder) {
        super(builder);
    }

    protected void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        // Message Driven Beans
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];

            OpenejbMessageDrivenBeanType openejbMessageDrivenBean = (OpenejbMessageDrivenBeanType) openejbBeans.get(messageDrivenBean.getEjbName().getStringValue().trim());
            if (openejbMessageDrivenBean == null) {
                throw new DeploymentException("No openejb deployment descriptor for mdb: " + messageDrivenBean.getEjbName().getStringValue() + ". Known beans: " + openejbBeans.keySet().toArray());
            }
            String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();
            AbstractName messageDrivenObjectName = Naming.createChildName(moduleBaseName, NameFactory.MESSAGE_DRIVEN_BEAN, ejbName);
            AbstractName activationSpecName = Naming.createChildName(messageDrivenObjectName, NameFactory.JCA_ACTIVATION_SPEC, ejbName);

            //TODO configid need canonical form
            String containerId = messageDrivenObjectName.toString();
            addActivationSpecWrapperGBean(earContext,
                    ejbModule.getModuleURI(),
                    activationSpecName,
                    openejbMessageDrivenBean.isSetActivationConfig() ? openejbMessageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : null,
                    messageDrivenBean.isSetActivationConfig() ? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : new ActivationConfigPropertyType[]{},
                    openejbMessageDrivenBean.getResourceAdapter(),
                    getMessagingType(messageDrivenBean),
                    containerId);
            GBeanData messageDrivenGBean = createBean(earContext, ejbModule, containerId, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, cl, componentPermissions, policyContextID);
            messageDrivenGBean.setAbstractName(messageDrivenObjectName);
            try {
                earContext.addGBean(messageDrivenGBean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add message driven bean to context", e);
            }
        }
    }

    public void initContext(ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = OpenEJBModuleBuilder.getJ2eeStringValue(messageDrivenBean.getMessagingType());
            if (messagingType != null) { // That is, not JMS
                try {
                    cl.loadClass(messagingType);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("could not load messaging type: " + messagingType, e);
                }
            }
        }
    }

    private GBeanData createBean(EARContext earContext,
                                 EJBModule ejbModule,
                                 String containerId,
                                 MessageDrivenBeanType messageDrivenBean,
                                 OpenejbMessageDrivenBeanType openejbMessageDrivenBean,
                                 AbstractName activationSpecWrapperName,
                                 TransactionPolicyHelper transactionPolicyHelper,
                                 ClassLoader cl,
                                 ComponentPermissions componentPermissions,
                                 String policyContextID) throws DeploymentException {

        if (openejbMessageDrivenBean == null) {
            throw new DeploymentException("openejb-jar.xml required to deploy an mdb");
        }

        String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();

        MDBContainerBuilder builder = new MDBContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(messageDrivenBean.getEjbClass().getStringValue().trim());
        builder.setEndpointInterfaceName(getMessagingType(messageDrivenBean));
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        SecurityConfiguration securityConfiguration = earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            Permissions toBeChecked = new Permissions();
            ContainerSecurityBuilder containerSecurityBuilder = new ContainerSecurityBuilder();
            String defaultRole = securityConfiguration.getDefaultRole();
            containerSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    null, componentPermissions);

            containerSecurityBuilder.setDetails(messageDrivenBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }

        UserTransactionImpl userTransaction;
        //TODO this is probably wrong???

        if ("Bean".equals(messageDrivenBean.getTransactionType().getStringValue().trim())) {
            userTransaction = new UserTransactionImpl();
            builder.setUserTransaction(userTransaction);
            builder.setTransactionPolicySource(TransactionPolicyHelper.BMTPolicySource);
        } else {
            userTransaction = null;
            TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
            builder.setTransactionPolicySource(transactionPolicySource);
        }

        processEnvironmentRefs(builder, earContext, ejbModule, messageDrivenBean, openejbMessageDrivenBean, userTransaction, cl);

        try {
            GBeanData gbean = builder.createConfiguration();
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            gbean.setReferencePattern("ActivationSpecWrapper", activationSpecWrapperName);
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private String getMessagingType(MessageDrivenBeanType messageDrivenBean) {
        String messageInterfaceType;
        if (messageDrivenBean.isSetMessagingType()) {
            messageInterfaceType = messageDrivenBean.getMessagingType().getStringValue().trim();
        } else {
            messageInterfaceType = "javax.jms.MessageListener";
        }
        return messageInterfaceType;
    }

    private void addActivationSpecWrapperGBean(EARContext earContext,
                                               URI uri,
                                               AbstractName activationSpecName,
                                               OpenejbActivationConfigPropertyType[] openejbActivationConfigProperties,
                                               ActivationConfigPropertyType[] activationConfigProperties,
                                               GerResourceLocatorType resourceAdapter,
                                               String messageListenerInterfaceName,
                                               String containerId) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        AbstractNameQuery resourceAdapterNameQuery = getResourceAdapterId(resourceAdapter);
        AbstractName resourceModuleName;
        try {
            AbstractName resourceAdapterName = earContext.getConfiguration().findGBean(resourceAdapterNameQuery);
            resourceModuleName = getParent(getParent(resourceAdapterName, NameFactory.JCA_RESOURCE), NameFactory.RESOURCE_ADAPTER_MODULE);

        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Can not find resource adapter for message driven bean", e);
        }
        GBeanData activationSpecInfo = refContext.getActivationSpecInfo(resourceModuleName, messageListenerInterfaceName, earContext);

        if (activationSpecInfo == null) {
            throw new DeploymentException("no activation spec found for resource adapter: " + resourceAdapterNameQuery + " and message listener type: " + messageListenerInterfaceName);
        }
        activationSpecInfo = new GBeanData(activationSpecInfo);
        activationSpecInfo.setAttribute("containerId", containerId);
        activationSpecInfo.setReferencePattern("ResourceAdapterWrapper", resourceAdapterNameQuery);
        if (openejbActivationConfigProperties != null) {
            for (int i = 0; i < openejbActivationConfigProperties.length; i++) {
                OpenejbActivationConfigPropertyType activationConfigProperty = openejbActivationConfigProperties[i];
                String propertyName = activationConfigProperty.getActivationConfigPropertyName();
                String propertyValue = activationConfigProperty.getActivationConfigPropertyValue();
                try {
                    activationSpecInfo.setAttribute(Introspector.decapitalize(propertyName), propertyValue);
                } catch (Exception e) {
                    throw new DeploymentException("Could not set property: " + propertyName + " to value: " + propertyValue + " on activationSpec: " + activationSpecInfo.getAttribute("activationSpecClass"), e);
                }
            }

        } else {
            for (int i = 0; i < activationConfigProperties.length; i++) {
                ActivationConfigPropertyType activationConfigProperty = activationConfigProperties[i];
                String propertyName = activationConfigProperty.getActivationConfigPropertyName().getStringValue().trim();
                String propertyValue = activationConfigProperty.getActivationConfigPropertyValue().isNil() ? null : activationConfigProperty.getActivationConfigPropertyValue().getStringValue().trim();
                try {
                    activationSpecInfo.setAttribute(Introspector.decapitalize(propertyName), propertyValue);
                } catch (Exception e) {
                    throw new DeploymentException("Could not set property: " + propertyName + " to value: " + propertyValue + " on activationSpec: " + activationSpecInfo.getAttribute("activationSpecClass"), e);
                }
            }
        }
        activationSpecInfo.setAbstractName(activationSpecName);
        try {
            earContext.addGBean(activationSpecInfo);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add activation spec gbean to context", e);
        }
    }

    private AbstractName getParent(AbstractName childName, String typeKey) throws DeploymentException {
        Map nameMap = childName.getName();
        String name = (String) nameMap.remove(typeKey);
        nameMap.put(NameFactory.J2EE_TYPE, typeKey);
        nameMap.put(NameFactory.J2EE_NAME, name);
        ObjectName parentObjectName;
        try {
            parentObjectName = ObjectName.getInstance(childName.getObjectName().getDomain(), new Hashtable(nameMap));
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct parent name", e);
        }
        return new AbstractName(childName.getArtifact(), nameMap, parentObjectName);
    }

    private static AbstractNameQuery getResourceAdapterId(GerResourceLocatorType resourceLocator) {
        if (resourceLocator.isSetResourceLink()) {
            return ENCConfigBuilder.buildAbstractNameQuery(null, NameFactory.JCA_RESOURCE_ADAPTER, resourceLocator.getResourceLink().trim());
        }
        //construct name from components
        return ENCConfigBuilder.buildAbstractNameQuery(resourceLocator.getPattern(), NameFactory.JCA_RESOURCE_ADAPTER);
    }

    protected void processEnvironmentRefs(MDBContainerBuilder builder, EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        // env entries
        EnvEntryType[] envEntries = messageDrivenBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = messageDrivenBean.getEjbRefArray();
        GerEjbRefType[] openejbEjbRefs = null;

        EjbLocalRefType[] ejbLocalRefs = messageDrivenBean.getEjbLocalRefArray();
        GerEjbLocalRefType[] openejbEjbLocalRefs = null;

        // resource refs
        ResourceRefType[] resourceRefs = messageDrivenBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = messageDrivenBean.getResourceEnvRefArray();
        GerResourceEnvRefType[] openejbResourceEnvRefs = null;

        ServiceRefType[] serviceRefs = messageDrivenBean.getServiceRefArray();
        GerServiceRefType[] openejbServiceRefs = null;

        //get arrays from openejb plan if present
        if (openejbMessageDrivenBean != null) {
            openejbEjbRefs = openejbMessageDrivenBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbMessageDrivenBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbMessageDrivenBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbMessageDrivenBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbMessageDrivenBean.getServiceRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = messageDrivenBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext, null, ejbModule, userTransaction, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, serviceRefs, openejbServiceRefs, cl);
        builder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(earContext, ejbModule.getModuleURI(), builder, resourceRefs, openejbResourceRefs);

    }

}