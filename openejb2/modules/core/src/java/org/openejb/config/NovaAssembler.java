/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
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
 *    (http://openejb.sf.net/).
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
 * Copyright 2004 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.config;

/**
 * Created by IntelliJ IDEA.
 * User: dblevins
 * Date: Oct 24, 2004
 * Time: 3:54:51 PM
 * To change this template use File | Settings | File Templates.
 */

import java.beans.PropertyEditor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.main.Daemon;
import org.apache.geronimo.system.main.ToolsJarHack;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.url.GeronimoURLFactory;
import org.openejb.OpenEJBException;
import org.openejb.assembler.Container;
import org.openejb.assembler.DeploymentInfo;
import org.openejb.config.sys.Openejb;
import org.openejb.config.sys.ServiceProvider;
import org.openejb.deployment.OpenEJBModuleBuilder;
import org.openejb.spi.Assembler;
import org.openejb.spi.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.urlhandler.resource.Handler;

public class NovaAssembler implements Assembler {

    private static Log log;
    private String configLocation;
    private Openejb openejb;

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
        log = LogFactory.getLog(Daemon.class.getName());

        // Install our url factory
        GeronimoURLFactory.install();
        new GeronimoURLFactory().registerHandler("resource2",new Handler());
        Map registeredHandlers = new GeronimoURLFactory().getRegisteredHandlers();
        for (Iterator iterator = registeredHandlers.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            System.out.println(entry.getKey()+" = "+entry.getValue());
        }
        // Install the lame tools jar hack
        ToolsJarHack.install();
    }


    public void init(Properties props) throws OpenEJBException {
        if (props == null) props = new Properties();

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = org.openejb.config.ConfigUtils.searchForConfiguration(configLocation);
        System.setProperty("openejb.configuration", configLocation);

    }

    public ContainerSystem getContainerSystem() {
        return new ContainerSystem() {
            public DeploymentInfo getDeploymentInfo(Object id) {
                return null;
            }

            public DeploymentInfo[] deployments() {
                return new DeploymentInfo[0];
            }

            public Container getContainer(Object id) {
                return null;
            }

            public Container[] containers() {
                return new Container[0];
            }

            public Context getJNDIContext() {
                return null;
            }
        };
    }

    public TransactionManager getTransactionManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SecurityService getSecurityService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void build() throws OpenEJBException {
        try {
            String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (str == null) {
                str = ":org.apache.geronimo.naming";
            } else {
                str = str + ":org.apache.geronimo.naming";
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);

            Kernel kernel = setUpKernel("openejb.kernel", j2eeDomainName);

            File testearFile = new File("/Users/dblevins/work/openejb/modules/core/target/test-ejb-jar.jar");
            startEar(testearFile, kernel);

            File dir = new File("beans");
            String[] files = dir.list();
            for (int x = 0; x < files.length; x++) {
                String f = files[x];
                if (!f.endsWith(".jar")) continue;
                File earFile = new File(dir, f);
//                startEar(earFile, kernel);
            }
        } catch (Exception e) {
            throw new OpenEJBException("Unable to construct kernel", e);
        }
    }


    private void startEar(File earFile, Kernel kernel) throws IOException, MalformedObjectNameException, DeploymentException, AttributeNotFoundException, ReflectionException, ClassNotFoundException, InstanceAlreadyExistsException, InvalidConfigException, InstanceNotFoundException, OpenEJBException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{earFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);


        String j2eeModuleName = earFile.getName().split("\\.")[0];
        File tempDir = DeploymentUtil.createTempDir();

        GBeanMBean earGBean = setUpEarModule(kernel, earFile, tempDir);

        // load the configuration
        ObjectName objectName = ObjectName.getInstance(j2eeDomainName + ":configuration=" + j2eeModuleName);
        kernel.loadGBean(objectName, earGBean);
        earGBean.setAttribute("baseURL", tempDir.toURL());

        // start the configuration
        kernel.startRecursiveGBean(objectName);

        Thread.currentThread().setContextClassLoader(oldCl);
    }


    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "OpenEJBServer";

    public static Kernel setUpKernel(String kernelName, String domainName) throws Exception {

        Kernel kernel = new Kernel(kernelName, domainName);
        kernel.boot();

        setUp77Support(kernel);

        startService("HOWLTransactionLog",kernel);
        startService("DefaultWorkManager",kernel);

        startService("TransactionManager",kernel);
        startService("TransactionContextManager",kernel);
        startService("ConnectionTracker",kernel);

        startService("ContainerIndex", kernel);

        startService("DefaultThreadPool", kernel);
        startService("NonTransactionalTimer", kernel);
        startService("TransactionalTimer", kernel);

        return kernel;
    }

    private static void setUp77Support(Kernel kernel) throws ReflectionException, AttributeNotFoundException, MalformedObjectNameException, InstanceAlreadyExistsException, InvalidConfigException, InstanceNotFoundException {
        GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");
        ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ServerInfo");
        kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
        kernel.startGBean(serverInfoObjectName);

        GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
        j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
        ObjectName j2eeServerObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName);
        kernel.loadGBean(j2eeServerObjectName, j2eeServerGBean);
        kernel.startGBean(j2eeServerObjectName);
    }

    private static ObjectName startService(String providerId, Kernel kernel) throws Exception {
        ServiceProvider serviceProvider = ServiceUtils.getServiceProvider(providerId);

        NovaAssembler.GBeanServiceProvider gBeanServiceProvider = new NovaAssembler.GBeanServiceProvider(serviceProvider);


        ObjectName objectName = gBeanServiceProvider.getObjectName();
        GBeanMBean gBean = gBeanServiceProvider.getGBeanMBean();

        kernel.loadGBean(objectName, gBean);
        kernel.startGBean(objectName);
        return objectName;
    }


    private static GBeanMBean setUpEarModule(Kernel kernel, File earFile, File tempDir) throws MalformedObjectNameException, IOException, DeploymentException, AttributeNotFoundException, ReflectionException, ClassNotFoundException, OpenEJBException {
        GBeanMBean earGBean;
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder();
        EARConfigBuilder earConfigBuilder = new EARConfigBuilder(new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                getObjectName("TransactionContextManager"),
                getObjectName("ConnectionTracker"),
                getObjectName("TransactionalTimer"),
                getObjectName("NonTransactionalTimer"),
                null, // repository
                moduleBuilder,  //ejb config builder
                moduleBuilder,  //ejb reference builder 
                null, // web
                null, // connector
                null, //resource reference builder
                null, // app client
                null // kernel
        );

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(earFile);
            Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
            if (plan == null) {
                System.out.println("null plan!!!! " + earFile.getPath());
                throw new RuntimeException("PLan is null");
            }
            earConfigBuilder.buildConfiguration(plan, jarFile, tempDir);
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        InputStream in = new FileInputStream(new File(tempDir, "META-INF/config.ser"));
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config1 = new GBeanMBean(gbeanInfo);
            Configuration.loadGMBeanState(config1, ois);
            earGBean = config1;


        } finally {
            in.close();
        }
        return earGBean;
    }


    static class GBeanServiceProvider{
        private ServiceProvider provider;

        public GBeanServiceProvider(ServiceProvider serviceProvider){
            this.provider = serviceProvider;
        }

        public ObjectName getObjectName() {
            return getObjectName(provider);
        }

        public ObjectName getObjectName(ServiceProvider provider){
            ObjectName objectName = JMXUtil.getObjectName(j2eeDomainName + ":type=" + provider.getProviderType() + ",name=" + provider.getId());
            return objectName;
        }

        public GBeanMBean getGBeanMBean() throws OpenEJBException, ReflectionException, DeploymentException, AttributeNotFoundException {
            GBeanInfo gBeanInfo = GBeanInfo.getGBeanInfo(provider.getClassName(), GBeanInfo.class.getClassLoader());
            GBeanMBean gBean = createGBean(gBeanInfo, provider.getProperties());
            return gBean;
        }

        private GBeanMBean createGBean(GBeanInfo gbeanInfo, Properties properties) throws DeploymentException, ReflectionException, AttributeNotFoundException, OpenEJBException {
            GBeanMBean gbean = new GBeanMBean(gbeanInfo);

            // Get GBean Properties
            HashMap map = new HashMap();
            for (Iterator iterator1 = gbeanInfo.getReferences().iterator(); iterator1.hasNext();) {
                GReferenceInfo referenceInfo = (GReferenceInfo) iterator1.next();
                map.put(referenceInfo.getName(), referenceInfo);
            }

            for (Iterator iterator11 = gbeanInfo.getAttributes().iterator(); iterator11.hasNext();) {
                GAttributeInfo attributeInfo = (GAttributeInfo) iterator11.next();
                map.put(attributeInfo.getName(), attributeInfo);
            }
            Map gBeanProperties = map;
            for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry property = (Map.Entry) iterator.next();
                String name = (String) property.getKey();
                String value = (String) property.getValue();
                Object info = gBeanProperties.get(name);

                if (value == null) {
                    continue;
                }

                if (info instanceof GReferenceInfo) {
                    String[] patterns = value.split(" ");
                    Set refpatterns = new HashSet();
                    for (int i = 0; i < patterns.length; i++) {
                        String pattern = patterns[i];
                        if (pattern.indexOf(':') == -1 && pattern.indexOf('=') == -1) {
                            ServiceProvider provider = ServiceUtils.getServiceProvider(pattern);
                            refpatterns.add( getObjectName(provider));
                        } else if (pattern.indexOf(':') == -1 && pattern.indexOf('=') != -1) {
                            refpatterns.add( JMXUtil.getObjectName(j2eeDomainName + ":" + pattern));
                        } else {
                            refpatterns.add( JMXUtil.getObjectName(pattern));
                        }
                        gbean.setReferencePatterns(name, refpatterns);
                    }

                } else if (info instanceof GAttributeInfo) {
                    GAttributeInfo gAttributeInfo = (GAttributeInfo) info;
                    String type = gAttributeInfo.getType();


                    Class clazz;

                    if ("byte".equals(type)) {
                        clazz = byte.class;
                    } else if ("char".equals(type)) {
                        clazz = char.class;
                    } else if ("short".equals(type)) {
                        clazz = short.class;
                    } else if ("int".equals(type)) {
                        clazz = int.class;
                    } else if ("long".equals(type)) {
                        clazz = long.class;
                    } else if ("float".equals(type)) {
                        clazz = float.class;
                    } else if ("double".equals(type)) {
                        clazz = double.class;
                    } else if ("boolean".equals(type)) {
                        clazz = boolean.class;
                    } else {
                        try {
                            clazz = gbean.getClassLoader().loadClass(type);
                        } catch (ClassNotFoundException e) {
                            throw new DeploymentException("Could not load attribute class: attribute: " + name + ", type: " + type, e);
                        }
                    }

                    PropertyEditor editor = PropertyEditors.getEditor(clazz);

                    Object parsedvalue = null;
                    try {
                        editor.setAsText(value);
                        parsedvalue = editor.getValue();
                    } catch (IllegalArgumentException e) {
                        throw new DeploymentException("Could not parse the value of attribute: attribute: " + name + ", type: " + type + ", value: " + value, e);
                    }

                    gbean.setAttribute(name, parsedvalue);
                }

            }
            return gbean;
        }
    }

    private static ObjectName getObjectName(String serviceName) throws OpenEJBException {
//        ObjectName objectName = JMXUtil.getObjectName(j2eeDomainName + ":type=" + service.getProviderType() + ",name=" + service.getId());
//        return objectName;
        NovaAssembler.GBeanServiceProvider gBeanServiceProvider = new NovaAssembler.GBeanServiceProvider(ServiceUtils.getServiceProvider(serviceName));
        return gBeanServiceProvider.getObjectName();
    }

}
