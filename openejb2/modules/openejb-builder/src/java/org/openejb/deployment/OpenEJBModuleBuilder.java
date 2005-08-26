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
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.EJBModuleImpl;
import org.openejb.corba.CORBAHandleDelegate;
import org.openejb.corba.proxy.CORBAProxyReference;
import org.openejb.deployment.corba.NoDistributedTxTransactionImportPolicyBuilder;
import org.openejb.deployment.corba.TransactionImportPolicyBuilder;
import org.openejb.deployment.pkgen.TranQLPKGenBuilder;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.EJBProxyReference;
import org.openejb.proxy.ProxyInfo;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.tranql.cache.CacheFlushStrategyFactory;
import org.tranql.cache.EnforceRelationshipsFlushStrategyFactory;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.SimpleFlushStrategyFactory;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.ejbqlcompiler.DerbyDBSyntaxtFactory;
import org.tranql.ejbqlcompiler.DerbyEJBQLCompilerFactory;
import org.tranql.sql.BaseSQLSchema;
import org.tranql.sql.DBSyntaxFactory;
import org.tranql.sql.DataSourceDelegate;
import org.tranql.sql.EJBQLCompilerFactory;
import org.tranql.sql.SQLSchema;


/**
 * Master builder for processing EJB JAR deployments and creating the
 * correspinding runtime objects (GBeans, etc.).
 *
 * @version $Revision$ $Date$
 */
public class OpenEJBModuleBuilder implements ModuleBuilder {

    private final URI defaultParentId;
    private final ObjectName listener;
    private final CMPEntityBuilder cmpEntityBuilder;
    private final SessionBuilder sessionBuilder;
    private final EntityBuilder entityBuilder;
    private final MdbBuilder mdbBuilder;
    private final WebServiceBuilder webServiceBuilder;
    private final TransactionImportPolicyBuilder transactionImportPolicyBuilder;
    private final Repository repository;

    public OpenEJBModuleBuilder(URI defaultParentId, ObjectName listener, Object webServiceLinkTemplate, WebServiceBuilder webServiceBuilder, Repository repository, Kernel kernel) throws GBeanNotFoundException {
        this(defaultParentId, listener, getLinkData(kernel, webServiceLinkTemplate), webServiceBuilder, repository);
    }

    public OpenEJBModuleBuilder(URI defaultParentId, ObjectName listener, GBeanData linkTemplate, WebServiceBuilder webServiceBuilder, Repository repository) {
        this.defaultParentId = defaultParentId;
        this.listener = listener;
        this.transactionImportPolicyBuilder = new NoDistributedTxTransactionImportPolicyBuilder();
        this.cmpEntityBuilder = new CMPEntityBuilder(this);
        this.sessionBuilder = new SessionBuilder(this, linkTemplate, webServiceBuilder);
        this.entityBuilder = new EntityBuilder(this);
        this.mdbBuilder = new MdbBuilder(this);
        this.webServiceBuilder = webServiceBuilder;
        this.repository = repository;
    }

    private static GBeanData getLinkData(Kernel kernel, Object webServiceLinkTemplate) throws GBeanNotFoundException {
        ObjectName webServiceLinkTemplateName = kernel.getProxyManager().getProxyTarget(webServiceLinkTemplate);
        GBeanData linkTemplate = kernel.getGBeanData(webServiceLinkTemplateName);
        return linkTemplate;
    }

    public TransactionImportPolicyBuilder getTransactionImportPolicyBuilder() {
        return transactionImportPolicyBuilder;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "ejb", null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone) throws DeploymentException {
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
            EjbJarDocument ejbJarDoc = SchemaConversionUtils.convertToEJBSchema(SchemaConversionUtils.parse(specDD));
            ejbJar = ejbJarDoc.getEjbJar();
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing ejb-jar.xml", e);
        }

        OpenejbOpenejbJarType openejbJar = getOpenejbJar(plan, moduleFile, standAlone, targetPath, ejbJar);

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(openejbJar.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + openejbJar.getConfigId(), e);
        }

        URI parentId = null;
        if (openejbJar.isSetParentId()) {
            try {
                parentId = new URI(openejbJar.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + openejbJar.getParentId(), e);
            }
        } else {
            parentId = defaultParentId;
        }

        return new EJBModule(standAlone, configId, parentId, moduleFile, targetPath, ejbJar, openejbJar, specDD);
    }

    OpenejbOpenejbJarType getOpenejbJar(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, EjbJarType ejbJar) throws DeploymentException {
        OpenejbOpenejbJarType openejbJar = null;
        try {
            // load the openejb-jar.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                            "openejb-jar",
                            OpenejbOpenejbJarType.type);
                } else {
                    OpenejbOpenejbJarDocument openejbJarDoc = null;
                    if (plan != null) {
                        openejbJarDoc = OpenejbOpenejbJarDocument.Factory.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/openejb-jar.xml");
                        openejbJarDoc = OpenejbOpenejbJarDocument.Factory.parse(path);
                    }
                    if (openejbJarDoc != null) {
                        openejbJar = openejbJarDoc.getOpenejbJar();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract, adjust, and validate it otherwise create a default one
            if (openejbJar != null) {
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoNamingSchema(openejbJar);
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoSecuritySchema(openejbJar);
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoServiceSchema(openejbJar);
                openejbJar = (OpenejbOpenejbJarType) OpenEJBSchemaUtils.convertToPKGenSchema(openejbJar);
                SchemaConversionUtils.validateDD(openejbJar);
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
        openejbEjbJar.setParentId(defaultParentId.toString());
        if (null != ejbJar.getId()) {
            openejbEjbJar.setConfigId(ejbJar.getId());
        } else {
            openejbEjbJar.setConfigId(id);
        }
        openejbEjbJar.addNewEnterpriseBeans();
        return openejbEjbJar;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        try {
            // extract the ejbJar file into a standalone packed jar file and add the contents to the output
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy ejb module jar into configuration: " + moduleFile.getName());
        }

        // add the dependencies declared in the openejb-jar.xml file
        OpenejbOpenejbJarType openEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        DependencyType[] dependencies = openEjbJar.getDependencyArray();
        ServiceConfigBuilder.addDependencies(earContext, dependencies, repository);
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.EJB_MODULE, module.getName());
        URI moduleUri = module.getModuleURI();

        EJBModule ejbModule = (EJBModule) module;
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();

        if (ejbJar.isSetAssemblyDescriptor()) {
            AssemblyDescriptorType assemblyDescriptor = ejbJar.getAssemblyDescriptor();

            MessageDestinationType[] messageDestinations = assemblyDescriptor.getMessageDestinationArray();
            OpenejbOpenejbJarType openejbJar = (OpenejbOpenejbJarType) module.getVendorDD();
            GerMessageDestinationType[] gerMessageDestinations = openejbJar.getMessageDestinationArray();

            ENCConfigBuilder.registerMessageDestinations(earContext.getRefContext(), module.getName(), messageDestinations, gerMessageDestinations);
        }

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        sessionBuilder.initContext(earContext, moduleJ2eeContext, moduleUri, cl, enterpriseBeans);
        entityBuilder.initContext(earContext, moduleJ2eeContext, moduleUri, cl, enterpriseBeans);
        mdbBuilder.initContext(cl, enterpriseBeans);

    }

    public CMPEntityBuilder getCmpEntityBuilder() {
        return cmpEntityBuilder;
    }

    public EntityBuilder getBmpEntityBuilder() {
        return entityBuilder;
    }

    public MdbBuilder getMdbBuilder() {
        return mdbBuilder;
    }

    public SessionBuilder getSessionBuilder() {
        return sessionBuilder;
    }

    /**
     * Does the meaty work of processing the deployment information and
     * creating GBeans for all the EJBs in the JAR, etc.
     */
    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.EJB_MODULE, module.getName());

        EJBModule ejbModule = (EJBModule) module;
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) module.getSpecDD();

        GbeanType[] gbeans = openejbEjbJar.getGbeanArray();
        ServiceConfigBuilder.addGBeans(gbeans, cl, moduleJ2eeContext, earContext);

        ObjectName ejbModuleObjectName = null;
        try {
            ejbModuleObjectName = NameFactory.getModuleName(null, null, null, null, null, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct module name", e);
        }

        // EJBModule GBean
        GerResourceLocatorType connectionFactoryLocator = openejbEjbJar.getCmpConnectionFactory();
        CacheFlushStrategyFactory flushStrategyFactory;
        if (openejbEjbJar.isSetEnforceForeignKeyConstraints()) {
            flushStrategyFactory = new EnforceRelationshipsFlushStrategyFactory();
        } else {
            flushStrategyFactory = new SimpleFlushStrategyFactory();
        }
        EJBSchema ejbSchema = new EJBSchema(module.getName());
        TransactionManagerDelegate tmDelegate = new TransactionManagerDelegate();
        DataSourceDelegate delegate = new DataSourceDelegate();

        EJBQLCompilerFactory compilerFactory = new DerbyEJBQLCompilerFactory();
        try {
            if (openejbEjbJar.isSetEjbQlCompilerFactory()) {
                String className = openejbEjbJar.getEjbQlCompilerFactory().toString();
                Class clazz = cl.loadClass(className);
                Constructor constructor = clazz.getConstructor(null);
                Object factory = constructor.newInstance(null);
                if (false == factory instanceof EJBQLCompilerFactory) {
                    throw new DeploymentException("EJBQLCompilerFactory expected. was=" + factory);
                }
                compilerFactory = (EJBQLCompilerFactory) factory;
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize ejb-ql-compiler-factory=" + openejbEjbJar.getEjbQlCompilerFactory(), e);
        }

        DBSyntaxFactory syntaxFactory = new DerbyDBSyntaxtFactory();
        try {
            if (openejbEjbJar.isSetDbSyntaxFactory()) {
                String className = openejbEjbJar.getDbSyntaxFactory().toString();
                Class clazz = cl.loadClass(className);
                Constructor constructor = clazz.getConstructor(null);
                Object factory = constructor.newInstance(null);
                if (false == factory instanceof DBSyntaxFactory) {
                    throw new DeploymentException("DBSyntaxFactory expected. was=" + factory);
                }
                syntaxFactory = (DBSyntaxFactory) factory;
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize ejb-ql-compiler-factory=" + openejbEjbJar.getEjbQlCompilerFactory(), e);
        }

        SQLSchema sqlSchema = new BaseSQLSchema(module.getName(), delegate, syntaxFactory, compilerFactory);

        GlobalSchema globalSchema = new GlobalSchema(module.getName(), flushStrategyFactory);

        GBeanData ejbModuleGBeanData = new GBeanData(ejbModuleObjectName, EJBModuleImpl.GBEAN_INFO);
        try {
            ejbModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                ejbModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getApplicationObjectName());
            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

            if (connectionFactoryLocator != null) {
                ObjectName connectionFactoryObjectName = getResourceContainerId(ejbModule.getModuleURI(), connectionFactoryLocator, earContext);
                //TODO this uses connection factory rather than datasource for the type.
                ejbModuleGBeanData.setReferencePattern("ConnectionFactory", connectionFactoryObjectName);
                ejbModuleGBeanData.setAttribute("Delegate", delegate);
            }

            ejbModuleGBeanData.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            ejbModuleGBeanData.setAttribute("TMDelegate", tmDelegate);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean " + ejbModuleGBeanData.getName(), e);
        }
        earContext.addGBean(ejbModuleGBeanData);

        // Handle automatic PK generation -- we want to use the same builder for all CMP entities
        TranQLPKGenBuilder pkgen = new TranQLPKGenBuilder();

        // @todo need a better schema name
        cmpEntityBuilder.buildCMPSchema(earContext, moduleJ2eeContext, ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema, globalSchema, pkgen, delegate);

        if (null == connectionFactoryLocator && false == ejbSchema.getEntities().isEmpty()) {
            throw new DeploymentException("A cmp-connection-factory element must be specified as CMP EntityBeans are defined.");
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

        Map portInfoMap = Collections.EMPTY_MAP;
        JarFile jarFile = ejbModule.getModuleFile();
        URL wsDDUrl = null;
        try {
            wsDDUrl = DeploymentUtil.createJarURL(jarFile, "META-INF/webservices.xml");
            portInfoMap = webServiceBuilder.parseWebServiceDescriptor(wsDDUrl, jarFile, true, correctedPortLocations);
        } catch (MalformedURLException e) {
            //there is no webservices file
        }


        TransactionPolicyHelper transactionPolicyHelper;
        if (ejbJar.isSetAssemblyDescriptor()) {
            transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());
        } else {
            transactionPolicyHelper = new TransactionPolicyHelper();
        }

        /**
         * Build the security configuration.  Attempt to auto generate role mappings.
         */
        if (openejbEjbJar.isSetSecurity()) {
            SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(openejbEjbJar.getSecurity());
            earContext.setSecurityConfiguration(securityConfiguration);
        }


        ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), new Permissions(), new HashMap());
        //TODO go back to the commented version when possible
//          String contextID = ejbModuleObjectName.getCanonicalName();
        String policyContextID = ejbModuleObjectName.getCanonicalName().replaceAll("[,: ]", "_");

        sessionBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, componentPermissions, openejbBeans, transactionPolicyHelper, enterpriseBeans, listener, policyContextID, portInfoMap);

        entityBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, openejbBeans, componentPermissions, transactionPolicyHelper, enterpriseBeans, policyContextID);

        cmpEntityBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, ejbSchema, sqlSchema, globalSchema, openejbBeans, transactionPolicyHelper, enterpriseBeans, tmDelegate, componentPermissions, policyContextID);

        mdbBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, openejbBeans, transactionPolicyHelper, enterpriseBeans, componentPermissions, policyContextID);

        earContext.addSecurityContext(policyContextID, componentPermissions);
    }

    private static ObjectName getResourceContainerId(URI uri, GerResourceLocatorType resourceLocator, EARContext earContext) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();
        try {
            if (resourceLocator.isSetResourceLink()) {
                String containerId = refContext.getConnectionFactoryContainerId(uri, resourceLocator.getResourceLink(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, earContext);
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
                    //todo determine type from iface class
//                        resourceLocator.getType(),
                    NameFactory.JCA_MANAGED_CONNECTION_FACTORY,
                    j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct cmp datasource object name", e);
        }
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
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(OpenEJBModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addAttribute("listener", ObjectName.class, true);
        infoBuilder.addReference("WebServiceLinkTemplate", Object.class, NameFactory.WEB_SERVICE_LINK);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addInterface(ModuleBuilder.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{"defaultParentId", "listener", "WebServiceLinkTemplate", "WebServiceBuilder", "Repository", "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
