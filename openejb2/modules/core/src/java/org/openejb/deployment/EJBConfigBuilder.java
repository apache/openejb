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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

import org.openejb.ContainerBuilder;
import org.openejb.EJBModule;
import org.openejb.entity.bmp.BMPContainerBuilder;
import org.openejb.entity.cmp.CMPContainerBuilder;
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
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.ejbjar.impl.OpenejbOpenejbJarDocumentImpl;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBConfigBuilder implements ConfigurationBuilder {
    private final Repository repository;
    private final Kernel kernel;

    public EJBConfigBuilder(Kernel kernel, Repository repository) {
        this.kernel = kernel;
        this.repository = repository;
    }

    public boolean canConfigure(XmlObject plan) {
        return plan instanceof OpenejbOpenejbJarDocument || plan instanceof EjbJarDocument;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        return new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    }

    public XmlObject getDeploymentPlan(URL module) {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            XmlObject plan = XmlBeansUtil.getXmlObject(new URL(moduleBase, "META-INF/openejb-jar.xml"), OpenejbOpenejbJarDocument.type);
            if (plan == null) {
                URL ejbJarXml = new URL(moduleBase, "META-INF/ejb-jar.xml");
                return createDefaultPlan(ejbJarXml);
            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private OpenejbOpenejbJarDocument createDefaultPlan(URL module) {
        EjbJarDocument ejbJarDoc = (EjbJarDocument) XmlBeansUtil.getXmlObject(module, EjbJarDocument.type);
        if (ejbJarDoc == null) {
            return null;
        }

        EjbJarType ejbJar = ejbJarDoc.getEjbJar();

        OpenejbOpenejbJarDocument doc = new OpenejbOpenejbJarDocumentImpl(OpenejbOpenejbJarDocument.type);
        OpenejbOpenejbJarType openejbEjbJar = doc.addNewOpenejbJar();
        openejbEjbJar.setParentId("org/apache/geronimo/Server");
        String ejbModuleName = ejbJar.getId();
        if (ejbModuleName != null) {
            openejbEjbJar.setConfigId(ejbModuleName);
        } else {
            openejbEjbJar.setConfigId("unnamed/ejbmodule/" + System.currentTimeMillis());
        }
        return doc;
    }

    public void buildConfiguration(File outfile, File module, XmlObject plan) throws IOException, DeploymentException {
        if (module.isDirectory()) {
            throw new DeploymentException("Unpacked ejb-jars are not supported");
        }
        FileInputStream is = new FileInputStream(module);
        try {
            buildConfiguration(outfile, is, plan);
            return;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void buildConfiguration(File outfile, InputStream module, XmlObject plan) throws IOException, DeploymentException {
        OpenejbOpenejbJarType openejbEjbJar = ((OpenejbOpenejbJarDocument) plan).getOpenejbJar();
        URI configID = getConfigID(openejbEjbJar);
        URI parentID = getParentID(openejbEjbJar);

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }
            context.addStreamInclude(URI.create("ejb.jar"), module);

            OpenejbDependencyType[] dependencies = openejbEjbJar.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                context.addDependency(getDependencyURI(dependencies[i]));
            }

            buildGBeanConfiguration(context, openejbEjbJar);

            context.close();
            os.flush();
        } finally {
            fos.close();
        }
    }

    private void buildGBeanConfiguration(DeploymentContext context, OpenejbOpenejbJarType openejbEjbJar) throws DeploymentException {
        ClassLoader cl = context.getClassLoader(repository);

        // load the ejb-jar.xml deployement descriptor
        URL ejbJarXml = cl.getResource("META-INF/ejb-jar.xml");
        if (ejbJarXml == null) {
            throw new DeploymentException("Module does not contain the ejb-jar.xml deployment descriptor");
        }
        EjbJarDocument doc = (EjbJarDocument) XmlBeansUtil.getXmlObject(ejbJarXml, EjbJarDocument.type);
        if (doc == null) {
            throw new DeploymentException("The ejb-jar.xml deployment descriptor is not valid");
        }
        EjbJarType ejbJar = doc.getEjbJar();

        OpenejbGbeanType[] gbeans = openejbEjbJar.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new OpenEJBGBeanAdapter(gbeans[i]), cl, context);
        }

        // add the GBean
        addGBeans(context, ejbJar, openejbEjbJar, cl);
    }

    public void addGBeans(DeploymentContext context, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl) throws DeploymentException {
        String ejbModuleName = context.getConfigID().toString();

        Properties nameProps = new Properties();
        nameProps.put("j2eeType", "EJBModule");
        nameProps.put("name", ejbModuleName);
        nameProps.put("J2EEServer", "null");
        nameProps.put("J2EEApplication", "null");

        ObjectName ejbModuleObjectName;
        try {
            ejbModuleObjectName = new ObjectName("openejb", nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        // EJBModule GBean
        GBeanMBean ejbModuleGBean = new GBeanMBean(EJBModule.GBEAN_INFO);
        try {
            ejbModuleGBean.setReferencePatterns("ejbs", Collections.singleton(new ObjectName("openejb:J2EEServer=null,J2EEApplication=null,EJBModule=" + ejbModuleName + ",*")));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean", e);
        }
        context.addGBean(ejbModuleObjectName, ejbModuleGBean);

        Map objectNameByEJBName = buildObjectNameByEJBNameMap(ejbJar.getEnterpriseBeans(), ejbModuleName);

        // create an index of the openejb ejb configurations by ejb-name
        Map openejbBeans = new HashMap();
        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            ObjectName sessionObjectName = (ObjectName) objectNameByEJBName.get(sessionBean.getEjbName());
            openejbBeans.put(sessionObjectName, sessionBean);
        }
        OpenejbEntityBeanType[] openejbEntityBeans = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openejbEntityBeans.length; i++) {
            OpenejbEntityBeanType entityBean = openejbEntityBeans[i];
            ObjectName entityObjectName = (ObjectName) objectNameByEJBName.get(entityBean.getEjbName());
            openejbBeans.put(entityObjectName, entityBean);
        }
        OpenejbMessageDrivenBeanType[] openejbMessageDrivenBean = openejbEjbJar.getEnterpriseBeans().getMessageDrivenArray();
        for (int i = 0; i < openejbMessageDrivenBean.length; i++) {
            OpenejbMessageDrivenBeanType messageDrivenBean = openejbMessageDrivenBean[i];
            ObjectName mdbObjectName = (ObjectName) objectNameByEJBName.get(messageDrivenBean.getEjbName());
            openejbBeans.put(mdbObjectName, messageDrivenBean);
        }


        TransactionPolicyHelper transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());

        // Session Beans
        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];

            ObjectName sessionObjectName = (ObjectName) objectNameByEJBName.get(sessionBean.getEjbName().getStringValue());
            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType) openejbBeans.get(sessionObjectName);

            GBeanMBean sessionGBean = createSessionBean(sessionObjectName.getCanonicalName(), sessionBean, openejbSessionBean, objectNameByEJBName, transactionPolicyHelper, cl);
            context.addGBean(sessionObjectName, sessionGBean);
        }


        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            ObjectName entityObjectName = (ObjectName) objectNameByEJBName.get(entityBean.getEjbName().getStringValue());
            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityObjectName);

            GBeanMBean entityGBean = createEntityBean(entityObjectName.getCanonicalName(), entityBean, openejbEntityBean, objectNameByEJBName, transactionPolicyHelper, cl);
            context.addGBean(entityObjectName, entityGBean);
        }
    }

    private Map buildObjectNameByEJBNameMap(EnterpriseBeansType enterpriseBeans, String ejbModuleName) throws DeploymentException {
        Map map = new HashMap();

        // Session Beans
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];
            String ejbName = sessionBean.getEjbName().getStringValue();

            ObjectName sessionObjectName = createSessionObjectName(
                    sessionBean,
                    "openejb",
                    "null",
                    "null",
                    ejbModuleName);

            map.put(ejbName, sessionObjectName);
        }


        // Entity Beans
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];
            String ejbName = entityBean.getEjbName().getStringValue();

            ObjectName entityObjectName = createEntityObjectName(
                    entityBean,
                    "openejb",
                    "null",
                    "null",
                    ejbModuleName);

            map.put(ejbName, entityObjectName);
        }
        return map;
    }

    public GBeanMBean createSessionBean(String containerId, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, Map objectNameByEJBName, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        ContainerBuilder builder = null;
        boolean isStateless = "Stateless".equals(sessionBean.getSessionType().getStringValue());
        if (isStateless) {
            builder = new StatelessContainerBuilder();
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
            ReadOnlyContext compContext = buildComponentContext(sessionBean, openejbSessionBean, objectNameByEJBName, userTransaction, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName" + ejbName, e);
        }

        if (openejbSessionBean != null) {
            builder.setJndiNames(openejbSessionBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbSessionBean.getLocalJndiNameArray());
        } else {
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePatterns("TransactionManager", Collections.singleton(new ObjectName("*:type=TransactionManager,*")));
            gbean.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(new ObjectName("*:type=ConnectionTracker,*")));
            return gbean;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private ObjectName createSessionObjectName(
            SessionBeanType sessionBean,
            String domainName,
            String serverName,
            String applicationName,
            String moduleName) throws DeploymentException {

        String ejbName = sessionBean.getEjbName().getStringValue();
        String type = sessionBean.getSessionType().getStringValue() + "SessionBean";

        return createEJBObjectName(type, domainName, serverName, applicationName, moduleName, ejbName);
    }

    public GBeanMBean createEntityBean(String containerId, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, Map objectNameByEJBName, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();

        ContainerBuilder builder = null;
        if ("Container".equals(entityBean.getPersistenceType().getStringValue())) {
            builder = new CMPContainerBuilder();
        } else {
            builder = new BMPContainerBuilder();
        }
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

        try {
            ReadOnlyContext compContext = buildComponentContext(entityBean, openejbEntityBean, objectNameByEJBName, null, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName=" + ejbName, e);
        }

        if (openejbEntityBean != null) {
            builder.setJndiNames(openejbEntityBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
        } else {
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePatterns("TransactionManager", Collections.singleton(new ObjectName("*:type=TransactionManager,*")));
            gbean.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(new ObjectName("*:type=ConnectionTracker,*")));
            return gbean;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
        }
    }

    private ObjectName createEntityObjectName(
            EntityBeanType entityBean,
            String domainName,
            String serverName,
            String applicationName,
            String moduleName) throws DeploymentException {

        String ejbName = entityBean.getEjbName().getStringValue();

        return createEJBObjectName("EntityBean", domainName, serverName, applicationName, moduleName, ejbName);
    }

    private ObjectName createEJBObjectName(
            String type,
            String domainName,
            String serverName,
            String applicationName,
            String moduleName,
            String ejbName) throws DeploymentException {

        Properties nameProps = new Properties();
        nameProps.put("j2eeType", type);
        nameProps.put("J2EEServer", serverName);
        nameProps.put("J2EEApplication", applicationName);
        nameProps.put("J2EEModule", moduleName);
        nameProps.put("name", ejbName);

        try {
            return new ObjectName(domainName, nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }
    }

    private ReadOnlyContext buildComponentContext(SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, Map objectNameByEJBName, UserTransaction userTransaction, ClassLoader cl) throws Exception {
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

        return buildComponentContext(envEntries, ejbRefs, ejbLocalRefs, objectNameByEJBName, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, userTransaction, cl);

    }

    private ReadOnlyContext buildComponentContext(EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, Map objectNameByEJBName, UserTransaction userTransaction, ClassLoader cl) throws Exception {
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

        return buildComponentContext(envEntries, ejbRefs, ejbLocalRefs, objectNameByEJBName, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, userTransaction, cl);

    }

    private static ReadOnlyContext buildComponentContext(EnvEntryType[] envEntries, EjbRefType[] ejbRefs, EjbLocalRefType[] ejbLocalRefs, Map objectNameByEJBName, ResourceRefType[] resourceRefs, OpenejbLocalRefType[] openejbResourceRefs, ResourceEnvRefType[] resourceEnvRefs, OpenejbLocalRefType[] openejbResourceEnvRefs, UserTransaction userTransaction, ClassLoader cl) throws NamingException, DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());

        if (userTransaction != null) {
            builder.addUserTransaction(userTransaction);
        }

        ENCConfigBuilder.addEnvEntries(envEntries, builder);

        // ejb-ref
        addEJBRefs(ejbRefs, objectNameByEJBName, cl, builder);

        // ejb-local-ref
        addEJBLocalRefs(ejbLocalRefs, objectNameByEJBName, cl, builder);

        // resource-ref
        if (openejbResourceRefs != null) {
            addResourceRefs(resourceRefs, openejbResourceRefs, cl, builder);
        }

        // resource-env-ref
        if (openejbResourceEnvRefs != null) {
            addResourceEnvRefs(resourceEnvRefs, openejbResourceEnvRefs, cl, builder);
        }

        // todo message-destination-ref

        return builder.getContext();
    }

    private static void addEJBRefs(EjbRefType[] ejbRefs, Map objectNameByEJBName, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = ejbRef.getEjbRefName().getStringValue();
            String ejbRefType = ejbRef.getEjbRefType().getStringValue();

            String remote = ejbRef.getRemote().getStringValue();
            assureEJBObjectInterface(remote, cl);

            String home = ejbRef.getHome().getStringValue();
            assureEJBHomeInterface(home, cl);

            String ejbLink = getJ2eeStringValue(ejbRef.getEjbLink());
            String containerId;
            if (ejbLink != null) {
                containerId = ((ObjectName)objectNameByEJBName.get(ejbLink)).getCanonicalName();
            } else {
                // todo get the id from the openejb-jar.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }

            try {
                ProxyRefAddr address = new ProxyRefAddr(
                        containerId,
                        ejbRefType.equals("Session"),
                        remote,
                        home,
                        null,
                        null,
                        false);

                builder.bind(ejbRefName, new Reference(null, address, ProxyObjectFactory.class.getName(), null));
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    private static void addEJBLocalRefs(EjbLocalRefType[] ejbLocalRefs, Map objectNameByEJBName, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = ejbLocalRef.getEjbRefName().getStringValue();
            String ejbRefType = ejbLocalRef.getEjbRefType().getStringValue();

            String local = ejbLocalRef.getLocal().getStringValue();
            assureEJBLocalObjectInterface(local, cl);

            String localHome = ejbLocalRef.getLocalHome().getStringValue();
            assureEJBLocalHomeInterface(localHome, cl);

            String ejbLink = getJ2eeStringValue(ejbLocalRef.getEjbLink());
            String containerId;
            if (ejbLink != null) {
                containerId = ((ObjectName)objectNameByEJBName.get(ejbLink)).getCanonicalName();
            } else {
                // todo get the id from the openejb-jar.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }


            try {
                ProxyRefAddr address = new ProxyRefAddr(
                        containerId,
                        ejbRefType.equals("Session"),
                        null,
                        null,
                        local,
                        localHome,
                        true);

                builder.bind(ejbRefName, new Reference(null, address, ProxyObjectFactory.class.getName(), null));
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


    private URI getParentID(OpenejbOpenejbJarType openejbEjbJar) throws DeploymentException {
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

    private URI getConfigID(OpenejbOpenejbJarType openejbEjbJar) throws DeploymentException {
        URI configID;
        try {
            configID = new URI(openejbEjbJar.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + openejbEjbJar.getConfigId(), e);
        }
        return configID;
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(EJBConfigBuilder.class);
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.setConstructor(
                new String[]{"Kernel", "Repository"},
                new Class[]{Kernel.class, Repository.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
