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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import javax.ejb.EJBHome;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBRefContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.openejb.ContainerIndex;
import org.openejb.DeploymentHelper;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Revision$ $Date$
 */
public class EJBConfigBuilderTest extends TestCase {

    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private Kernel kernel;

    public void testCreateResourceAdapterNameQuery() throws Exception {
        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            OpenEJBModuleBuilder builder = new OpenEJBModuleBuilder(null);
            EARContext earContext = new EARContext(tempDir,
                    URI.create("id"),
                    ConfigurationModuleType.EJB,
                    URI.create("parentId"),
                    null,
                    "geronimo.server",
                    "geronimo",
                    null,
                    null,
                    null,
                    null,
                    null,
                    new EJBRefContext(builder));

            ObjectName testName = builder.createResourceAdapterQueryName(earContext, "TestResourceAdapterName");
            assertEquals(ObjectName.getInstance("geronimo.server:j2eeType=ResourceAdapter,name=TestResourceAdapterName,J2EEServer=geronimo,*"), testName);
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testBuildUnpackedModule() throws Exception {
        executeTestBuildModule(new File("target/test-ejb-jar"));
    }
 
    public void testBuildPackedModule() throws Exception {
        executeTestBuildModule(new File("target/test-ejb-jar.jar"));
    }
    
    private void executeTestBuildModule(File ejbJarFile) throws Exception {
        String j2eeApplicationName = "null";
        String j2eeModuleName = "org/openejb/deployment/test";

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(kernel);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{ejbJarFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        JarFile jarFile = DeploymentUtil.createJarFile(ejbJarFile);
        Module module = moduleBuilder.createModule(null, jarFile);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            EARContext earContext = new EARContext(tempDir,
                    module.getConfigId(),
                    module.getType(),
                    module.getParentId(),
                    null,
                    j2eeDomainName,
                    j2eeServerName,
                    j2eeApplicationName,
                    DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME,
                    DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME,
                    DeploymentHelper.TRANSACTIONALTIMER_NAME,
                    DeploymentHelper.NONTRANSACTIONALTIMER_NAME,
                    new EJBRefContext(moduleBuilder));

            moduleBuilder.installModule(DeploymentUtil.createJarFile(ejbJarFile), earContext, module);
            earContext.getClassLoader(null);
            moduleBuilder.initContext(earContext, module, cl);
            moduleBuilder.addGBeans(earContext, module, cl);
            earContext.close();

            verifyDeployment(tempDir, oldCl, j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName);
        } finally {
            module.close();
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testEJBJarDeploy() throws Exception {
        String j2eeApplicationName = "null";
        String j2eeModuleName = "org/openejb/deployment/test";

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(kernel);
        File earFile = new File("target/test-ejb-jar.jar");

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{earFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            EARConfigBuilder earConfigBuilder = new EARConfigBuilder(
                    new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                    DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME,
                    DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME,
                    DeploymentHelper.TRANSACTIONALTIMER_NAME,
                    DeploymentHelper.NONTRANSACTIONALTIMER_NAME,
                    null, // repository
                    moduleBuilder,
                    moduleBuilder,
                    null, // web
                    null, // connector
                    null, // app client
                    null // kernel
            );

            JarFile jarFile = null;
            try {
                jarFile = new JarFile(earFile);
                Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
                earConfigBuilder.buildConfiguration(plan, jarFile, tempDir);
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }

            verifyDeployment(tempDir, oldCl, j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName);
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testEARDeploy() throws Exception {
        String j2eeApplicationName = "org/apache/geronimo/j2ee/deployment/test";
        String j2eeModuleName = "test-ejb-jar.jar";

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(kernel);
        File earFile = new File("target/test-ear.ear");

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{earFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            EARConfigBuilder earConfigBuilder = new EARConfigBuilder(
                    new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                    DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME,
                    DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME,
                    DeploymentHelper.TRANSACTIONALTIMER_NAME,
                    DeploymentHelper.NONTRANSACTIONALTIMER_NAME,
                    null, // Repository
                    moduleBuilder, 
                    moduleBuilder,
                    null, // web
                    null, // connector
                    null, // app client
                    null // kernel
            );

            JarFile jarFile = DeploymentUtil.createJarFile(earFile);
            Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
            earConfigBuilder.buildConfiguration(plan, jarFile, tempDir);

            verifyDeployment(tempDir, oldCl, j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName);
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    private void verifyDeployment(File tempDir, ClassLoader cl, String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName) throws Exception {
        DataSource ds = null;
        try {
            GBeanMBean config = loadConfig(tempDir);

            GBeanMBean containerIndexGBean = new GBeanMBean(ContainerIndex.GBEAN_INFO);
            ObjectName containerIndexObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ContainerIndex");
            Set ejbContainerNames = new HashSet();
            ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName + ":j2eeType=StatelessSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName + ":j2eeType=StatefulSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName + ":j2eeType=EntityBean,*"));
            containerIndexGBean.setReferencePatterns("EJBContainers", ejbContainerNames);
            kernel.loadGBean(containerIndexObjectName, containerIndexGBean);
            kernel.startGBean(containerIndexObjectName);
            assertRunning(kernel, containerIndexObjectName);

            GBeanMBean connectionProxyFactoryGBean = new GBeanMBean(MockConnectionProxyFactory.GBEAN_INFO);
            JMXReferenceFactory refFactory = new JMXReferenceFactory("geronimo.server", "geronimo");
            ObjectName connectionProxyFactoryObjectName = refFactory.createManagedConnectionFactoryObjectName("DefaultDatasource");
            kernel.loadGBean(connectionProxyFactoryObjectName, connectionProxyFactoryGBean);
            kernel.startGBean(connectionProxyFactoryObjectName);
            assertRunning(kernel, connectionProxyFactoryObjectName);

            ds = (DataSource) kernel.getAttribute(connectionProxyFactoryObjectName, "proxy");
            Connection connection = null;
            Statement statement = null;
            try {
                connection = ds.getConnection();
                statement = connection.createStatement();
                statement.execute("CREATE TABLE SIMPLECMP(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
            } finally {
                JDBCUtil.close(statement);
                JDBCUtil.close(connection);
            }

            // load the configuration
            ObjectName objectName = ObjectName.getInstance("test:configuration=test-ejb-jar");
            kernel.loadGBean(objectName, config);
            config.setAttribute("baseURL", tempDir.toURL());

            // start the configuration
            kernel.startRecursiveGBean(objectName);
            assertRunning(kernel, objectName);

            ObjectName applicationObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEApplication,name=" + j2eeApplicationName + ",J2EEServer=" + j2eeServerName);
            if (!j2eeApplicationName.equals("null")) {
                assertRunning(kernel, applicationObjectName);
            } else {
                Set applications = kernel.getMBeanServer().queryNames(applicationObjectName, null);
                assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
            }

            ObjectName moduleName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=EJBModule,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",name=" + j2eeModuleName);
            assertRunning(kernel, moduleName);

            // STATELESS
            ObjectName statelessBeanName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=StatelessSessionBean,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",J2EEModule=" + j2eeModuleName + ",name=SimpleStatelessSession");
            assertRunning(kernel, statelessBeanName);

            // use reflection to invoke a method on the stateless bean, because we don't have access to the classes here
            Object statelessHome = kernel.getAttribute(statelessBeanName, "ejbHome");
            assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
            Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
            assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
            Object statelessLocalHome = kernel.getAttribute(statelessBeanName, "ejbLocalHome");
            Object statelessLocal = statelessLocalHome.getClass().getMethod("create", null).invoke(statelessLocalHome, null);
            statelessLocal.getClass().getMethod("startTimer", null).invoke(statelessLocal, null);
            Thread.sleep(200L);
            assertEquals(new Integer(1), statelessLocal.getClass().getMethod("getTimeoutCount", null).invoke(statelessLocal, null));

            // STATEFUL
            ObjectName statefulBeanName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=StatefulSessionBean,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",J2EEModule=" + j2eeModuleName + ",name=SimpleStatefulSession");
            assertRunning(kernel, statefulBeanName);

            Object statefulHome = kernel.getAttribute(statefulBeanName, "ejbHome");
            assertTrue("Home is not an instance of EJBHome", statefulHome instanceof EJBHome);
            Object stateful = statefulHome.getClass().getMethod("create", null).invoke(statefulHome, null);
            stateful.getClass().getMethod("setValue", new Class[]{String.class}).invoke(stateful, new Object[]{"SomeValue"});
            assertEquals("SomeValue", stateful.getClass().getMethod("getValue", null).invoke(stateful, null));

            // BMP
            ObjectName bmpBeanName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=EntityBean,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",J2EEModule=" + j2eeModuleName + ",name=SimpleBMPEntity");
            assertRunning(kernel, bmpBeanName);

            Object bmpHome = kernel.getAttribute(bmpBeanName, "ejbHome");
            assertTrue("Home is not an instance of EJBHome", bmpHome instanceof EJBHome);
            Object bmp = bmpHome.getClass().getMethod("create", null).invoke(bmpHome, null);
            bmp.getClass().getMethod("setName", new Class[]{String.class}).invoke(bmp, new Object[]{"MyNameValue"});
            assertEquals("MyNameValue", bmp.getClass().getMethod("getName", null).invoke(bmp, null));

            // CMP
            ObjectName cmpBeanName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=EntityBean,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",J2EEModule=" + j2eeModuleName + ",name=SimpleCMPEntity");
            assertRunning(kernel, cmpBeanName);

            Object cmpHome = kernel.getAttribute(cmpBeanName, "ejbHome");
            assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
            Object cmp = cmpHome.getClass().getMethod("create", new Class[]{Integer.class}).invoke(cmpHome, new Object[]{new Integer(42)});

            cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFistName"});
            assertEquals("MyFistName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));

            //mdb
            ObjectName mdbBeanName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=MessageDrivenBean,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",J2EEModule=" + j2eeModuleName + ",name=SimpleMessageDriven");
            assertRunning(kernel, mdbBeanName);


            kernel.stopGBean(objectName);
            kernel.stopGBean(connectionProxyFactoryObjectName);
        } finally {
            if (ds != null) {
                Connection connection = null;
                Statement statement = null;
                try {
                    connection = ds.getConnection();
                    statement = connection.createStatement();
                    statement.execute("SHUTDOWN");
                } finally {
                    JDBCUtil.close(statement);
                    JDBCUtil.close(connection);
                }
            }

            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, state);
    }

    private GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar, "META-INF/config.ser"));
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config = new GBeanMBean(gbeanInfo);
            Configuration.loadGMBeanState(config, ois);
            return config;
        } finally {
            in.close();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            str = str + ":org.apache.geronimo.naming";
        }
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);

        kernel = DeploymentHelper.setUpKernelWithTransactionManager("EJBConfigBuilderTest");
        DeploymentHelper.setUpTimer(kernel);

        GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");
        ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ServerInfo");
        kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
        kernel.startGBean(serverInfoObjectName);
        assertRunning(kernel, serverInfoObjectName);

        GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
        j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
        ObjectName j2eeServerObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName);
        kernel.loadGBean(j2eeServerObjectName, j2eeServerGBean);
        kernel.startGBean(j2eeServerObjectName);
        assertRunning(kernel, j2eeServerObjectName);

        //load mock resource adapter for mdb
        DeploymentHelper.setUpResourceAdapter(kernel);

    }

    protected void tearDown() throws Exception {
        DeploymentHelper.tearDownAdapter(kernel);
        kernel.shutdown();
    }
}
