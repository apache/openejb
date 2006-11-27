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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.openejb.EJBModuleImplGBean;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.apache.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.apache.openejb.xbeans.pkgen.EjbKeyGeneratorDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


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
    private final Collection webServiceBuilders;
    private final NamespaceDrivenBuilderCollection securityBuilders;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final NamingBuilder namingBuilders;
    private final ActivationSpecInfoLocator activationSpecInfoLocator;
    private final ResourceEnvironmentSetter resourceEnvironmentSetter;
    private static final QName CMP_VERSION = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-version");


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
            Collection securityBuilders,
            Collection serviceBuilders,
            NamingBuilder namingBuilders,
            ResourceEnvironmentSetter resourceEnvironmentSetter,
            ActivationSpecInfoLocator activationSpecInfoLocator,
            Kernel kernel) throws GBeanNotFoundException {

        this(defaultEnvironment,
                defaultStatelessEjbContainer,
                defaultStatefulEjbContainer,
                defaultBmpEjbContainer,
                defaultCmpEjbContainer,
                defaultMdbEjbContainer,
                listener,
                getLinkData(kernel,
                        webServiceLinkTemplate),
                webServiceBuilder,
                securityBuilders == null ? Collections.EMPTY_SET : securityBuilders,
                serviceBuilders == null ? Collections.EMPTY_SET : serviceBuilders,
                namingBuilders,
                resourceEnvironmentSetter,
                activationSpecInfoLocator,
                kernel);
    }

    //note different constructor argument order to avoid confusing GBeanInfo
    public OpenEjbModuleBuilder(String defaultStatelessEjbContainer,
            String defaultStatefulEjbContainer,
            String defaultBmpEjbContainer,
            String defaultCmpEjbContainer,
            String defaultMdbEjbContainer,
            Environment defaultEnvironment,
            AbstractNameQuery listener,
            GBeanData linkTemplate,
            WebServiceBuilder webServiceBuilder,
            NamespaceDrivenBuilder securityBuilder,
            NamespaceDrivenBuilder serviceBuilder,
            NamingBuilder namingBuilders,
            ResourceEnvironmentSetter resourceEnvironmentSetter,
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
                Collections.singleton(webServiceBuilder),
                securityBuilder == null ? Collections.EMPTY_SET : Collections.singleton(securityBuilder),
                serviceBuilder == null ? Collections.EMPTY_SET : Collections.singleton(serviceBuilder),
                namingBuilders,
                resourceEnvironmentSetter,
                activationSpecInfoLocator,
                kernel);
    }

    OpenEjbModuleBuilder(Environment defaultEnvironment,
            String defaultStatelessEjbContainer,
            String defaultStatefulEjbContainer,
            String defaultBmpEjbContainer,
            String defaultCmpEjbContainer,
            String defaultMdbEjbContainer,
            AbstractNameQuery listener,
            GBeanData linkTemplate,
            Collection webServiceBuilder,
            Collection securityBuilders,
            Collection serviceBuilders,
            NamingBuilder namingBuilders,
            ResourceEnvironmentSetter resourceEnvironmentSetter,
            ActivationSpecInfoLocator activationSpecInfoLocator,
            Kernel kernel) {
        this.defaultEnvironment = defaultEnvironment;

        this.listener = listener;
        this.xmlBeansSessionBuilder = new XmlBeansSessionBuilder(this, defaultStatelessEjbContainer, defaultStatefulEjbContainer, linkTemplate);
        this.xmlBeansEntityBuilder = new XmlBeansEntityBuilder(this, defaultBmpEjbContainer, defaultCmpEjbContainer);
        this.cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
        this.xmlBeansMdbBuilder = new XmlBeansMdbBuilder(this, kernel, defaultMdbEjbContainer);
        this.webServiceBuilders = webServiceBuilder;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders, GerSecurityDocument.type.getDocumentElementName());
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders, GBeanBuilder.SERVICE_QNAME);
        this.namingBuilders = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
        this.activationSpecInfoLocator = activationSpecInfoLocator;
    }

    public Collection getWebServiceBuilders() {
        return webServiceBuilders;
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
            EjbJarDocument ejbJarDoc = convertToEJBSchema(XmlBeansUtil.parse(specDD));
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

        //overridden web service locations
        Map correctedPortLocations = new HashMap();

        OpenejbSessionBeanType[] openejbSessionBeans = openejbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
                if (sessionBean.isSetWebServiceAddress()) {
                    String location = sessionBean.getWebServiceAddress().trim();
                    correctedPortLocations.put(sessionBean.getEjbName(), location);
                }
        }
        Map sharedContext = new HashMap();
        for (Iterator iterator = webServiceBuilders.iterator(); iterator.hasNext();) {
            WebServiceBuilder serviceBuilder = (WebServiceBuilder) iterator.next();
            serviceBuilder.findWebServices(moduleFile, true, correctedPortLocations, environment, sharedContext);
        }

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.EJB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.EJB_MODULE);
        }

        return new EJBModule(standAlone, moduleName, environment, moduleFile, targetPath, ejbJar, openejbJar, specDD, sharedContext);
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

    public ActivationSpecInfoLocator getActivationSpecInfoLocator() {
        return activationSpecInfoLocator;
    }

    public ResourceEnvironmentSetter getResourceEnvironmentSetter() {
        return resourceEnvironmentSetter;
    }

    public static EjbJarDocument convertToEJBSchema(XmlObject xmlObject) throws XmlException {
        if (EjbJarDocument.type.equals(xmlObject.schemaType())) {
            XmlBeansUtil.validateDD(xmlObject);
            return (EjbJarDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        //cursor is intially located before the logical STARTDOC token
        try {
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                XmlObject result = xmlObject.changeType(EjbJarDocument.type);
                XmlBeansUtil.validateDD(result);
                return (EjbJarDocument) result;
            }
            // deployment descriptor is probably in EJB 1.1 or 2.0 format
            XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
            String publicId = xmlDocumentProperties.getDoctypePublicId();
            String cmpVersion;
            if ("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN".equals(publicId)) {
                cmpVersion = "1.x";
            } else if ("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN".equals(publicId)) {
                cmpVersion = null;//2.x is the default "2.x";
            } else {
                throw new XmlException("Unrecognized document type: " + publicId);
            }
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version);
            //play with message-driven
            cursor.toStartDoc();
            convertBeans(cursor, moveable, cmpVersion);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(EjbJarDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (EjbJarDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (EjbJarDocument) xmlObject;
    }

    private static void convertBeans(XmlCursor cursor, XmlCursor moveable, String cmpVersion) {
        cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "ejb-jar");
        cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "enterprise-beans");
        if (cursor.toFirstChild()) {
            //there's at least one ejb...
            do {
                cursor.push();
                String type = cursor.getName().getLocalPart();
                if ("session".equals(type)) {
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                    cursor.toNextSibling();
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                } else if ("entity".equals(type)) {
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "persistence-type");
                    String persistenceType = cursor.getTextValue();
                    //reentrant is the last required tag before jndiEnvironmentRefsGroup
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "reentrant");
                    //Convert 2.0 True/False to true/false for 2.1
                    cursor.setTextValue(cursor.getTextValue().toLowerCase());
                    if (cmpVersion != null && !cursor.toNextSibling(CMP_VERSION) && "Container".equals(persistenceType)) {
                        cursor.toNextSibling();
                        cursor.insertElementWithText(CMP_VERSION, cmpVersion);
                    }

                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "abstract-schema-name");
                    while (cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-field")) {
                    }
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "primkey-field");
                    cursor.toNextSibling();
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                } else if ("message-driven".equals(type)) {
                    cursor.toFirstChild();
                    if (cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "messaging-type")) {
                        cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                    } else {
                        cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-type");
                        //insert messaging-type (introduced in EJB 2.1 spec) before transaction-type
                        cursor.insertElementWithText("messaging-type", SchemaConversionUtils.J2EE_NAMESPACE, "javax.jms.MessageListener");
                        //cursor still on transaction-type
                    }
                    if (!cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "activation-config")) {
                        //skip transaction-type
                        cursor.toNextSibling();
                        //convert EJB 2.0 elements to activation-config-properties.
                        moveable.toCursor(cursor);
                        cursor.push();
                        cursor.beginElement("activation-config", SchemaConversionUtils.J2EE_NAMESPACE);
                        boolean hasProperties = addActivationConfigProperty(moveable, cursor, "message-selector", "messageSelector");
                        hasProperties |= addActivationConfigProperty(moveable, cursor, "acknowledge-mode", "acknowledgeMode");
                        if (new QName(SchemaConversionUtils.J2EE_NAMESPACE, "message-driven-destination").equals(moveable.getName()) ||
                                moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "message-driven-destination")) {
                            moveable.push();
                            moveable.toFirstChild();
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "destination-type", "destinationType");
                            hasProperties |= addActivationConfigProperty(moveable, cursor, "subscription-durability", "subscriptionDurability");
                            moveable.pop();
                            moveable.removeXml();
                        }
                        cursor.pop();
                        if (!hasProperties) {
                            //the activation-config element that we created is empty so delete it
                            cursor.toPrevSibling();
                            cursor.removeXml();
                            //cursor should now be at first element in JNDIEnvironmentRefsGroup
                        }
                    } else {
                        //cursor pointing at activation-config
                        cursor.toNextSibling();
                        //cursor should now be at first element in JNDIEnvironmentRefsGroup
                    }
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                }
                cursor.pop();
            } while (cursor.toNextSibling());
        }
    }

    private static boolean addActivationConfigProperty(XmlCursor moveable, XmlCursor cursor, String elementName, String propertyName) {
        QName name = new QName(SchemaConversionUtils.J2EE_NAMESPACE, elementName);
        if (name.equals(moveable.getName()) || moveable.toNextSibling(name)) {
            cursor.push();
            cursor.beginElement("activation-config-property", SchemaConversionUtils.J2EE_NAMESPACE);
            cursor.insertElementWithText("activation-config-property-name", SchemaConversionUtils.J2EE_NAMESPACE, propertyName);
            cursor.insertElementWithText("activation-config-property-value", SchemaConversionUtils.J2EE_NAMESPACE, moveable.getTextValue());
            moveable.removeXml();
            cursor.pop();
            cursor.toNextSibling();
            return true;
        }
        return false;
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

        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            if (beans.contains(sessionBean.getEjbName())) {
                openejbBeans.put(sessionBean.getEjbName(), sessionBean);
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

        Map portInfoMap = ejbModule.getSharedContext();

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
        infoBuilder.addReference("ResourceEnvironmentSetter", ResourceEnvironmentSetter.class, NameFactory.MODULE_BUILDER);
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
                "ResourceEnvironmentSetter",
                "ActivationSpecInfoLocator",
                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
