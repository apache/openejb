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
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.transaction.TransactionManagerProxy;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;

import junit.framework.TestCase;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.ContainerIndex;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBConfigBuilderTest extends TestCase {
    public void testCreateSessionBean() throws Exception {
        EJBConfigBuilder configBuilder = new EJBConfigBuilder(null, null);
        File ejbJarFile = new File("target/test-ejb-jar.jar");
        assertTrue(ejbJarFile.canRead());

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[] {ejbJarFile.toURL()}, oldCl);
        URL ejbJarXml = cl.getResource("META-INF/ejb-jar.xml");
        InputStream in = ejbJarXml.openStream();
        in.close();

        assertNotNull(cl.loadClass("org.openejb.test.simple.slsb.SimpleStatelessSessionEJB"));

        EjbJarDocument doc = (EjbJarDocument) XmlBeansUtil.getXmlObject(ejbJarXml, EjbJarDocument.type);
        EjbJarType ejbJar = doc.getEjbJar();

        SessionBeanType[] sessionBeans = ejbJar.getEnterpriseBeans().getSessionArray();

        SessionBeanType sessionBean = sessionBeans[0];
        OpenejbSessionBeanType openejbSessionBean = null;

        TransactionPolicyHelper transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());

        try {
            Thread.currentThread().setContextClassLoader(cl);
            configBuilder.createSessionBean("containerId", sessionBean, openejbSessionBean, new HashMap(), transactionPolicyHelper, cl);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void testBuildConfiguration() throws Exception {
        EJBConfigBuilder configBuilder = new EJBConfigBuilder(null, null);
        File ejbJarFile = new File("target/test-ejb-jar.jar");

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[] {ejbJarFile.toURL()}, oldCl);
        URL ejbJarXml = cl.getResource("META-INF/openejb-jar.xml");

        OpenejbOpenejbJarDocument plan = (OpenejbOpenejbJarDocument) XmlBeansUtil.getXmlObject(ejbJarXml, OpenejbOpenejbJarDocument.type);

        File carFile = File.createTempFile("OpenEJBTest", ".car");
        Kernel kernel  = null;
        try {
            Thread.currentThread().setContextClassLoader(cl);
            configBuilder.buildConfiguration(carFile, ejbJarFile, plan);

            File tempdir = new File(System.getProperty("java.io.tmpdir"));
            File unpackedDir = new File(tempdir, "OpenEJBTest-Unpacked");
            LocalConfigStore.unpack(unpackedDir, new FileInputStream(carFile));

            GBeanMBean config = loadConfig(unpackedDir);

            kernel = new Kernel("blah");
            kernel.boot();

            GBeanMBean tmGBean = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
            ObjectName tmObjectName = ObjectName.getInstance("openejb:type=TransactionManager");
            kernel.loadGBean(tmObjectName, tmGBean);
            kernel.startGBean(tmObjectName);
            assertRunning(kernel, tmObjectName);

            GBeanMBean connectionTrackerGBean = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
            ObjectName connectionTrackerObjectName = ObjectName.getInstance("openejb:type=ConnectionTracker");
            kernel.loadGBean(connectionTrackerObjectName, connectionTrackerGBean);
            kernel.startGBean(connectionTrackerObjectName);
            assertRunning(kernel, connectionTrackerObjectName);

            GBeanMBean containerIndexGBean = new GBeanMBean(ContainerIndex.GBEAN_INFO);
            ObjectName containerIndexObjectName = ObjectName.getInstance("openejb:type=ContainerIndex");
            Set ejbContainerNames = new HashSet();
            ejbContainerNames.add(ObjectName.getInstance("openejb:j2eeType=StatelessSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance("openejb:j2eeType=StatefulSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance("openejb:j2eeType=EntityBean,*"));
            containerIndexGBean.setReferencePatterns("EJBContainers", ejbContainerNames);
            kernel.loadGBean(containerIndexObjectName, containerIndexGBean);
            kernel.startGBean(containerIndexObjectName);
            assertRunning(kernel, containerIndexObjectName);

            GBeanMBean connectionProxyFactoryGBean = new GBeanMBean(MockConnectionProxyFactory.GBEAN_INFO);
            ObjectName connectionProxyFactoryObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + "DefaultDatasource");
            kernel.loadGBean(connectionProxyFactoryObjectName, connectionProxyFactoryGBean);
            kernel.startGBean(connectionProxyFactoryObjectName);
            assertRunning(kernel, connectionProxyFactoryObjectName);

            DataSource ds = (DataSource) kernel.getAttribute(connectionProxyFactoryObjectName, "Proxy");
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

            ObjectName objectName = ObjectName.getInstance("test:configuration=test-ejb-jar");
            kernel.loadGBean(objectName, config);
            config.setAttribute("BaseURL", unpackedDir.toURL());

            kernel.startRecursiveGBean(objectName);
            assertRunning(kernel, objectName);

            ObjectName moduleName = ObjectName.getInstance("openejb:j2eeType=EJBModule,J2EEServer=null,J2EEApplication=null,name=org/openejb/itests");
            assertRunning(kernel, moduleName);

            // STATELESS
            ObjectName statelessBeanName = ObjectName.getInstance("openejb:j2eeType=StatelessSessionBean,J2EEServer=null,J2EEApplication=null,J2EEModule=org/openejb/itests,name=SimpleStatelessSession");
            assertRunning(kernel, statelessBeanName);

            // use reflection to invoke a method on the stateless bean, becuase we don't have access to the classes here
            Object statelessHome = kernel.getAttribute(statelessBeanName, "EJBHome");
            assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
            Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
            assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[] {String.class}).invoke(stateless, new Object[] {"TestResult"}));


            // STATEFUL
            ObjectName statefulBeanName = ObjectName.getInstance("openejb:j2eeType=StatefulSessionBean,J2EEServer=null,J2EEApplication=null,J2EEModule=org/openejb/itests,name=SimpleStatefulSession");
            assertRunning(kernel, statefulBeanName);

            Object statefulHome = kernel.getAttribute(statefulBeanName, "EJBHome");
            assertTrue("Home is not an instance of EJBHome", statefulHome instanceof EJBHome);
            Object stateful = statefulHome.getClass().getMethod("create", null).invoke(statefulHome, null);
            stateful.getClass().getMethod("setValue", new Class[] {String.class}).invoke(stateful, new Object[] {"SomeValue"});
            assertEquals("SomeValue", stateful.getClass().getMethod("getValue", null).invoke(stateful, null));

            // BMP
            ObjectName bmpBeanName = ObjectName.getInstance("openejb:j2eeType=EntityBean,J2EEServer=null,J2EEApplication=null,J2EEModule=org/openejb/itests,name=SimpleBMPEntity");
            assertRunning(kernel, bmpBeanName);

            Object bmpHome = kernel.getAttribute(bmpBeanName, "EJBHome");
            assertTrue("Home is not an instance of EJBHome", bmpHome instanceof EJBHome);
            Object bmp = bmpHome.getClass().getMethod("create", null).invoke(bmpHome, null);
            bmp.getClass().getMethod("setName", new Class[] {String.class}).invoke(bmp, new Object[] {"MyNameValue"});
            assertEquals("MyNameValue", bmp.getClass().getMethod("getName", null).invoke(bmp, null));

            // CMP
            ObjectName cmpBeanName = ObjectName.getInstance("openejb:j2eeType=EntityBean,J2EEServer=null,J2EEApplication=null,J2EEModule=org/openejb/itests,name=SimpleCMPEntity");
            assertRunning(kernel, cmpBeanName);

            Object cmpHome = kernel.getAttribute(cmpBeanName, "EJBHome");
            assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
            Object cmp = cmpHome.getClass().getMethod("create", new Class[] {Integer.class}).invoke(cmpHome, new Object[] {new Integer(42)});

// JNB: disabled due to problem with fault handler
//            cmp.getClass().getMethod("setFirstName", new Class[] {String.class}).invoke(cmp, new Object[] {"MyFistName"});
//            assertEquals("MyFistName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));

            kernel.stopGBean(objectName);
            kernel.stopGBean(connectionProxyFactoryObjectName);
            kernel.stopGBean(tmObjectName);
        } finally {
            if (kernel != null) {
                kernel.shutdown();
            }
            carFile.delete();
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        int state = ((Integer)kernel.getAttribute(objectName, "state")).intValue();
        assertEquals(State.RUNNING_INDEX, state);
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
        if (str == null)
            str = ":org.apache.geronimo.naming";
        else
            str = str + ":org.apache.geronimo.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }
}
