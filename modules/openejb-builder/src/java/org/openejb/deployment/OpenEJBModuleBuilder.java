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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.EJBModuleImpl;
import org.openejb.corba.compiler.CompilerException;
import org.openejb.corba.compiler.SkeletonGenerator;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.EJBProxyReference;
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
import org.tranql.sql.DataSourceDelegate;
import org.tranql.sql.sql92.SQL92Schema;


/**
 * @version $Revision$ $Date$
 */
public class OpenEJBModuleBuilder implements ModuleBuilder, EJBReferenceBuilder {

    private final URI defaultParentId;
    private final CMPEntityBuilder cmpEntityBuilder;
    private final SessionBuilder sessionBuilder;
    private final EntityBuilder entityBuilder;
    private final MdbBuilder mdbBuilder;
    private final ContainerSecurityBuilder containerSecurityBuilder;
    private final SkeletonGenerator skeletonGenerator;
    private final Repository repository;
    private final Kernel kernel;

    public OpenEJBModuleBuilder(URI defaultParentId, SkeletonGenerator skeletonGenerator, Repository repository, Kernel kernel) {
        this.defaultParentId = defaultParentId;
        this.skeletonGenerator = skeletonGenerator;
        this.containerSecurityBuilder = new ContainerSecurityBuilder(this);
        this.cmpEntityBuilder = new CMPEntityBuilder(this);
        this.sessionBuilder = new SessionBuilder(this);
        this.entityBuilder = new EntityBuilder(this);
        this.mdbBuilder = new MdbBuilder(this);
        this.repository = repository;
        this.kernel = kernel;
    }

    public ContainerSecurityBuilder getSecurityBuilder() {
        return containerSecurityBuilder;
    }

    public SkeletonGenerator getSkeletonGenerator() {
        return skeletonGenerator;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "war", null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId) throws DeploymentException {
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

            // parse it
            EjbJarDocument ejbJarDoc = SchemaConversionUtils.convertToEJBSchema(SchemaConversionUtils.parse(specDD));
            ejbJar = ejbJarDoc.getEjbJar();
        } catch (Exception e) {
            return null;
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

            // if we got one extract the validate it otherwise create a default one
            if (openejbJar != null) {
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoNamingSchema(openejbJar);
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoSecuritySchema(openejbJar);
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.convertToGeronimoServiceSchema(openejbJar);
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
        openejbEjbJar.setParentId("org/apache/geronimo/Server");
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
        J2eeContext moduleJ2eeContext = new J2eeContextImpl(earJ2eeContext.getJ2eeDomainName(), earJ2eeContext.getJ2eeServerName(), earJ2eeContext.getJ2eeApplicationName(), module.getName(), null, null);
        URI moduleUri = module.getModuleURI();

        EJBModule ejbModule = (EJBModule) module;
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();
        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        Set interfaces = new HashSet();
        sessionBuilder.initContext(earContext, moduleJ2eeContext, moduleUri, cl, enterpriseBeans, interfaces);
        entityBuilder.initContext(earContext, moduleJ2eeContext, moduleUri, cl, enterpriseBeans, interfaces);
        mdbBuilder.initContext(cl, enterpriseBeans);

        if (skeletonGenerator != null) {
            File tempJar = null;
            try {
                tempJar = DeploymentUtil.createTempFile();

                /**
                 * Windoze may be holding on to this
                 */               
                tempJar.delete();

                skeletonGenerator.generateSkeletons(interfaces, tempJar, cl);

                earContext.addIncludeAsPackedJar(URI.create("corba.jar"), new JarFile(tempJar));
            } catch (IOException e) {
                throw new DeploymentException("Unable to generate CORBA skels for: " + moduleUri, e);
            } catch (CompilerException e) {
                throw new DeploymentException("Unable to generate CORBA skels for: " + moduleUri, e);
            } finally {
                DeploymentUtil.recursiveDelete(tempJar);
            }
        }
    }

    public Reference createEJBLocalReference(String objectName, boolean session, String localHome, String local) {
        return EJBProxyReference.createLocal(objectName, session, local, localHome);
    }

    public Reference createEJBRemoteReference(String objectName, boolean session, String home, String remote) {
        return EJBProxyReference.createRemote(objectName, session, remote, home);
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

    public String addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = new J2eeContextImpl(earJ2eeContext.getJ2eeDomainName(), earJ2eeContext.getJ2eeServerName(), earJ2eeContext.getJ2eeApplicationName(), module.getName(), null, null);

        EJBModule ejbModule = (EJBModule) module;
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) module.getSpecDD();

        GbeanType[] gbeans = openejbEjbJar.getGbeanArray();
        ServiceConfigBuilder.addGBeans(gbeans, cl, moduleJ2eeContext, earContext);

        ObjectName ejbModuleObjectName = null;
        try {
            ejbModuleObjectName = NameFactory.getModuleName(null, null, null, null, NameFactory.EJB_MODULE, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }

        // EJBModule GBean
        GerResourceLocatorType connectionFactoryLocator = openejbEjbJar.getCmpConnectionFactory();
        CacheFlushStrategyFactory flushStrategyFactory;
        if ( openejbEjbJar.isSetEnforceForeignKeyConstraints() ) {
            flushStrategyFactory = new EnforceRelationshipsFlushStrategyFactory();
        } else {
            flushStrategyFactory = new SimpleFlushStrategyFactory();
        }
        EJBSchema ejbSchema = new EJBSchema(module.getName());
        TransactionManagerDelegate tmDelegate = new TransactionManagerDelegate();
        DataSourceDelegate delegate = new DataSourceDelegate();
        SQL92Schema sqlSchema = new SQL92Schema(module.getName(), delegate);
        GlobalSchema globalSchema = new GlobalSchema(module.getName(), flushStrategyFactory);

        GBeanData ejbModuleGBeanData = new GBeanData(ejbModuleObjectName, EJBModuleImpl.GBEAN_INFO);
        try {
            ejbModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                ejbModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getApplicationObjectName());
            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

            if (connectionFactoryLocator != null) {
                ObjectName connectionFactoryObjectName = getResourceContainerId(ejbModule.getModuleURI(), connectionFactoryLocator, earContext.getRefContext(), earContext.getJ2eeContext());
                //TODO this uses connection factory rather than datasource for the type.
                ejbModuleGBeanData.setReferencePattern("ConnectionFactory", connectionFactoryObjectName);
                ejbModuleGBeanData.setAttribute("Delegate", delegate);
            }

            ejbModuleGBeanData.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            ejbModuleGBeanData.setAttribute("TMDelegate", tmDelegate);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean", e);
        }
        earContext.addGBean(ejbModuleGBeanData);

        // @todo need a better schema name
        cmpEntityBuilder.buildCMPSchema(earContext, moduleJ2eeContext, ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema, globalSchema);

        // create an index of the openejb ejb configurations by ejb-name
        Map openejbBeans = new HashMap();
        //TODO NPE if enterprise-beans or session is missing
        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            openejbBeans.put(sessionBean.getEjbName(), sessionBean);
        }
        //TODO NPE as above
        OpenejbEntityBeanType[] openejbEntityBeans = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openejbEntityBeans.length; i++) {
            OpenejbEntityBeanType entityBean = openejbEntityBeans[i];
            openejbBeans.put(entityBean.getEjbName(), entityBean);
        }
        //TODO NPE as above
        OpenejbMessageDrivenBeanType[] openejbMessageDrivenBeans = openejbEjbJar.getEnterpriseBeans().getMessageDrivenArray();
        for (int i = 0; i < openejbMessageDrivenBeans.length; i++) {
            OpenejbMessageDrivenBeanType messageDrivenBean = openejbMessageDrivenBeans[i];
            openejbBeans.put(messageDrivenBean.getEjbName(), messageDrivenBean);
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
        Security security = null;
        //TODO fix this!
        Map localSecurityRealms = new HashMap();
        security = SecurityBuilder.buildSecurityConfig(Collections.EMPTY_SET, openejbEjbJar.getSecurity(), collectRoleNames(ejbJar), localSecurityRealms, kernel);

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        sessionBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, openejbBeans, transactionPolicyHelper, security, enterpriseBeans);

        entityBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, openejbBeans, transactionPolicyHelper, security, enterpriseBeans);

        cmpEntityBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, ejbSchema, sqlSchema, globalSchema, openejbBeans, transactionPolicyHelper, security, enterpriseBeans, tmDelegate);

        mdbBuilder.buildBeans(earContext, moduleJ2eeContext, cl, ejbModule, openejbBeans, transactionPolicyHelper, security, enterpriseBeans);

        return null;
    }

    private static Set collectRoleNames(EjbJarType ejbJar) {
        Set roleNames = new HashSet();

        if( ejbJar.isSetAssemblyDescriptor() ) {
	        SecurityRoleType[] securityRoles = ejbJar.getAssemblyDescriptor().getSecurityRoleArray();
	        for (int i=0; i<securityRoles.length; i++) {
	            roleNames.add(securityRoles[i].getRoleName().getStringValue());
	        }
        }

        return roleNames;
    }

    private static ObjectName getResourceContainerId(URI uri, GerResourceLocatorType resourceLocator, RefContext refContext, J2eeContext j2eeContext) throws DeploymentException {
        try {
            if (resourceLocator.isSetResourceLink()) {
                String containerId = refContext.getConnectionFactoryContainerId(uri, resourceLocator.getResourceLink(), j2eeContext);
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
            return NameFactory.getResourceComponentName(resourceLocator.getDomain(),
                    resourceLocator.getServer(),
                    resourceLocator.getApplication(),
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
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(OpenEJBModuleBuilder.class);
        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addReference("SkeletonGenerator", SkeletonGenerator.class);
        infoBuilder.addReference("Repository", Repository.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
         infoBuilder.addInterface(ModuleBuilder.class);
        infoBuilder.addInterface(EJBReferenceBuilder.class);

        infoBuilder.setConstructor(new String[] {"defaultParentId", "SkeletonGenerator", "Repository", "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
