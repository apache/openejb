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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.connector.ActivationSpecInfo;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.ExcludeListType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.MethodPermissionType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityIdentityType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.ContainerBuilder;
import org.openejb.EJBModuleImpl;
import org.openejb.ResourceEnvironmentBuilder;
import org.openejb.SecureBuilder;
import org.openejb.dispatch.MethodSignature;
import org.openejb.entity.bmp.BMPContainerBuilder;
import org.openejb.entity.cmp.CMPContainerBuilder;
import org.openejb.mdb.MDBContainerBuilder;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyObjectFactory;
import org.openejb.proxy.ProxyRefAddr;
import org.openejb.security.SecurityConfiguration;
import org.openejb.sfsb.StatefulContainerBuilder;
import org.openejb.slsb.StatelessContainerBuilder;
import org.openejb.xbeans.ejbjar.OpenejbDefaultPrincipalType;
import org.openejb.xbeans.ejbjar.OpenejbDependencyType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbGbeanType;
import org.openejb.xbeans.ejbjar.OpenejbLocalRefType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbPrincipalType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
import org.openejb.xbeans.ejbjar.OpenejbRealmType;
import org.openejb.xbeans.ejbjar.OpenejbRemoteRefType;
import org.openejb.xbeans.ejbjar.OpenejbRoleMappingsType;
import org.openejb.xbeans.ejbjar.OpenejbRoleType;
import org.openejb.xbeans.ejbjar.OpenejbSecurityType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.schema.Schema;
import org.tranql.sql.Column;
import org.tranql.sql.DataSourceDelegate;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;


/**
 * @version $Revision$ $Date$
 */
public class OpenEJBModuleBuilder implements ModuleBuilder, EJBReferenceBuilder {

    private static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(OpenejbOpenejbJarDocument.class.getClassLoader())
    });

    private final Kernel kernel;

    public OpenEJBModuleBuilder(Kernel kernel) {
        this.kernel = kernel;
    }

    public XmlObject getDeploymentPlan(URL module) throws XmlException {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            OpenejbOpenejbJarDocument plan = (OpenejbOpenejbJarDocument) XmlBeansUtil.getXmlObject(new URL(moduleBase, "META-INF/openejb-jar.xml"), OpenejbOpenejbJarDocument.type);
            if (plan == null) {
                return createDefaultPlan(moduleBase);
            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private OpenejbOpenejbJarDocument createDefaultPlan(URL moduleBase) throws XmlException {
        URL ejbJarXml = null;
        try {
            ejbJarXml = new URL(moduleBase, "META-INF/ejb-jar.xml");
        } catch (MalformedURLException e) {
            return null;
        }
        EjbJarDocument ejbJarDoc = (EjbJarDocument) XmlBeansUtil.getXmlObject(ejbJarXml, EjbJarDocument.type);
        if (ejbJarDoc == null) {
            return null;
        }

        EjbJarType ejbJar = ejbJarDoc.getEjbJar();
        String id = ejbJar.getId();
        if (id == null) {
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if ( id.endsWith("/") ) {
                id = id.substring(0, id.length() - 1);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }

        return newOpenejbJarDocument(ejbJar, id);
    }

    private OpenejbOpenejbJarDocument newOpenejbJarDocument(EjbJarType ejbJar, String id) {
        OpenejbOpenejbJarDocument openejbJarDocument = OpenejbOpenejbJarDocument.Factory.newInstance();
        OpenejbOpenejbJarType openejbEjbJar = openejbJarDocument.addNewOpenejbJar();
        openejbEjbJar.setParentId("org/apache/geronimo/Server");
        if ( null != ejbJar.getId() ) {
            openejbEjbJar.setConfigId(ejbJar.getId());
        } else {
            openejbEjbJar.setConfigId(id);
        }
        openejbEjbJar.addNewEnterpriseBeans();
        return openejbJarDocument;
    }

    public boolean canHandlePlan(XmlObject plan) {
        return plan instanceof OpenejbOpenejbJarDocument;
    }

    public Module createModule(String name, XmlObject plan) throws DeploymentException {
        if (!canHandlePlan(plan)) {
            throw new DeploymentException("wrong kind of plan");
        }
        EJBModule module = new EJBModule(name, URI.create("/"));
        OpenejbOpenejbJarType vendorDD = ((OpenejbOpenejbJarDocument) plan).getOpenejbJar();
        module.setVendorDD(vendorDD);
        return module;
    }

    public URI getParentId(XmlObject plan) throws DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = ((OpenejbOpenejbJarDocument) plan).getOpenejbJar();
        URI parentID;
        if (openejbEjbJar.isSetParentId()) {
            try {
                parentID = new URI(openejbEjbJar.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + openejbEjbJar.getParentId(), e);
            }
        } else {
            parentID = null;
        }
        return parentID;
    }

    public URI getConfigId(XmlObject plan) throws DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = ((OpenejbOpenejbJarDocument) plan).getOpenejbJar();
        URI configID;
        try {
            configID = new URI(openejbEjbJar.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + openejbEjbJar.getConfigId(), e);
        }
        return configID;
    }

    public void installModule(File earFolder, EARContext earContext, Module ejbModule) throws DeploymentException {
        File ejbFolder = new File(earFolder, ejbModule.getURI().toString());
        
        // Unpacked EAR modules can define via application.xml either
        // (standard) packed or unpacked modules
        InstallCallback callback;
        if ( ejbFolder.isDirectory() ) {
            callback = new UnPackedInstallCallback(ejbModule, ejbFolder);
        } else {
            JarFile jarFile;
            try {
                jarFile = new JarFile(ejbFolder);
            } catch (IOException e) {
                throw new DeploymentException("Can not create EJB JAR file " + ejbFolder, e);
            }
            callback = new PackedInstallCallback(ejbModule, jarFile);
        }
        installModule(callback, earContext, ejbModule);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule) throws DeploymentException {
        JarFile ejbJarFile;
        try {
            if (!ejbModule.getURI().equals(URI.create("/"))) {
                ZipEntry jarEntry = earFile.getEntry(ejbModule.getURI().toString());
                // Unpack the nested JAR.
                File tempFile = FileUtil.toTempFile(earFile.getInputStream(jarEntry));
                ejbJarFile = new JarFile(tempFile);
            } else {
                ejbJarFile = earFile;
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying jar", e);
        }
        InstallCallback callback = new PackedInstallCallback(ejbModule, ejbJarFile);
        installModule(callback, earContext, ejbModule);
    }

    private void installModule(InstallCallback callback, EARContext earContext, Module ejbModule) throws DeploymentException {
        URI ejbJarModuleLocation;
        if (!ejbModule.getURI().equals(URI.create("/"))) {
            ejbJarModuleLocation = ejbModule.getURI();
        } else {
            ejbJarModuleLocation = URI.create("ejb.jar");
        }
        try {
            // load the ejb-jar.xml file
            EjbJarType ejbJar;
            try {
                InputStream ddInputStream = callback.getEjbJarDD();
                XmlObject dd = SchemaConversionUtils.parse(ddInputStream);
                EjbJarDocument doc = SchemaConversionUtils.convertToEJBSchema(dd);
                ejbJar = doc.getEjbJar();
                ejbModule.setSpecDD(ejbJar);
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse ejb-jar.xml", e);
            }

            // load the openejb-jar.xml file
            OpenejbOpenejbJarType openEjbJar = (OpenejbOpenejbJarType) ejbModule.getVendorDD();
            if (openEjbJar == null) {
                try {
                    InputStream openejbDDInputStream = callback.getOpenejbJarDD();
                    OpenejbOpenejbJarDocument doc;
                    if (openejbDDInputStream != null) {
                        doc = (OpenejbOpenejbJarDocument) XmlBeansUtil.parse(openejbDDInputStream, OpenejbOpenejbJarDocument.type);
                    } else {
                        doc = newOpenejbJarDocument(ejbJar, ejbJarModuleLocation.toString());
                    }
                    openEjbJar = doc.getOpenejbJar();
                    ejbModule.setVendorDD(openEjbJar);
                } catch (XmlException e) {
                    throw new DeploymentException("Unable to parse openejb-jar.xml");
                }
            }

            callback.installInEARContext(earContext, ejbJarModuleLocation);
            
            // add the dependencies declared in the openejb-jar.xml file
            OpenejbDependencyType[] dependencies = openEjbJar.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Unable to deploy ejb module [" + ejbModule.getName() + "]", e);
        }
    }
    
    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        org.apache.geronimo.j2ee.deployment.EJBModule ejbModule = (org.apache.geronimo.j2ee.deployment.EJBModule) module;
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();
        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];
            String ejbName = sessionBean.getEjbName().getStringValue();

            ObjectName sessionObjectName = createEJBObjectName(earContext, module.getName(), sessionBean);

            // ejb-ref
            if (sessionBean.isSetRemote()) {
                String remote = getJ2eeStringValue(sessionBean.getRemote());
                ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = getJ2eeStringValue(sessionBean.getHome());
                ENCConfigBuilder.assureEJBHomeInterface(home, cl);

                String objectName = sessionObjectName.getCanonicalName();

                boolean isSession = true;
                Reference reference = createEJBRemoteReference(objectName, isSession, remote, home);
                earContext.addEJBRef(module.getURI(), ejbName, reference);
            }

            // ejb-local-ref
            if (sessionBean.isSetLocal()) {
                String local = getJ2eeStringValue(sessionBean.getLocal());
                ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = getJ2eeStringValue(sessionBean.getLocalHome());
                ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);

                String objectName = sessionObjectName.getCanonicalName();
                boolean isSession = true;
                Reference reference = createEJBLocalReference(objectName, isSession, local, localHome);
                earContext.addEJBLocalRef(module.getURI(), ejbName, reference);
            }
        }


        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            String ejbName = entityBean.getEjbName().getStringValue();

            ObjectName entityObjectName = createEJBObjectName(earContext, module.getName(), entityBean);

            // ejb-ref
            if (entityBean.isSetRemote()) {
                String remote = getJ2eeStringValue(entityBean.getRemote());
                ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = getJ2eeStringValue(entityBean.getHome());
                ENCConfigBuilder.assureEJBHomeInterface(home, cl);

                String objectName = entityObjectName.getCanonicalName();
                boolean isSession = false;
                Reference reference = createEJBRemoteReference(objectName, isSession, remote, home);
                earContext.addEJBRef(module.getURI(), ejbName, reference);
            }

            // ejb-local-ref
            if (entityBean.isSetLocal()) {
                String local = getJ2eeStringValue(entityBean.getLocal());
                ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = getJ2eeStringValue(entityBean.getLocalHome());
                ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);

                String objectName = entityObjectName.getCanonicalName();
                boolean isSession = false;
                Reference reference = createEJBLocalReference(objectName, isSession, local, localHome);
                earContext.addEJBLocalRef(module.getURI(), ejbName, reference);
            }
        }
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = getJ2eeStringValue(messageDrivenBean.getMessagingType());
            ENCConfigBuilder.assureEJBObjectInterface(messagingType, cl);
        }
    }

    public Reference createEJBLocalReference(String objectName, boolean session, String local, String localHome) {
        ProxyRefAddr address = ProxyRefAddr.createLocal(objectName, session, local, localHome);
        Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
        return reference;
    }

    public Reference createEJBRemoteReference(String objectName, boolean session, String remote, String home) {
        ProxyRefAddr address = ProxyRefAddr.createRemote(objectName, session, remote, home);
        Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
        return reference;
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        EJBModule ejbModule = (EJBModule) module;
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) module.getSpecDD();
        OpenejbGbeanType[] gbeans = openejbEjbJar.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new OpenEJBGBeanAdapter(gbeans[i]), cl, earContext);
        }

        Properties nameProps = new Properties();
        nameProps.put("j2eeType", "EJBModule");
        nameProps.put("name", module.getName());
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());

        ObjectName ejbModuleObjectName;
        try {
            ejbModuleObjectName = new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        // EJBModule GBean
        String connectionFactoryName = openejbEjbJar.getCmpConnectionFactory();
        EJBSchema ejbSchema = new EJBSchema(module.getName());
        DataSourceDelegate delegate = new DataSourceDelegate();
        SQL92Schema sqlSchema = new SQL92Schema(module.getName(), delegate);
        GBeanMBean ejbModuleGBean = new GBeanMBean(EJBModuleImpl.GBEAN_INFO, cl);
        try {
            ejbModuleGBean.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerObjectName()));
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                ejbModuleGBean.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getApplicationObjectName()));
            }
            ejbModuleGBean.setAttribute("deploymentDescriptor", null);
            if (connectionFactoryName != null) {
                ObjectName connectionFactoryObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + connectionFactoryName);
                ejbModuleGBean.setReferencePattern("ConnectionFactory", connectionFactoryObjectName);
                ejbModuleGBean.setAttribute("Delegate", delegate);
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean", e);
        }
        earContext.addGBean(ejbModuleObjectName, ejbModuleGBean);

        // @todo need a better schema name
        buildCMPSchema(earContext, module.getName(), ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema);

        // create an index of the openejb ejb configurations by ejb-name
        Map openejbBeans = new HashMap();
        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            openejbBeans.put(sessionBean.getEjbName(), sessionBean);
        }
        OpenejbEntityBeanType[] openejbEntityBeans = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openejbEntityBeans.length; i++) {
            OpenejbEntityBeanType entityBean = openejbEntityBeans[i];
            openejbBeans.put(entityBean.getEjbName(), entityBean);
        }
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

        Security security = buildSecurityConfig(openejbEjbJar);

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            ObjectName sessionObjectName = createEJBObjectName(earContext, module.getName(), sessionBean);

            GBeanMBean sessionGBean = createSessionBean(earContext, ejbModule, sessionObjectName.getCanonicalName(), sessionBean, openejbSessionBean, transactionPolicyHelper, security, cl);
            earContext.addGBean(sessionObjectName, sessionGBean);
        }


        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityBean.getEjbName().getStringValue());
            ObjectName entityObjectName = createEJBObjectName(earContext, module.getName(), entityBean);

            GBeanMBean entityGBean = null;
            if ("Container".equals(entityBean.getPersistenceType().getStringValue())) {
                entityGBean = createCMPBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, ejbSchema, sqlSchema, connectionFactoryName, transactionPolicyHelper, security, cl);
            } else {
                entityGBean = createBMPBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, transactionPolicyHelper, security, cl);
            }
            earContext.addGBean(entityObjectName, entityGBean);
        }

        // Message Driven Beans
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];

            OpenejbMessageDrivenBeanType openejbMessageDrivenBean = (OpenejbMessageDrivenBeanType) openejbBeans.get(messageDrivenBean.getEjbName().getStringValue());
            if (openejbMessageDrivenBean == null) {
                throw new DeploymentException("No openejb deployment descriptor for mdb: " + messageDrivenBean.getEjbName().getStringValue());
            }
            ObjectName messageDrivenObjectName = createEJBObjectName(earContext, module.getName(), messageDrivenBean);
            ObjectName activationSpecName = createActivationSpecObjectName(earContext, module.getName(), messageDrivenBean);

            String containerId = messageDrivenObjectName.getCanonicalName();
            GBeanMBean activationSpecGBean = createActivationSpecWrapperGBean(earContext,
                                                                              messageDrivenBean.isSetActivationConfig() ? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : new ActivationConfigPropertyType[]{},
                                                                              openejbMessageDrivenBean.getResourceAdapterName(),
                                                                              openejbMessageDrivenBean.getActivationSpecClass(),
                                                                              containerId,
                                                                              cl);
            GBeanMBean messageDrivenGBean = createMessageDrivenBean(earContext, ejbModule, containerId, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, security, cl);
            earContext.addGBean(activationSpecName, activationSpecGBean);
            earContext.addGBean(messageDrivenObjectName, messageDrivenGBean);
        }

    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }

    private void buildCMPSchema(EARContext earContext, String ejbModuleName, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQL92Schema sqlSchema) throws DeploymentException {
        EntityBeanType[] entityBeans = ejbJar.getEnterpriseBeans().getEntityArray();

        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            if ("Container".equals(entityBean.getPersistenceType().getStringValue())) {
                String ejbName = entityBean.getEjbName().getStringValue();
                String abstractSchemaName = entityBean.getAbstractSchemaName().getStringValue();

                ObjectName entityObjectName = createEJBObjectName(earContext, ejbModuleName, entityBean);

                EJBProxyFactory proxyFactory = (EJBProxyFactory) createEJBProxyFactory(entityObjectName.getCanonicalName(),
                                                                                       false,
                                                                                       getJ2eeStringValue(entityBean.getRemote()),
                                                                                       getJ2eeStringValue(entityBean.getHome()),
                                                                                       getJ2eeStringValue(entityBean.getLocal()),
                                                                                       getJ2eeStringValue(entityBean.getLocalHome()),
                                                                                       cl);

                Class ejbClass = null;
                try {
                    ejbClass = cl.loadClass(entityBean.getEjbClass().getStringValue());
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load cmp bean class: ejbName=" + ejbName + " ejbClass=" + entityBean.getEjbClass().getStringValue());
                }

                EJB ejb = new EJB(ejbName, abstractSchemaName, proxyFactory);
                Table table = new Table(ejbName, abstractSchemaName);

                String primkeyField = entityBean.getPrimkeyField().getStringValue();
                CmpFieldType[] cmpFieldTypes = entityBean.getCmpFieldArray();
                for (int cmpFieldIndex = 0; cmpFieldIndex < cmpFieldTypes.length; cmpFieldIndex++) {
                    CmpFieldType cmpFieldType = cmpFieldTypes[cmpFieldIndex];
                    String fieldName = cmpFieldType.getFieldName().getStringValue();
                    Class fieldType = getCMPFieldType(fieldName, ejbClass);
                    boolean isPKField = fieldName.equals(primkeyField);
                    CMPField cmpField = new CMPField(fieldName, fieldType, isPKField);
                    ejb.addCMPField(cmpField);
                    if (isPKField) {
                        ejb.setPrimaryKeyField(cmpField);
                    }

                    Column column = new Column(fieldName, fieldType, isPKField);
                    table.addColumn(column);
                }

                ejbSchema.addEJB(ejb);
                sqlSchema.addTable(table);
            }
        }
    }

    private static Class getCMPFieldType(String fieldName, Class beanClass) throws DeploymentException {
        try {
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method getter = beanClass.getMethod(getterName, null);
            return getter.getReturnType();
        } catch (Exception e) {
            throw new DeploymentException("Getter for CMP field not found: fieldName=" + fieldName + " beanClass=" + beanClass.getName());
        }
    }

    public GBeanMBean createSessionBean(EARContext earContext, EJBModule ejbModule, String containerId, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        ContainerBuilder builder = null;
        Permissions toBeChecked = new Permissions();
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        if (isStateless) {
            builder = new StatelessContainerBuilder();
            builder.setTransactedTimerName(earContext.getTransactedTimerName());
            builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
            builder.setServiceEndpointName(getJ2eeStringValue(sessionBean.getServiceEndpoint()));
            addToPermissions(toBeChecked, ejbName, "ServiceEndpoint", builder.getServiceEndpointName(), cl);
        } else {
            builder = new StatefulContainerBuilder();
        }
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(sessionBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(getJ2eeStringValue(sessionBean.getHome()));
        builder.setRemoteInterfaceName(getJ2eeStringValue(sessionBean.getRemote()));
        builder.setLocalHomeInterfaceName(getJ2eeStringValue(sessionBean.getLocalHome()));
        builder.setLocalInterfaceName(getJ2eeStringValue(sessionBean.getLocal()));

        addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);

        fillContainerBuilderSecurity(builder,
                                     toBeChecked,
                                     security,
                                     ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                                     sessionBean.getEjbName().getStringValue(),
                                     sessionBean.getSecurityIdentity(),
                                     sessionBean.getSecurityRoleRefArray());

        UserTransactionImpl userTransaction;
        if ("Bean".equals(sessionBean.getTransactionType().getStringValue())) {
            userTransaction = new UserTransactionImpl();
            builder.setUserTransaction(userTransaction);
            if (isStateless) {
                builder.setTransactionPolicySource(TransactionPolicyHelper.StatelessBMTPolicySource);
            } else {
                builder.setTransactionPolicySource(TransactionPolicyHelper.StatefulBMTPolicySource);
            }
        } else {
            userTransaction = null;
            TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
            builder.setTransactionPolicySource(transactionPolicySource);
        }

        try {
            ReadOnlyContext compContext = buildComponentContext(earContext, ejbModule, sessionBean, openejbSessionBean, userTransaction, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName" + ejbName, e);
        }

        if (openejbSessionBean != null) {
            setResourceEnvironment(builder, sessionBean.getResourceRefArray(), openejbSessionBean.getResourceRefArray());
            builder.setJndiNames(openejbSessionBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbSessionBean.getLocalJndiNameArray());
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
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    public GBeanMBean createBMPBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();

        BMPContainerBuilder builder = new BMPContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(entityBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(getJ2eeStringValue(entityBean.getHome()));
        builder.setRemoteInterfaceName(getJ2eeStringValue(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(getJ2eeStringValue(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(getJ2eeStringValue(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(getJ2eeStringValue(entityBean.getPrimKeyClass()));
        TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
        builder.setTransactionPolicySource(transactionPolicySource);
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        Permissions toBeChecked = new Permissions();
        addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
        fillContainerBuilderSecurity(builder,
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

    public GBeanMBean createCMPBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, Schema sqlSchema, String connectionFactoryName, TransactionPolicyHelper transactionPolicyHelper, Security security, ClassLoader cl) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();

        CMPContainerBuilder builder = new CMPContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(entityBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(getJ2eeStringValue(entityBean.getHome()));
        builder.setRemoteInterfaceName(getJ2eeStringValue(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(getJ2eeStringValue(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(getJ2eeStringValue(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(getJ2eeStringValue(entityBean.getPrimKeyClass()));
        TransactionPolicySource transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);
        builder.setTransactionPolicySource(transactionPolicySource);
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        Permissions toBeChecked = new Permissions();
        addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
        addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
        fillContainerBuilderSecurity(builder,
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

        builder.setEJBSchema(ejbSchema);
        builder.setSQLSchema(sqlSchema);
        builder.setConnectionFactoryName(connectionFactoryName);

        Map queries = new HashMap();
        if (openejbEntityBean != null) {
            OpenejbQueryType[] queryTypes = openejbEntityBean.getQueryArray();
            for (int i = 0; i < queryTypes.length; i++) {
                OpenejbQueryType queryType = queryTypes[i];
                MethodSignature signature = new MethodSignature(queryType.getQueryMethod().getMethodName(),
                                                                queryType.getQueryMethod().getMethodParams().getMethodParamArray());
                String sql = queryType.getSql();
                queries.put(signature, sql);
            }
        }
        builder.setQueries(queries);

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
        }
    }

    public GBeanMBean createMessageDrivenBean(EARContext earContext,
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
        builder.setEndpointInterfaceName(getJ2eeStringValue(messageDrivenBean.getMessagingType()));
        builder.setTransactedTimerName(earContext.getTransactedTimerName());
        builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());

        Permissions toBeChecked = new Permissions();
        fillContainerBuilderSecurity(builder,
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
        return activationSpecGBean;
    }

    private Object createEJBProxyFactory(String containerId, boolean isSessionBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws DeploymentException {
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

    private ObjectName createEJBObjectName(EARContext earContext, String ejbModuleName, SessionBeanType sessionBean) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();
        String type = sessionBean.getSessionType().getStringValue() + "SessionBean";
        return createEJBObjectName(earContext, ejbModuleName, type, ejbName);
    }

    private ObjectName createEJBObjectName(EARContext earContext, String moduleName, EntityBeanType entityBean) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "EntityBean", ejbName);
    }

    private ObjectName createActivationSpecObjectName(EARContext earContext, String moduleName, MessageDrivenBeanType messageDrivenBean) throws DeploymentException {
        String ejbName = messageDrivenBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "ActivationSpec", ejbName);
    }

    private ObjectName createEJBObjectName(EARContext earContext, String moduleName, MessageDrivenBeanType messageDrivenBean) throws DeploymentException {
        String ejbName = messageDrivenBean.getEjbName().getStringValue();
        return createEJBObjectName(earContext, moduleName, "MessageDrivenBean", ejbName);
    }

    private ObjectName createEJBObjectName(EARContext earContext, String moduleName, String type, String ejbName) throws DeploymentException {
        Properties nameProps = new Properties();
        nameProps.put("j2eeType", type);
        nameProps.put("name", ejbName);
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
        //TODO should this be EJBModule rather than J2EEModule???
        nameProps.put("J2EEModule", moduleName);

        try {
            return new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }
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

    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = sessionBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = sessionBean.getEjbRefArray();
        OpenejbRemoteRefType[] openejbEjbRefs = null;
        if (openejbSessionBean != null) {
            openejbEjbRefs = openejbSessionBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = sessionBean.getEjbLocalRefArray();
        OpenejbLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbSessionBean != null) {
            openejbEjbLocalRefs = openejbSessionBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = sessionBean.getResourceRefArray();
        OpenejbLocalRefType[] openejbResourceRefs = null;
        if (openejbSessionBean != null) {
            openejbResourceRefs = openejbSessionBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = sessionBean.getResourceEnvRefArray();
        OpenejbLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbSessionBean != null) {
            openejbResourceEnvRefs = openejbSessionBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = sessionBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = entityBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = entityBean.getEjbRefArray();
        OpenejbRemoteRefType[] openejbEjbRefs = null;
        if (openejbEntityBean != null) {
            openejbEjbRefs = openejbEntityBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = entityBean.getEjbLocalRefArray();
        OpenejbLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbEntityBean != null) {
            openejbEjbLocalRefs = openejbEntityBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = entityBean.getResourceRefArray();
        OpenejbLocalRefType[] openejbResourceRefs = null;
        if (openejbEntityBean != null) {
            openejbResourceRefs = openejbEntityBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = entityBean.getResourceEnvRefArray();
        OpenejbLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbEntityBean != null) {
            openejbResourceEnvRefs = openejbEntityBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = entityBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }


    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = messageDrivenBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = messageDrivenBean.getEjbRefArray();
        OpenejbRemoteRefType[] openejbEjbRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbEjbRefs = openejbMessageDrivenBean.getEjbRefArray();
        }

        EjbLocalRefType[] ejbLocalRefs = messageDrivenBean.getEjbLocalRefArray();
        OpenejbLocalRefType[] openejbEjbLocalRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbEjbLocalRefs = openejbMessageDrivenBean.getEjbLocalRefArray();
        }

        // resource refs
        ResourceRefType[] resourceRefs = messageDrivenBean.getResourceRefArray();
        OpenejbLocalRefType[] openejbResourceRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbResourceRefs = openejbMessageDrivenBean.getResourceRefArray();
        }

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = messageDrivenBean.getResourceEnvRefArray();
        OpenejbLocalRefType[] openejbResourceEnvRefs = null;
        if (openejbMessageDrivenBean != null) {
            openejbResourceEnvRefs = openejbMessageDrivenBean.getResourceEnvRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = messageDrivenBean.getMessageDestinationRefArray();

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    private static ReadOnlyContext buildComponentContext(EARContext earContext,
                                                         EJBModule ejbModule,
                                                         EnvEntryType[] envEntries,
                                                         EjbRefType[] ejbRefs,
                                                         OpenejbRemoteRefType[] openejbEjbRefs,
                                                         EjbLocalRefType[] ejbLocalRefs,
                                                         OpenejbLocalRefType[] openejbEjbLocalRefs,
                                                         ResourceRefType[] resourceRefs,
                                                         OpenejbLocalRefType[] openejbResourceRefs,
                                                         ResourceEnvRefType[] resourceEnvRefs,
                                                         OpenejbLocalRefType[] openejbResourceEnvRefs,
                                                         MessageDestinationRefType[] messageDestinationRefs, UserTransaction userTransaction,
                                                         ClassLoader cl) throws NamingException, DeploymentException {

        Map ejbRefMap = mapRefs(openejbEjbRefs);
        Map ejbLocalRefMap = mapRefs(openejbEjbLocalRefs);
        Map resourceRefMap = mapRefs(openejbResourceRefs);
        Map resourceEnvRefMap = mapRefs(openejbResourceEnvRefs);

        URI uri = ejbModule.getURI();

        return ENCConfigBuilder.buildComponentContext(earContext, uri, userTransaction, envEntries, ejbRefs, ejbRefMap, ejbLocalRefs, ejbLocalRefMap, resourceRefs, resourceRefMap, resourceEnvRefs, resourceEnvRefMap, messageDestinationRefs, cl);

    }

    private static Map mapRefs(OpenejbRemoteRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                OpenejbRemoteRefType ref = refs[i];
                refMap.put(ref.getRefName(), new OpenEJBRefAdapter(ref));
            }
        }
        return refMap;
    }


    private void setResourceEnvironment(ResourceEnvironmentBuilder builder, ResourceRefType[] resourceRefArray, OpenejbLocalRefType[] openejbResourceRefArray) {
        Map openejbNames = new HashMap();
        for (int i = 0; i < openejbResourceRefArray.length; i++) {
            OpenejbLocalRefType openejbLocalRefType = openejbResourceRefArray[i];
            openejbNames.put(openejbLocalRefType.getRefName(), openejbLocalRefType.getTargetName());
        }
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        for (int i = 0; i < resourceRefArray.length; i++) {
            ResourceRefType resourceRefType = resourceRefArray[i];
            String name = (String) openejbNames.get(resourceRefType.getResRefName().getStringValue());
            if ("Unshareable".equals(getJ2eeStringValue(resourceRefType.getResSharingScope()))) {
                unshareableResources.add(name);
            }
            if ("Application".equals(resourceRefType.getResAuth().getStringValue())) {
                applicationManagedSecurityResources.add(name);
            }
        }
        builder.setUnshareableResources(unshareableResources);
        builder.setApplicationManagedSecurityResources(applicationManagedSecurityResources);
    }

    private URI getDependencyURI(OpenejbDependencyType dep) throws DeploymentException {
        URI uri;
        if (dep.isSetUri()) {
            try {
                uri = new URI(dep.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dep.getUri(), e);
            }
        } else {
            // @todo support more than just jars
            String id = dep.getGroupId() + "/jars/" + dep.getArtifactId() + '-' + dep.getVersion() + ".jar";
            try {
                uri = new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dep.getGroupId() + ", artifactId=" + dep.getArtifactId() + ", version=" + dep.getVersion(), e);
            }
        }
        return uri;
    }


    private static String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    public static Security buildSecurityConfig(OpenejbOpenejbJarType openejbEjbJar) {
        Security security = null;

        OpenejbSecurityType securityType = openejbEjbJar.getSecurity();
        if (securityType != null) {
            security = new Security();

            security.setUseContextHandler(securityType.getUseContextHandler());
            security.setDefaultRole(securityType.getDefaultRole());

            OpenejbDefaultPrincipalType defaultPrincipalType = securityType.getDefaultPrincipal();
            DefaultPrincipal defaultPrincipal = new DefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName());
            defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));

            security.setDefaultPrincipal(defaultPrincipal);

            OpenejbRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            if (roleMappingsType != null) {
                for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                    OpenejbRoleType roleType = roleMappingsType.getRoleArray(i);
                    Role role = new Role();

                    role.setRoleName(roleType.getRoleName());

                    for (int j = 0; j < roleType.sizeOfRealmArray(); j++) {
                        OpenejbRealmType realmType = roleType.getRealmArray(j);
                        Realm realm = new Realm();

                        realm.setRealmName(realmType.getRealmName());

                        for (int k = 0; k < realmType.sizeOfPrincipalArray(); k++) {
                            realm.getPrincipals().add(buildPrincipal(realmType.getPrincipalArray(k)));
                        }

                        role.getRealms().add(realm);
                    }

                    security.getRoleMappings().add(role);
                }
            }
        }

        return security;
    }

    private static Principal buildPrincipal(OpenejbPrincipalType principalType) {
        Principal principal = new Principal();

        principal.setClassName(principalType.getClass1());
        principal.setPrincipalName(principalType.getName());
        principal.setDesignatedRunAs(principalType.isSetDesignatedRunAs());

        return principal;
    }

    /**
     * Fill the container builder with the security information that it needs
     * to create the proper interceptors.  A <code>SecurityConfiguration</code>
     * is also filled with permissions that need to be used to fill the JACC
     * policy configuration.
     *
     * @param builder            the container builder that is to be filled
     * @param notAssigned        the set of all possible permissions.  These will be
     *                           culled so that all that are left are those that have
     *                           not been assigned roles.
     * @param security           the OpenEJB security information already parsed
     *                           from XML descriptor into a POJO
     * @param assemblyDescriptor the assembly descriptor
     * @param EJBName            the name of the EJB
     * @param securityIdentity   the EJB's security identity
     * @param roleReferences     the EJB's role references
     * @throws DeploymentException if any constraints are violated
     */
    private static void fillContainerBuilderSecurity(SecureBuilder builder,
                                                     Permissions notAssigned,
                                                     Security security,
                                                     AssemblyDescriptorType assemblyDescriptor,
                                                     String EJBName,
                                                     SecurityIdentityType securityIdentity,
                                                     SecurityRoleRefType[] roleReferences)
            throws DeploymentException {

        if (security == null) return;

        SecurityConfiguration securityConfiguration = new SecurityConfiguration();

        builder.setSecurityConfiguration(securityConfiguration);
        builder.setDoAsCurrentCaller(security.isDoAsCurrentCaller());
        builder.setUseContextHandler(security.isUseContextHandler());

        /**
         * Add the default subject
         */
        builder.setDefaultSubject(createDefaultSubject(security));

        /**
         * JACC v1.0 section 3.1.5.1
         */
        MethodPermissionType[] methodPermissions = assemblyDescriptor.getMethodPermissionArray();
        if (methodPermissions != null) {
            for (int i = 0; i < methodPermissions.length; i++) {
                MethodPermissionType mpt = methodPermissions[i];
                MethodType[] methods = mpt.getMethodArray();
                RoleNameType[] roles = mpt.getRoleNameArray();
                boolean unchecked = (mpt.getUnchecked() != null);

                Map rolePermissions = securityConfiguration.getRolePolicies();

                for (int j = 0; j < roles.length; j++) {
                    String rolename = roles[j].getStringValue();

                    Permissions permissions = (Permissions) rolePermissions.get(rolename);
                    if (permissions == null) {
                        permissions = new Permissions();
                        rolePermissions.put(rolename, permissions);
                    }

                    for (int k = 0; k < methods.length; k++) {
                        MethodType method = methods[k];

                        if (!EJBName.equals(method.getEjbName().getStringValue())) continue;

                        String methodName = getJ2eeStringValue(method.getMethodName());
                        String methodIntf = getJ2eeStringValue(method.getMethodIntf());
                        String[] methodPara = (method.getMethodParams() != null ? ConfigurationUtil.toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                        // map EJB semantics to JACC semantics for method names
                        if ("*".equals(methodName)) methodName = null;

                        EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);
                        notAssigned = cullPermissions(notAssigned, permission);
                        if (unchecked) {
                            securityConfiguration.getUncheckedPolicy().add(permission);
                        } else {
                            permissions.add(permission);
                        }
                    }
                }

            }
        }

        /**
         * JACC v1.0 section 3.1.5.2
         */
        ExcludeListType excludeList = assemblyDescriptor.getExcludeList();
        if (excludeList != null) {
            MethodType[] methods = excludeList.getMethodArray();
            for (int i = 0; i < methods.length; i++) {
                MethodType method = methods[i];

                if (!EJBName.equals(method.getEjbName().getStringValue())) continue;

                String methodName = getJ2eeStringValue(method.getMethodName());
                String methodIntf = getJ2eeStringValue(method.getMethodIntf());
                String[] methodPara = (method.getMethodParams() != null ? ConfigurationUtil.toStringArray(method.getMethodParams().getMethodParamArray()) : null);

                EJBMethodPermission permission = new EJBMethodPermission(EJBName, methodName, methodIntf, methodPara);

                securityConfiguration.getExcludedPolicy().add(permission);
                notAssigned = cullPermissions(notAssigned, permission);
            }
        }

        /**
         * JACC v1.0 section 3.1.5.3
         */
        if (roleReferences != null) {
            for (int i = 0; i < roleReferences.length; i++) {
                if (roleReferences[i].getRoleLink() == null) throw new DeploymentException("Missing role-link");

                String roleName = roleReferences[i].getRoleName().getStringValue();
                String roleLink = roleReferences[i].getRoleLink().getStringValue();

                Map roleRefPermissions = securityConfiguration.getRoleReferences();
                Set roleLinks = (Set) roleRefPermissions.get(roleLink);
                if (roleLinks == null) {
                    roleLinks = new HashSet();
                    roleRefPermissions.put(roleLink, roleLinks);

                }
                roleLinks.add(new EJBRoleRefPermission(EJBName, roleName));
            }
        }

        /**
         * Set the security interceptor's run-as subject, if one has been defined.
         */
        if (securityIdentity != null && securityIdentity.getRunAs() != null) {
            String roleName = securityIdentity.getRunAs().getRoleName().getStringValue();
            boolean found = false;

            Iterator rollMappings = security.getRoleMappings().iterator();
            while (rollMappings.hasNext()) {
                Role role = (Role) rollMappings.next();

                if (!roleName.equals(role.getRoleName())) continue;

                Subject roleDesignate = new Subject();

                Iterator realms = role.getRealms().iterator();
                while (realms.hasNext()) {
                    Set principalSet = new HashSet();
                    Realm realm = (Realm) realms.next();

                    Iterator principals = realm.getPrincipals().iterator();
                    while (principals.hasNext()) {
                        Principal principal = (Principal) principals.next();

                        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal, realm.getRealmName());

                        if (realmPrincipal == null) throw new DeploymentException("Unable to create realm principal");

                        principalSet.add(realmPrincipal);
                        if (principal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(realmPrincipal);
                    }
                }

                if (roleDesignate.getPrincipals().size() > 0) {
                    builder.setRunAs(roleDesignate);
                    found = true;
                    break;
                }
            }

            if (!found) throw new DeploymentException("Role designate not found for role: " + roleName);
        }

        /**
         * EJB v2.1 section 21.3.2
         *
         * It is possible that some methods are not assigned to any security
         * roles nor contained in the <code>exclude-list</code> element. In
         * this case, it is the responsibility of the Deployer to assign method
         * permissions for all of the unspecified methods, either by assigning
         * them to security roles, or by marking them as <code>unchecked</code>.
         */
        Permissions permissions;
        if (security.getDefaultRole() == null || security.getDefaultRole().length() == 0) {
            permissions = securityConfiguration.getUncheckedPolicy();
        } else {
            Map rolePermissions = securityConfiguration.getRolePolicies();
            permissions = (Permissions) rolePermissions.get(security.getDefaultRole());
            if (permissions == null) {
                permissions = new Permissions();
                rolePermissions.put(security.getDefaultRole(), permissions);
            }
        }

        Enumeration enum = notAssigned.elements();
        while (enum.hasMoreElements()) {
            permissions.add((Permission) enum.nextElement());
        }
    }

    private static Subject createDefaultSubject(Security security) {

        Subject defaultSubject = new Subject();

        DefaultPrincipal principal = security.getDefaultPrincipal();

        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (realmPrincipal == null) throw new GeronimoSecurityException("Unable to create realm principal");
        PrimaryRealmPrincipal primaryRealmPrincipal = ConfigurationUtil.generatePrimaryRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (primaryRealmPrincipal == null) throw new GeronimoSecurityException("Unable to create primary realm principal");

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        return defaultSubject;
    }

    /**
     * Gernate all the possible permissions for a bean's interface.
     * <p/>
     * Method permissions are defined in the deployment descriptor as a binary
     * relation from the set of security roles to the set of methods of the
     * home, component, and/or web service endpoint interfaces of session and
     * entity beans, including all their superinterfaces (including the methods
     * of the <code>EJBHome</code> and <code>EJBObject</code> interfaces and/or
     * <code>EJBLocalHome</code> and <code>EJBLocalObject</code> interfaces).
     *
     * @param permissions     the permission set to be extended
     * @param EJBName         the name of the EJB
     * @param methodInterface the EJB method interface
     * @param interfaceClass  the class name of the interface to be used to
     *                        generate the permissions
     * @param cl              the class loader to be used in obtaining the interface class
     * @throws DeploymentException
     */
    private static void addToPermissions(Permissions permissions,
                                         String EJBName, String methodInterface, String interfaceClass,
                                         ClassLoader cl)
            throws DeploymentException {

        if (interfaceClass == null) return;

        try {
            Class clazz = Class.forName(interfaceClass, false, cl);
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                permissions.add(new EJBMethodPermission(EJBName, methodInterface, methods[i]));
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        }

    }

    /**
     * Removes permissions from <code>toBeChecked</code> that are implied by
     * <code>permission</code>.
     *
     * @param toBeChecked the permissions that are to be checked and possibly
     *                    culled
     * @param permission  the permission that is to be used for culling
     * @return the culled set of permissions that are not implied by
     *         <code>permission</code>
     */
    private static Permissions cullPermissions(Permissions toBeChecked, Permission permission) {
        Permissions result = new Permissions();

        Enumeration enum = toBeChecked.elements();
        while (enum.hasMoreElements()) {
            Permission test = (Permission) enum.nextElement();
            if (!permission.implies(test)) {
                result.add(test);
            }
        }

        return result;
    }

    private interface InstallCallback {

        /**
         * Installs in the specified EARContext and based on the provided URI
         * a module.
         */
        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException;
        
        /**
         * @return the ejb-jar.xml file as an InputStream.
         */
        public InputStream getEjbJarDD() throws DeploymentException, IOException;

        /**
         * @return the openejb-jar.xml file as an InputStream. If this file
         *  does not exist, then null is returned.
         */
        public InputStream getOpenejbJarDD() throws DeploymentException, IOException;

    }

    private static class UnPackedInstallCallback implements InstallCallback {

        private final File ejbFolder;
        
        private final Module ejbModule;
        
        private UnPackedInstallCallback(Module ejbModule, File ejbFolder) {
            this.ejbFolder = ejbFolder;
            this.ejbModule = ejbModule;
        }

        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException {
            try {
                moduleBase = new URI(moduleBase.toString() + "/");
            } catch (URISyntaxException e) {
                throw new DeploymentException(e);
            }
            URI baseURI = ejbFolder.toURI();
            Collection files = new ArrayList();
            FileUtil.listRecursiveFiles(ejbFolder, files);
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                URI path = baseURI.relativize(file.toURI());
                URI target = moduleBase.resolve(path);
                earContext.addFile(target, file);
            }
            earContext.addToClassPath(moduleBase, ejbFolder.toURL());
        }

        public InputStream getEjbJarDD() throws DeploymentException, IOException {
            File ejbJarFile = new File(ejbFolder, "META-INF/ejb-jar.xml");
            if ( !ejbJarFile.exists() ) {
                throw new DeploymentException("No META-INF/ejb-jar.xml in module [" + ejbModule.getName() + "]");
            }
            return new FileInputStream(ejbJarFile);
        }

        public InputStream getOpenejbJarDD() throws DeploymentException, IOException {
            File openejbEjbJarFile = new File(ejbFolder, "META-INF/openejb-jar.xml");
            if ( openejbEjbJarFile.exists() ) {
                return new FileInputStream(openejbEjbJarFile);
            }
            return null;
        }
        
    }
    
    private static class PackedInstallCallback implements InstallCallback {

        private final Module ejbModule;
        
        private final JarFile ejbJarFile;
        
        private PackedInstallCallback(Module ejbModule, JarFile ejbJarFile) {
            this.ejbModule = ejbModule;
            this.ejbJarFile = ejbJarFile;
        }
        
        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException {
            earContext.addStreamInclude(moduleBase, new FileInputStream(ejbJarFile.getName()));
        }

        public InputStream getEjbJarDD() throws DeploymentException, IOException {
            JarEntry entry = ejbJarFile.getJarEntry("META-INF/ejb-jar.xml");
            if (entry == null) {
                throw new DeploymentException("No META-INF/ejb-jar.xml in module [" + ejbModule.getName() + "]");
            }
            return ejbJarFile.getInputStream(entry);
        }

        public InputStream getOpenejbJarDD() throws DeploymentException, IOException {
            JarEntry entry = ejbJarFile.getJarEntry("META-INF/openejb-jar.xml");
            if (entry != null) {
                return ejbJarFile.getInputStream(entry);
            }
            return null;
        }
        
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(OpenEJBModuleBuilder.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(ModuleBuilder.class);
        infoFactory.addInterface(EJBReferenceBuilder.class);

        infoFactory.setConstructor(new String[]{"kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
