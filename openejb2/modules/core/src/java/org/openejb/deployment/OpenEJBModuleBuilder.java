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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.connector.ActivationSpecInfo;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.CmpFieldType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.ContainerBuilder;
import org.openejb.EJBModuleImpl;
import org.openejb.ResourceEnvironmentBuilder;
import org.openejb.dispatch.MethodSignature;
import org.openejb.entity.bmp.BMPContainerBuilder;
import org.openejb.entity.cmp.CMPContainerBuilder;
import org.openejb.mdb.MDBContainerBuilder;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyObjectFactory;
import org.openejb.proxy.ProxyRefAddr;
import org.openejb.sfsb.StatefulContainerBuilder;
import org.openejb.slsb.StatelessContainerBuilder;
import org.openejb.xbeans.ejbjar.OpenejbDependencyType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbGbeanType;
import org.openejb.xbeans.ejbjar.OpenejbLocalRefType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbQueryType;
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
public class OpenEJBModuleBuilder implements ModuleBuilder {
    private static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
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

        OpenejbOpenejbJarDocument openejbJarDocument = OpenejbOpenejbJarDocument.Factory.newInstance();
        OpenejbOpenejbJarType openejbEjbJar = openejbJarDocument.addNewOpenejbJar();
        openejbEjbJar.setParentId("org/apache/geronimo/Server");
        String id = ejbJar.getId();
        if (id == null) {
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }
        openejbEjbJar.setConfigId(id);

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
        OpenejbOpenejbJarType vendorDD = ((OpenejbOpenejbJarDocument)plan).getOpenejbJar();
        module.setVendorDD(vendorDD);
        return module;
    }

    public URI getParentId(XmlObject plan) throws DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = ((OpenejbOpenejbJarDocument)plan).getOpenejbJar();
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
        OpenejbOpenejbJarType openejbEjbJar = ((OpenejbOpenejbJarDocument)plan).getOpenejbJar();
        URI configID;
        try {
            configID = new URI(openejbEjbJar.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + openejbEjbJar.getConfigId(), e);
        }
        return configID;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule) throws DeploymentException {
        try {
            // get an input stream for the ejb-jar and the target location in the earContext
            InputStream in = null;
            URI ejbJarModuleLocation;
            if (!ejbModule.getURI().equals(URI.create("/"))) {
                ZipEntry ejbJarEntry = earFile.getEntry(ejbModule.getURI().toString());
                in = earFile.getInputStream(ejbJarEntry);
                ejbJarModuleLocation = ejbModule.getURI();
            } else {
                in = new FileInputStream(earFile.getName());
                ejbJarModuleLocation = URI.create("ejb.jar");
            }

            // copy the ejb jar file into the earContext and add it to the earContext class loader
            File tempFile = earContext.addStreamInclude(ejbJarModuleLocation, in);
            JarFile ejbJarFile = new JarFile(tempFile);

            // load the ejb-jar.xml file
            EjbJarType ejbJar;
            try {
                JarEntry ejbJarEntry = ejbJarFile.getJarEntry("META-INF/ejb-jar.xml");
                if (ejbJarEntry == null) {
                    throw new DeploymentException("Did not find META-INF/ejb-jar.xml in module");
                }
                XmlObject dd = SchemaConversionUtils.parse(ejbJarFile.getInputStream(ejbJarEntry));
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
                    JarEntry openEjbJarEntry = ejbJarFile.getJarEntry("META-INF/openejb-jar.xml");
                    if (openEjbJarEntry == null) {
                        throw new DeploymentException("Did not find META-INF/openejb-jar.xml in module");
                    }
                    OpenejbOpenejbJarDocument doc = (OpenejbOpenejbJarDocument) XmlBeansUtil.parse(ejbJarFile.getInputStream(openEjbJarEntry), OpenejbOpenejbJarDocument.type);
                    openEjbJar = doc.getOpenejbJar();
                    ejbModule.setVendorDD(openEjbJar);
                } catch (XmlException e) {
                    throw new DeploymentException("Unable to parse openejb-jar.xml");
                }
            }

            assert openEjbJar != null: "openejb-jar.xml not defined";
            ejbModule.setVendorDD(openEjbJar);

            // add the dependencies declared in the openejb-jar.xml file
            OpenejbDependencyType[] dependencies = openEjbJar.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying ejb jar", e);
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
                assureEJBObjectInterface(remote, cl);

                String home = getJ2eeStringValue(sessionBean.getHome());
                assureEJBHomeInterface(home, cl);

                ProxyRefAddr address = ProxyRefAddr.createRemote(sessionObjectName.getCanonicalName(), true, remote, home);
                Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
                earContext.addEJBRef(module.getURI(), ejbName, reference);
            }

            // ejb-local-ref
            if (sessionBean.isSetLocal()) {
                String local = getJ2eeStringValue(sessionBean.getLocal());
                assureEJBLocalObjectInterface(local, cl);

                String localHome = getJ2eeStringValue(sessionBean.getLocalHome());
                assureEJBLocalHomeInterface(localHome, cl);

                ProxyRefAddr address = ProxyRefAddr.createLocal(sessionObjectName.getCanonicalName(), true, local, localHome);
                Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
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
                assureEJBObjectInterface(remote, cl);

                String home = getJ2eeStringValue(entityBean.getHome());
                assureEJBHomeInterface(home, cl);

                ProxyRefAddr address = ProxyRefAddr.createRemote(entityObjectName.getCanonicalName(), false, remote, home);
                Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
                earContext.addEJBRef(module.getURI(), ejbName, reference);
            }

            // ejb-local-ref
            if (entityBean.isSetLocal()) {
                String local = getJ2eeStringValue(entityBean.getLocal());
                assureEJBLocalObjectInterface(local, cl);

                String localHome = getJ2eeStringValue(entityBean.getLocalHome());
                assureEJBLocalHomeInterface(localHome, cl);

                ProxyRefAddr address = ProxyRefAddr.createLocal(entityObjectName.getCanonicalName(), false, local, localHome);
                Reference reference = new Reference(null, address, ProxyObjectFactory.class.getName(), null);
                earContext.addEJBLocalRef(module.getURI(), ejbName, reference);
            }
        }
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = getJ2eeStringValue(messageDrivenBean.getMessagingType());
            assureEJBObjectInterface(messagingType, cl);
        }
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

        TransactionPolicyHelper transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionBean.getEjbName().getStringValue());
            ObjectName sessionObjectName = createEJBObjectName(earContext, module.getName(), sessionBean);

            GBeanMBean sessionGBean = createSessionBean(earContext, ejbModule, sessionObjectName.getCanonicalName(), sessionBean, openejbSessionBean, transactionPolicyHelper, cl);
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
                entityGBean = createCMPBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, ejbSchema, sqlSchema, connectionFactoryName, transactionPolicyHelper, cl);
            } else {
                entityGBean = createBMPBean(earContext, ejbModule, entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, transactionPolicyHelper, cl);
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
                    messageDrivenBean.isSetActivationConfig()? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray(): new ActivationConfigPropertyType[] {},
                    openejbMessageDrivenBean.getResourceAdapterName(),
                    openejbMessageDrivenBean.getActivationSpecClass(),
                    containerId,
                    cl);
            GBeanMBean messageDrivenGBean = createMessageDrivenBean(earContext, ejbModule, containerId, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, cl);
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

    public GBeanMBean createSessionBean(EARContext earContext, EJBModule ejbModule, String containerId, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        ContainerBuilder builder = null;
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        if (isStateless) {
            builder = new StatelessContainerBuilder();
            builder.setTransactedTimerName(earContext.getTransactedTimerName());
            builder.setNonTransactedTimerName(earContext.getNonTransactedTimerName());
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

    public GBeanMBean createBMPBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl) throws DeploymentException {
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

    public GBeanMBean createCMPBean(EARContext earContext, EJBModule ejbModule, String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, EJBSchema ejbSchema, Schema sqlSchema, String connectionFactoryName, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl) throws DeploymentException {
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
                activationSpecInfos = (Map)kernel.getAttribute(resourceAdapterObjectName, "activationSpecInfoMap");
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
            String propertyValue = activationConfigProperty.getActivationConfigPropertyValue().isNil()? null: activationConfigProperty.getActivationConfigPropertyValue().getStringValue();
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
        EjbLocalRefType[] ejbLocalRefs = sessionBean.getEjbLocalRefArray();

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

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, ejbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = entityBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = entityBean.getEjbRefArray();
        EjbLocalRefType[] ejbLocalRefs = entityBean.getEjbLocalRefArray();

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

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, ejbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }


    private ReadOnlyContext buildComponentContext(EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        // env entries
        EnvEntryType[] envEntries = messageDrivenBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = messageDrivenBean.getEjbRefArray();
        EjbLocalRefType[] ejbLocalRefs = messageDrivenBean.getEjbLocalRefArray();

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

        return buildComponentContext(earContext, ejbModule, envEntries, ejbRefs, ejbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, userTransaction, cl);

    }

    private static ReadOnlyContext buildComponentContext(EARContext earContext,
                                                         EJBModule ejbModule,
                                                         EnvEntryType[] envEntries,
                                                         EjbRefType[] ejbRefs,
                                                         EjbLocalRefType[] ejbLocalRefs,
                                                         ResourceRefType[] resourceRefs,
                                                         OpenejbLocalRefType[] openejbResourceRefs,
                                                         ResourceEnvRefType[] resourceEnvRefs,
                                                         OpenejbLocalRefType[] openejbResourceEnvRefs,
                                                         MessageDestinationRefType[] messageDestinationRefs, UserTransaction userTransaction,
                                                         ClassLoader cl) throws NamingException, DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());

        if (userTransaction != null) {
            builder.addUserTransaction(userTransaction);
        }

        ENCConfigBuilder.addEnvEntries(envEntries, builder);

        // ejb-ref
        addEJBRefs(earContext, ejbModule, ejbRefs, cl, builder);

        // ejb-local-ref
        addEJBLocalRefs(earContext, ejbModule, ejbLocalRefs, cl, builder);

        // resource-ref
        if (openejbResourceRefs != null) {
            addResourceRefs(resourceRefs, openejbResourceRefs, cl, builder);
        }

        // resource-env-ref
        if (openejbResourceEnvRefs != null) {
            addResourceEnvRefs(resourceEnvRefs, openejbResourceEnvRefs, cl, builder);
        }

        addMessageDestinationRefs(messageDestinationRefs, cl, builder);

        return builder.getContext();
    }

    private static void addEJBRefs(EARContext earContext, EJBModule ejbModule, EjbRefType[] ejbRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = ejbRef.getEjbRefName().getStringValue();

            String remote = ejbRef.getRemote().getStringValue();
            assureEJBObjectInterface(remote, cl);

            String home = ejbRef.getHome().getStringValue();
            assureEJBHomeInterface(home, cl);

            String ejbLink = getJ2eeStringValue(ejbRef.getEjbLink());
            Object ejbRefObject;
            if (ejbLink != null) {
                ejbRefObject = earContext.getEJBRef(ejbModule.getURI(), ejbLink);
            } else {
                // todo get the id from the openejb-jar.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }

            try {
                builder.bind(ejbRefName, ejbRefObject);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    private static void addEJBLocalRefs(EARContext earContext, EJBModule ejbModule, EjbLocalRefType[] ejbLocalRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = ejbLocalRef.getEjbRefName().getStringValue();

            String local = ejbLocalRef.getLocal().getStringValue();
            assureEJBLocalObjectInterface(local, cl);

            String localHome = ejbLocalRef.getLocalHome().getStringValue();
            assureEJBLocalHomeInterface(localHome, cl);

            String ejbLink = getJ2eeStringValue(ejbLocalRef.getEjbLink());
            Object ejbLocalRefObject;
            if (ejbLink != null) {
                ejbLocalRefObject = earContext.getEJBLocalRef(ejbModule.getURI(), ejbLink);
            } else {
                // todo get the id from the openejb-jar.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }


            try {
                builder.bind(ejbRefName, ejbLocalRefObject);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-local-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    private static void addResourceEnvRefs(ResourceEnvRefType[] resourceEnvRefs, OpenejbLocalRefType[] openejbResourceEnvRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        Map resourceEnvRefMap = new HashMap();
        if (openejbResourceEnvRefs != null) {
            for (int i = 0; i < openejbResourceEnvRefs.length; i++) {
                OpenejbLocalRefType openejbResourceEnvRef = openejbResourceEnvRefs[i];
                resourceEnvRefMap.put(openejbResourceEnvRef.getRefName(), new OpenEJBRefAdapter(openejbResourceEnvRef));
            }
        }
        ENCConfigBuilder.addResourceEnvRefs(resourceEnvRefs, cl, resourceEnvRefMap, builder);
    }

    private static void addResourceRefs(ResourceRefType[] resourceRefs, OpenejbLocalRefType[] openejbResourceRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        Map resourceRefMap = new HashMap();
        if (openejbResourceRefs != null) {
            for (int i = 0; i < openejbResourceRefs.length; i++) {
                OpenejbLocalRefType openejbResourceRef = openejbResourceRefs[i];
                resourceRefMap.put(openejbResourceRef.getRefName(), new OpenEJBRefAdapter(openejbResourceRef));
            }
        }
        ENCConfigBuilder.addResourceRefs(resourceRefs, cl, resourceRefMap, builder);
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

    private static void addMessageDestinationRefs(MessageDestinationRefType[] messageDestinationRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        ENCConfigBuilder.addMessageDestinationRefs(messageDestinationRefs, cl, builder);
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

    private static void assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    private static void assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    private static void assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    private static void assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }

    private static void assureInterface(String interfaceName, String superInterfaceName, String interfactType, ClassLoader cl) throws DeploymentException {
        Class clazz = null;
        try {
            clazz = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfactType + " interface class not found: " + interfaceName);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfactType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface = null;
        try {
            superInterface = cl.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded");
        }
        if (clazz.isAssignableFrom(superInterface)) {
            throw new DeploymentException(interfactType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(OpenEJBModuleBuilder.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(ModuleBuilder.class);

        infoFactory.setConstructor(new String[]{"kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
