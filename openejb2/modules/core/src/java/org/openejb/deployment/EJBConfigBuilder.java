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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
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
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.openejb.EJBModule;
import org.openejb.slsb.StatelessContainerBuilder;
import org.openejb.transaction.EJBUserTransaction;
import org.openejb.xbeans.ejbjar.OpenejbGbeanType;
import org.openejb.xbeans.ejbjar.OpenejbLocalRefType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;

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
// todo needs generic web XMLBeans
//            if (plan == null) {
//                plan = getPlan(new URL(moduleBase, "WEB-INF/geronimo-web.xml"));
//            }
// todo should be able to deploy a naked WAR
//            if (plan == null) {
//                plan = getPlan(new URL(moduleBase, "WEB-INF/web.xml"), WebAppDocument.type);
//            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }


    public void buildConfiguration(File outfile, File module, XmlObject plan) throws IOException, DeploymentException {
        if (module.isDirectory()) {
            throw new DeploymentException("Unpacked ejb-jars are not supported");
        }
        FileInputStream is = new FileInputStream(module);
        try {
            buildConfiguration(outfile, new JarInputStream(new BufferedInputStream(is)), plan);
            return;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void buildConfiguration(File outfile, JarInputStream module, XmlObject plan) throws IOException, DeploymentException {
        EjbJarType ejbJar = null;
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

            // add the jarfile's content to the configuration
            ZipEntry src;
            while ((src = module.getNextEntry()) != null) {
                URI target = URI.create(src.getName());
                if ("META-INF/ejb-jar.xml".equals(src.getName())) {
                    byte[] buffer = getBytes(module);
                    context.addFile(target, new ByteArrayInputStream(buffer));
                    try {
                        EjbJarDocument doc = (EjbJarDocument) XmlBeansUtil.parse(new ByteArrayInputStream(buffer), EjbJarDocument.type);
                        ejbJar = doc.getEjbJar();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse ejb-jar.xml");
                    }
                } else {
                    context.addFile(target, module);
                }
            }

            if (ejbJar == null) {
                throw new DeploymentException("Did not find META-INF/ejb-jar.xml in module");
            }

            buildGBeanConfiguration(context, openejbEjbJar, ejbJar);


            context.close();
            os.flush();
        } finally {
            fos.close();
        }
    }

    private void buildGBeanConfiguration(DeploymentContext context, OpenejbOpenejbJarType openejbEjbJar, EjbJarType ejbJar) throws DeploymentException {
        ClassLoader cl = context.getClassLoader(repository);

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
        OpenejbMessageDrivenBeanType[] openejbMessageDrivenBean = openejbEjbJar.getEnterpriseBeans().getMessageDrivenArray();
        for (int i = 0; i < openejbMessageDrivenBean.length; i++) {
            OpenejbMessageDrivenBeanType messageDrivenBean = openejbMessageDrivenBean[i];
            openejbBeans.put(messageDrivenBean.getEjbName(), messageDrivenBean);
        }


        TransactionPolicyHelper transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());

        // Session Beans
        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sessionBeans.length; i++) {
            SessionBeanType sessionBean = sessionBeans[i];
            String ejbName = sessionBean.getEjbName().getStringValue();
            OpenejbSessionBeanType openejbSessionBean = (OpenejbSessionBeanType)openejbBeans.get(ejbName);
            TransactionPolicySource txPolicySource = transactionPolicyHelper.getTransactionPolicySource(ejbName);

            ObjectName sessionObjectName = createSessionObjectName(
                    sessionBean,
                    "openejb",
                    "null",
                    "null",
                    ejbModuleName);

            GBeanMBean sessionGBean = createSessionBean(sessionObjectName, sessionBean, openejbSessionBean, txPolicySource, cl);
            context.addGBean(sessionObjectName, sessionGBean);
        }
    }

    public GBeanMBean createSessionBean(Object containerId, SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, TransactionPolicySource transactionPolicySource, ClassLoader cl) throws DeploymentException {
        String ejbName = sessionBean.getEjbName().getStringValue();

        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(cl);
        builder.setContainerId(containerId);
        builder.setEJBName(ejbName);
        builder.setBeanClassName(sessionBean.getEjbClass().getStringValue());
        builder.setHomeInterfaceName(getJ2eeStringValue(sessionBean.getHome()));
        builder.setRemoteInterfaceName(getJ2eeStringValue(sessionBean.getRemote()));
        builder.setLocalHomeInterfaceName(getJ2eeStringValue(sessionBean.getLocalHome()));
        builder.setLocalInterfaceName(getJ2eeStringValue(sessionBean.getLocal()));

        EJBUserTransaction userTransaction;
        if ("Bean".equals(sessionBean.getTransactionType().getStringValue())) {
            userTransaction = new EJBUserTransaction();
            builder.setUserTransaction(userTransaction);
            builder.setTransactionPolicySource(TransactionPolicyHelper.StatelessBMTPolicySource);
        } else {
            userTransaction = null;
            builder.setTransactionPolicySource(transactionPolicySource);
        }

        try {
            ReadOnlyContext compContext = buildComponentContext(sessionBean, openejbSessionBean, userTransaction, cl);
            builder.setComponentContext(compContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create EJB jndi environment: ejbName" + ejbName, e);
        }

        try {
            GBeanMBean gbean = builder.createConfiguration();
            gbean.setReferencePatterns("transactionManager", Collections.singleton(new ObjectName("*:type=TransactionManager,*")));
            gbean.setReferencePatterns("trackedConnectionAssociator", Collections.singleton(new ObjectName("*:type=ConnectionTracker,*")));
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

    private ObjectName createEJBObjectName(
            String type,
            String domainName,
            String serverName,
            String applicationName,
            String moduleName,
            String ejbName) throws DeploymentException {

        Properties nameProps = new Properties();
        nameProps.put("j2eeType", type);
        nameProps.put("name", ejbName);
        nameProps.put("J2EEServer", serverName);
        nameProps.put("J2EEApplication", applicationName);
        nameProps.put("J2EEModule", moduleName);

        try {
            return new ObjectName(domainName, nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }
    }

    private ReadOnlyContext buildComponentContext(SessionBeanType sessionBean, OpenejbSessionBeanType openejbSessionBean, UserTransaction userTransaction, ClassLoader cl) throws Exception {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());

        if (userTransaction != null) {
            builder.addUserTransaction(userTransaction);
        }

        EnvEntryType[] envEntries = sessionBean.getEnvEntryArray();
        ENCConfigBuilder.addEnvEntries(envEntries, builder);

        // todo ejb-ref

        // todo ejb-local-ref

        // resource-ref
        if(openejbSessionBean != null) {
            Map resourceRefMap = new HashMap();
            OpenejbLocalRefType[] openejbResourceRefs = openejbSessionBean.getResourceRefArray();
            for (int i = 0; i < openejbResourceRefs.length; i++) {
                OpenejbLocalRefType jettyResourceRef = openejbResourceRefs[i];
                resourceRefMap.put(jettyResourceRef.getRefName(), new OpenEJBRefAdapter(jettyResourceRef));
            }
            ENCConfigBuilder.addResourceRefs(sessionBean.getResourceRefArray(), cl, resourceRefMap, builder);
        }

        // resource-env-ref
        if(openejbSessionBean != null) {
            Map resourceEnvRefMap = new HashMap();
            OpenejbLocalRefType[] openejbResourceEnvRefs = openejbSessionBean.getResourceEnvRefArray();
            for (int i = 0; i < openejbResourceEnvRefs.length; i++) {
                OpenejbLocalRefType openejbResourceEnvRef = openejbResourceEnvRefs[i];
                resourceEnvRefMap.put(openejbResourceEnvRef.getRefName(), new OpenEJBRefAdapter(openejbResourceEnvRef));
            }
            ENCConfigBuilder.addResourceEnvRefs(sessionBean.getResourceEnvRefArray(), cl, resourceEnvRefMap, builder);
        }

        // todo message-destination-ref

        return builder.getContext();
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

    private byte[] getBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        while ((count = is.read(buffer)) > 0) {
            baos.write(buffer, 0, count);
        }
        return baos.toByteArray();
    }

    private String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
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
