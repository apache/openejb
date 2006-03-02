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
import java.util.SortedMap;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefType;
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
import org.openejb.xbeans.ejbjar.OpenejbActivationConfigPropertyType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;


public class XmlBeansMdbBuilder extends XmlBeanBuilder {
    private final ObjectName defaultMdbEjbContainer;

    public XmlBeansMdbBuilder(OpenEjbModuleBuilder moduleBuilder, ObjectName defaultMdbEjbContainer) {
        super(moduleBuilder);
        this.defaultMdbEjbContainer = defaultMdbEjbContainer;
    }

    protected void buildBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        // Message Driven Beans
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];

            OpenejbMessageDrivenBeanType openejbMessageDrivenBean = (OpenejbMessageDrivenBeanType) openejbBeans.get(messageDrivenBean.getEjbName().getStringValue().trim());
            if (openejbMessageDrivenBean == null) {
                throw new DeploymentException("No openejb deployment descriptor for mdb: " + messageDrivenBean.getEjbName().getStringValue() + ". Known beans: " + openejbBeans.keySet().toArray());
            }
            String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();
            ObjectName messageDrivenObjectName = null;
            ObjectName activationSpecName = null;
            try {
                messageDrivenObjectName = NameFactory.getEjbComponentName(null, null, null, null, ejbName, NameFactory.MESSAGE_DRIVEN_BEAN, moduleJ2eeContext);
                activationSpecName = NameFactory.getEjbComponentName(null, null, null, null, ejbName, NameFactory.JCA_ACTIVATION_SPEC, moduleJ2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct object name: " + ejbName, e);
            }

            addActivationSpecWrapperGBean(earContext,
                    ejbModule.getModuleURI(),
                    activationSpecName,
                    openejbMessageDrivenBean.isSetActivationConfig() ? openejbMessageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : null,
                    messageDrivenBean.isSetActivationConfig() ? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : new ActivationConfigPropertyType[]{},
                    openejbMessageDrivenBean.getResourceAdapter(),
                    getMessagingType(messageDrivenBean),
                    messageDrivenObjectName.getCanonicalName());
            GBeanData messageDrivenGBean = createBean(earContext, ejbModule, messageDrivenObjectName, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, cl, componentPermissions, policyContextID);
            messageDrivenGBean.setName(messageDrivenObjectName);
            earContext.addGBean(messageDrivenGBean);
        }
    }

    public void initContext(ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = OpenEjbModuleBuilder.getJ2eeStringValue(messageDrivenBean.getMessagingType());
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
            ObjectName containerId,
            MessageDrivenBeanType messageDrivenBean,
            OpenejbMessageDrivenBeanType openejbMessageDrivenBean,
            ObjectName activationSpecWrapperName,
            TransactionPolicyHelper transactionPolicyHelper,
            ClassLoader cl,
            ComponentPermissions componentPermissions,
            String policyContextID) throws DeploymentException {

        if (openejbMessageDrivenBean == null) {
            throw new DeploymentException("openejb-jar.xml required to deploy an mdb");
        }

        String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();

        MdbBuilder builder = new MdbBuilder();
        builder.setContainerId(containerId);
        builder.setEjbName(ejbName);

        builder.setEndpointInterfaceName(getMessagingType(messageDrivenBean));
        builder.setBeanClassName(messageDrivenBean.getEjbClass().getStringValue().trim());

        builder.setActivationSpecName(activationSpecWrapperName);

        builder.setEjbContainerName(defaultMdbEjbContainer);

        SecurityConfiguration securityConfiguration = earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            Permissions toBeChecked = new Permissions();
            XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
            String defaultRole = securityConfiguration.getDefaultRole();
            xmlBeansSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    null, componentPermissions);

            xmlBeansSecurityBuilder.setDetails(messageDrivenBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }

        boolean beanManagedTransactions = new Boolean("Bean".equals(messageDrivenBean.getTransactionType().getStringValue().trim())).booleanValue();
        builder.setBeanManagedTransactions(beanManagedTransactions);

        if (!beanManagedTransactions) {
            SortedMap transactionPolicies = transactionPolicyHelper.getTransactionPolicies(ejbName);
            builder.setTransactionPolicies(transactionPolicies);
        }

        processEnvironmentRefs(builder, earContext, ejbModule, messageDrivenBean, openejbMessageDrivenBean, cl);

        try {
            GBeanData gbean = builder.createConfiguration();
            gbean.setReferencePattern("ActivationSpecWrapper", activationSpecWrapperName);
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private String getMessagingType(MessageDrivenBeanType messageDrivenBean) {
        String messageInterfaceType = null;
        if (messageDrivenBean.isSetMessagingType()) {
            messageInterfaceType = messageDrivenBean.getMessagingType().getStringValue().trim();
        } else {
            messageInterfaceType = "javax.jms.MessageListener";
        }
        return messageInterfaceType;
    }

    private void addActivationSpecWrapperGBean(EARContext earContext,
            URI uri,
            ObjectName activationSpecName,
            OpenejbActivationConfigPropertyType[] openejbActivationConfigProperties,
            ActivationConfigPropertyType[] activationConfigProperties,
            GerResourceLocatorType resourceAdapter,
            String messageListenerInterfaceName,
            String containerId) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        ObjectName resourceAdapterObjectName = getResourceAdapterId(uri, resourceAdapter, earContext);
        J2eeContext resourceAdapterJ2eeContext = J2eeContextImpl.newContext(resourceAdapterObjectName, NameFactory.JCA_RESOURCE);
        ObjectName resourceModuleObjectName = null;
        try {
            //N.B. the resource adapter name has module type "JCAResource" but the resource adapter module has module type "ResourceAdapterModule"
            resourceModuleObjectName = NameFactory.getModuleName(null, null, null, NameFactory.RESOURCE_ADAPTER_MODULE, null, resourceAdapterJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct resource module name", e);
        }
        GBeanData activationSpecInfo = refContext.getActivationSpecInfo(resourceModuleObjectName, messageListenerInterfaceName, earContext);

        if (activationSpecInfo == null) {
            throw new DeploymentException("no activation spec found for resource adapter: " + resourceAdapterObjectName + " and message listener type: " + messageListenerInterfaceName);
        }
        activationSpecInfo = new GBeanData(activationSpecInfo);
        activationSpecInfo.setAttribute("containerId", containerId);
        activationSpecInfo.setReferencePattern("ResourceAdapterWrapper", resourceAdapterObjectName);
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
        activationSpecInfo.setName(activationSpecName);
        earContext.addGBean(activationSpecInfo);
    }

    private static ObjectName getResourceAdapterId(URI uri, GerResourceLocatorType resourceLocator, EARContext earContext) throws DeploymentException {
        try {
            if (resourceLocator.isSetResourceLink()) {
                String containerId = earContext.getRefContext().getResourceAdapterContainerId(uri, resourceLocator.getResourceLink(), earContext);
                return ObjectName.getInstance(containerId);
            } else if (resourceLocator.isSetTargetName()) {
                String containerId = resourceLocator.getTargetName();
                return ObjectName.getInstance(containerId);
            }
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct connector name", e);
        }
        //construct name from components
        try {
            return NameFactory.getComponentName(resourceLocator.getDomain(),
                    resourceLocator.getServer(),
                    resourceLocator.getApplication(),
                    NameFactory.JCA_RESOURCE,
                    resourceLocator.getModule(),
                    resourceLocator.getName(),
                    NameFactory.JCA_RESOURCE_ADAPTER,
                    earContext.getJ2eeContext());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct resource adapter object name", e);
        }
    }

    protected void processEnvironmentRefs(MdbBuilder builder, EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, ClassLoader cl) throws DeploymentException {
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

        GerGbeanRefType[] gBeanRefs = null;

        //get arrays from openejb plan if present
        if (openejbMessageDrivenBean != null) {
            openejbEjbRefs = openejbMessageDrivenBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbMessageDrivenBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbMessageDrivenBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbMessageDrivenBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbMessageDrivenBean.getServiceRefArray();
            gBeanRefs = openejbMessageDrivenBean.getGbeanRefArray(); 
        }

        MessageDestinationRefType[] messageDestinationRefs = messageDrivenBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext, 
                null, 
                ejbModule, 
                null, 
                envEntries, 
                ejbRefs, openejbEjbRefs, 
                ejbLocalRefs, openejbEjbLocalRefs, 
                resourceRefs, openejbResourceRefs, 
                resourceEnvRefs, openejbResourceEnvRefs, 
                messageDestinationRefs, 
                serviceRefs, openejbServiceRefs,
                gBeanRefs,
                cl);
        builder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(earContext, ejbModule.getModuleURI(), builder, resourceRefs, openejbResourceRefs);
    }
}