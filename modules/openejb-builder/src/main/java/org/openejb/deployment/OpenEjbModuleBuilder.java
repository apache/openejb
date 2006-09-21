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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.EJBModuleImplGBean;
import org.openejb.EjbDeployment;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.pkgen.EjbKeyGeneratorDocument;


/**
 * Master builder for processing EJB JAR deployments and creating the
 * correspinding runtime objects (GBeans, etc.).
 *
 * @version $Revision$ $Date$
 */
public class OpenEjbModuleBuilder implements ModuleBuilder {

    private static QName OPENEJBJAR_QNAME = OpenejbOpenejbJarDocument.type.getDocumentElementName();
    private static final String OPENEJBJAR_NAMESPACE = OPENEJBJAR_QNAME.getNamespaceURI();

    private final Environment defaultEnvironment;

    private final AbstractNameQuery listener;
    private final XmlBeansSessionBuilder xmlBeansSessionBuilder;
    private final XmlBeansEntityBuilder xmlBeansEntityBuilder;
    private final CmpSchemaBuilder cmpSchemaBuilder;
    private final XmlBeansMdbBuilder xmlBeansMdbBuilder;
    private final SingleElementCollection webServiceBuilder;
    private final NamespaceDrivenBuilderCollection securityBuilders;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final NamingBuilder namingBuilders;
    private final ActivationSpecInfoLocator activationSpecInfoLocator;


    static {
        Map conversions = new HashMap();
        QName name = EjbKeyGeneratorDocument.type.getDocumentElementName();
        conversions.put(name.getLocalPart(), new NamespaceElementConverter(name.getNamespaceURI()));
        SchemaConversionUtils.registerNamespaceConversions(conversions);
    }

    public OpenEjbModuleBuilder(Environment defaultEnvironment,
            String defaultStatelessEjbContainer,
            String defaultStatefulEjbContainer,
            String defaultBmpEjbContainer,
            String defaultCmpEjbContainer,
            String defaultMdbEjbContainer,
            AbstractNameQuery listener,
            Object webServiceLinkTemplate,
            Collection webServiceBuilder,
            NamespaceDrivenBuilder securityBuilder,
            NamespaceDrivenBuilder serviceBuilder,
            NamingBuilder namingBuilders,
            ActivationSpecInfoLocator activationSpecInfoLocator,
            Kernel kernel) throws GBeanNotFoundException {

        this(defaultEnvironment,
                defaultStatelessEjbContainer,
                defaultStatefulEjbContainer,
                defaultBmpEjbContainer,
                defaultCmpEjbContainer,
                defaultMdbEjbContainer,
                listener,
                getLinkData(kernel, webServiceLinkTemplate),
                new SingleElementCollection(webServiceBuilder),
                securityBuilder == null ? Collections.EMPTY_SET : Collections.singleton(securityBuilder),
                serviceBuilder == null ? Collections.EMPTY_SET : Collections.singleton(serviceBuilder),
                namingBuilders,
                activationSpecInfoLocator, kernel);
    }

    public OpenEjbModuleBuilder(Environment defaultEnvironment,
            String defaultStatelessEjbContainer,
            String defaultStatefulEjbContainer,
            String defaultBmpEjbContainer,
            String defaultCmpEjbContainer,
            String defaultMdbEjbContainer,
            AbstractNameQuery listener,
            GBeanData linkTemplate,
            WebServiceBuilder webServiceBuilder,
            NamespaceDrivenBuilder securityBuilder,
            NamespaceDrivenBuilder serviceBuilder,
            NamingBuilder namingBuilders,
            ActivationSpecInfoLocator activationSpecInfoLocator,
            Kernel kernel) {

        this(defaultEnvironment,
                defaultStatelessEjbContainer,
                defaultStatefulEjbContainer,
                defaultBmpEjbContainer,
                defaultCmpEjbContainer,
                defaultMdbEjbContainer,
                listener,
                linkTemplate,
                new SingleElementCollection(webServiceBuilder),
                securityBuilder == null ? Collections.EMPTY_SET : Collections.singleton(securityBuilder),
                serviceBuilder == null ? Collections.EMPTY_SET : Collections.singleton(serviceBuilder),
                namingBuilders,
                activationSpecInfoLocator,
                kernel);
    }

    public OpenEjbModuleBuilder(Environment defaultEnvironment,
            String defaultStatelessEjbContainer,
            String defaultStatefulEjbContainer,
            String defaultBmpEjbContainer,
            String defaultCmpEjbContainer,
            String defaultMdbEjbContainer,
            AbstractNameQuery listener,
            GBeanData linkTemplate,
            SingleElementCollection webServiceBuilder,
            Collection securityBuilders,
            Collection serviceBuilders,
            NamingBuilder namingBuilders,
            ActivationSpecInfoLocator activationSpecInfoLocator, Kernel kernel) {
        this.defaultEnvironment = defaultEnvironment;

        this.listener = listener;
        this.xmlBeansSessionBuilder = new XmlBeansSessionBuilder(this, defaultStatelessEjbContainer, defaultStatefulEjbContainer, linkTemplate);
        this.xmlBeansEntityBuilder = new XmlBeansEntityBuilder(this, defaultBmpEjbContainer, defaultCmpEjbContainer);
        this.cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
        this.xmlBeansMdbBuilder = new XmlBeansMdbBuilder(this, kernel, defaultMdbEjbContainer);
        this.webServiceBuilder = webServiceBuilder;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders);
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.namingBuilders = namingBuilders;
        this.activationSpecInfoLocator = activationSpecInfoLocator;
    }

    public WebServiceBuilder getWebServiceBuilder() {
        return (WebServiceBuilder) webServiceBuilder.getElement();
    }

    public NamingBuilder getNamingBuilders() {
        return namingBuilders;
    }

    private static GBeanData getLinkData(Kernel kernel, Object webServiceLinkTemplate) throws GBeanNotFoundException {
        AbstractName webServiceLinkTemplateName = kernel.getAbstractNameFor(webServiceLinkTemplate);
        return kernel.getGBeanData(webServiceLinkTemplateName);
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "ejb", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        String specDD;
        EjbJarType ejbJar;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/ejb-jar.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            return null;
        }
        //there is a file named ejb-jar.xml in META-INF.  If we can't process it, it is an error.
        try {
            // parse it
            EjbJarDocument ejbJarDoc = SchemaConversionUtils.convertToEJBSchema(XmlBeansUtil.parse(specDD));
            ejbJar = ejbJarDoc.getEjbJar();
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing ejb-jar.xml", e);
        }

        boolean standAlone = earEnvironment == null;
        OpenejbOpenejbJarType openejbJar = getOpenejbJar(plan, moduleFile, standAlone, targetPath, ejbJar);
        if (openejbJar == null)
        { // Avoid NPE GERONIMO-1220; todo: remove this if we can work around the requirement for a plan
            throw new DeploymentException("Currently a Geronimo deployment plan is required for an EJB module.  Please provide a plan as a deployer argument or packaged in the EJB JAR at META-INF/openejb-jar.xml");
        }

        EnvironmentType environmentType = openejbJar.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        if (earEnvironment != null) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, environment);
            environment = earEnvironment;
            if (!environment.getConfigId().isResolved()) {
                throw new IllegalStateException("EJB module ID should be fully resolved (not " + environment.getConfigId() + ")");
            }
        } else {
            idBuilder.resolve(environment, new File(moduleFile.getName()).getName(), "jar");
        }

        if (ejbJar.isSetAssemblyDescriptor()) {
            AssemblyDescriptorType assemblyDescriptor = ejbJar.getAssemblyDescriptor();
            namingBuilders.buildEnvironment(assemblyDescriptor, openejbJar, environment);
        }

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.EJB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.EJB_MODULE);
        }

        return new EJBModule(standAlone, moduleName, environment, moduleFile, targetPath, ejbJar, openejbJar, specDD);
    }

    OpenejbOpenejbJarType getOpenejbJar(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, EjbJarType ejbJar) throws DeploymentException {
        OpenejbOpenejbJarType openejbJar;
        XmlObject rawPlan = null;
        try {
            // load the openejb-jar.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURL(), getClass().getClassLoader());
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/openejb-jar.xml");
                        rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                    }
                }
            } catch (IOException e) {
                //no plan, create a default
            }

            // if we got one extract, adjust, and validate it otherwise create a default one
            if (rawPlan != null) {
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, OPENEJBJAR_QNAME, OpenejbOpenejbJarType.type);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                openejbJar = createDefaultPlan(path, ejbJar);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
        return openejbJar;
    }

    private OpenejbOpenejbJarType createDefaultPlan(String name, EjbJarType ejbJar) {
        String id = ejbJar.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        OpenejbOpenejbJarType openejbEjbJar = OpenejbOpenejbJarType.Factory.newInstance();
        //TODO add a module id
        openejbEjbJar.addNewEnterpriseBeans();
        return openejbEjbJar;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        try {
            // extract the ejbJar file into a standalone packed jar file and add the contents to the output
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy ejb module jar into configuration: " + moduleFile.getName());
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        AbstractName moduleBaseName = module.getModuleName();
        URI moduleUri = module.getModuleURI();

        EJBModule ejbModule = (EJBModule) module;
        ejbModule.setEarContext(earContext);
        ejbModule.setRootEarContext(earContext);
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();

        if (ejbJar.isSetAssemblyDescriptor()) {
            AssemblyDescriptorType assemblyDescriptor = ejbJar.getAssemblyDescriptor();
            OpenejbOpenejbJarType openejbJar = (OpenejbOpenejbJarType) module.getVendorDD();
            namingBuilders.initContext(assemblyDescriptor, openejbJar, module.getEarContext().getConfiguration(), earContext.getConfiguration(), module);
        }

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        xmlBeansSessionBuilder.initContext(earContext, moduleBaseName, moduleUri, cl, enterpriseBeans);
        xmlBeansEntityBuilder.initContext(earContext, moduleBaseName, moduleUri, cl, enterpriseBeans);
        xmlBeansMdbBuilder.initContext(cl, enterpriseBeans);
        cmpSchemaBuilder.initContext(earContext, ejbModule, cl);

        /**
         * Build the security configuration.  Attempt to auto generate role mappings.
         */
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        securityBuilders.build(openejbEjbJar, earContext, ejbModule.isStandAlone() ? module.getEarContext() : null);
        serviceBuilders.build(openejbEjbJar, earContext, module.getEarContext());
    }

    public XmlBeansEntityBuilder getBmpEntityBuilder() {
        return xmlBeansEntityBuilder;
    }

    public XmlBeansMdbBuilder getMdbBuilder() {
        return xmlBeansMdbBuilder;
    }

    public XmlBeansSessionBuilder getSessionBuilder() {
        return xmlBeansSessionBuilder;
    }

    /**
     * Does the meaty work of processing the deployment information and
     * creating GBeans for all the EJBs in the JAR, etc.
     */
    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repositories) throws DeploymentException {
        AbstractName moduleBaseName = module.getModuleName();

        EJBModule ejbModule = (EJBModule) module;
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) module.getSpecDD();

        cmpSchemaBuilder.addBeans(earContext, ejbModule, cl);

//        GbeanType[] gbeans = openejbEjbJar.getGbeanArray();
//        ServiceConfigBuilder.addGBeans(gbeans, cl, moduleBaseName, earContext);

        GBeanData ejbModuleGBeanData = new GBeanData(moduleBaseName, EJBModuleImplGBean.GBEAN_INFO);
        try {
            ejbModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerName());
            if (!module.isStandAlone()) {
                ejbModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getModuleName());
            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

            ejbModuleGBeanData.setReferencePatterns("EJBCollection",
                    new ReferencePatterns(new AbstractNameQuery(null,
                            Collections.singletonMap(NameFactory.EJB_MODULE, moduleBaseName.getNameProperty(NameFactory.J2EE_NAME)),
                            EjbDeployment.class.getName())));

            earContext.addGBean(ejbModuleGBeanData);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean " + ejbModuleGBeanData.getAbstractName(), e);
        }

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();
        Set beans = new HashSet();
        EntityBeanType[] ebs = enterpriseBeans.getEntityArray();
        for (int i = 0; i < ebs.length; i++) {
            beans.add(ebs[i].getEjbName().getStringValue().trim());
        }
        SessionBeanType[] sbs = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sbs.length; i++) {
            beans.add(sbs[i].getEjbName().getStringValue().trim());
        }
        MessageDrivenBeanType[] mbs = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < mbs.length; i++) {
            beans.add(mbs[i].getEjbName().getStringValue().trim());
        }

        // create an index of the openejb ejb configurations by ejb-name
        Map openejbBeans = new HashMap();
        List badBeans = new ArrayList();
        //overridden web service locations
        Map correctedPortLocations = new HashMap();

        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            if (beans.contains(sessionBean.getEjbName())) {
                openejbBeans.put(sessionBean.getEjbName(), sessionBean);
                if (sessionBean.isSetWebServiceAddress()) {
                    String location = sessionBean.getWebServiceAddress().trim();
                    correctedPortLocations.put(sessionBean.getEjbName(), location);
                }
            } else {
                badBeans.add(sessionBean.getEjbName());
            }
        }
        OpenejbEntityBeanType[] openejbEntityBeans = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openejbEntityBeans.length; i++) {
            OpenejbEntityBeanType entityBean = openejbEntityBeans[i];
            if (beans.contains(entityBean.getEjbName())) {
                openejbBeans.put(entityBean.getEjbName(), entityBean);
            } else {
                badBeans.add(entityBean.getEjbName());
            }
        }
        OpenejbMessageDrivenBeanType[] openejbMessageDrivenBeans = openejbEjbJar.getEnterpriseBeans().getMessageDrivenArray();
        for (int i = 0; i < openejbMessageDrivenBeans.length; i++) {
            OpenejbMessageDrivenBeanType messageDrivenBean = openejbMessageDrivenBeans[i];
            if (beans.contains(messageDrivenBean.getEjbName())) {
                openejbBeans.put(messageDrivenBean.getEjbName(), messageDrivenBean);
            } else {
                badBeans.add(messageDrivenBean.getEjbName());
            }
        }

        if (badBeans.size() > 0) {
            if (badBeans.size() == 1) {
                throw new DeploymentException("EJB '" + badBeans.get(0) + "' is described in OpenEJB deployment plan but does not exist in META-INF/ejb-jar.xml");
            }
            StringBuffer buf = new StringBuffer();
            buf.append("The following EJBs are described in the OpenEJB deployment plan but do not exist in META-INF/ejb-jar.xml: ");
            for (int i = 0; i < badBeans.size(); i++) {
                if (i > 0) buf.append(", ");
                buf.append(badBeans.get(i));
            }
            throw new DeploymentException(buf.toString());
        }

        Map portInfoMap = getWebServiceBuilder().findWebServices(ejbModule.getModuleFile(), true, correctedPortLocations);

        TransactionPolicyHelper transactionPolicyHelper;
        if (ejbJar.isSetAssemblyDescriptor()) {
            transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());
        } else {
            transactionPolicyHelper = new TransactionPolicyHelper();
        }

        ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), new Permissions(), new HashMap());
        //TODO go back to the commented version when possible
//          String contextID = ejbModuleObjectName.getCanonicalName();
        String policyContextID = moduleBaseName.toString().replaceAll("[,: ]", "_");

        xmlBeansSessionBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, componentPermissions, openejbBeans, transactionPolicyHelper, enterpriseBeans, listener, policyContextID, portInfoMap);

        xmlBeansEntityBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, openejbBeans, componentPermissions, transactionPolicyHelper, enterpriseBeans, policyContextID);

        xmlBeansMdbBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, openejbBeans, transactionPolicyHelper, enterpriseBeans, componentPermissions, policyContextID);

        earContext.addSecurityContext(policyContextID, componentPermissions);
    }

    public String getSchemaNamespace() {
        return OPENEJBJAR_NAMESPACE;
    }

    public static AbstractNameQuery getResourceContainerId(GerResourceLocatorType resourceLocator, EARContext earContext) throws GBeanNotFoundException {
        AbstractNameQuery resourceQuery;
        if (resourceLocator.isSetResourceLink()) {
            resourceQuery = ENCConfigBuilder.buildAbstractNameQuery(null, null, resourceLocator.getResourceLink().trim(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            //construct name from components
            resourceQuery = ENCConfigBuilder.buildAbstractNameQuery(resourceLocator.getPattern(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, NameFactory.RESOURCE_ADAPTER_MODULE, null);
        }
        Configuration configuration = earContext.getConfiguration();
        //throws GBeanNotFoundException if not satisfied
        configuration.findGBean(resourceQuery);
        return resourceQuery;
    }


    public Object createEJBProxyFactory(String containerId, boolean isSessionBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws DeploymentException {
        Class remoteInterface = loadClass(cl, remoteInterfaceName);
        Class homeInterface = loadClass(cl, homeInterfaceName);
        Class localInterface = loadClass(cl, localInterfaceName);
        Class localHomeInterface = loadClass(cl, localHomeInterfaceName);
        return new EJBProxyFactory(containerId, isSessionBean, remoteInterface, homeInterface, localInterface, localHomeInterface);
    }

    private Class loadClass(ClassLoader cl, String name) throws DeploymentException {
        if (name == null) {
            return null;
        }
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to load Class: " + name);
        }
    }

    protected static String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true);
        infoBuilder.addAttribute("defaultStatelessEjbContainer", String.class, true);
        infoBuilder.addAttribute("defaultStatefulEjbContainer", String.class, true);
        infoBuilder.addAttribute("defaultBmpEjbContainer", String.class, true);
        infoBuilder.addAttribute("defaultCmpEjbContainer", String.class, true);
        infoBuilder.addAttribute("defaultMdbEjbContainer", String.class, true);
        infoBuilder.addAttribute("listener", AbstractNameQuery.class, true);
        infoBuilder.addReference("WebServiceLinkTemplate", Object.class, NameFactory.WEB_SERVICE_LINK);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("SecurityBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ActivationSpecInfoLocator", ActivationSpecInfoLocator.class, NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "defaultStatelessEjbContainer",
                "defaultStatefulEjbContainer",
                "defaultBmpEjbContainer",
                "defaultCmpEjbContainer",
                "defaultMdbEjbContainer",
                "listener",
                "WebServiceLinkTemplate",
                "WebServiceBuilder",
                "SecurityBuilders",
                "ServiceBuilders",
                "NamingBuilders",
                "ActivationSpecInfoLocator",
                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public ActivationSpecInfoLocator getActivationSpecInfoLocator() {
        return activationSpecInfoLocator;
    }
}
