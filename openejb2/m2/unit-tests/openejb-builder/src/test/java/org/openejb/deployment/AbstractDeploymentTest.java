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
package org.openejb.deployment;

import java.util.Set;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import javax.ejb.EJBHome;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.openejb.proxy.EJBProxyReference;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractDeploymentTest extends TestCase implements DeploymentTestContants {
    private final ObjectName STATELESS_BEAN_NAME = JMXUtil.getObjectName(DOMAIN_NAME + ":j2eeType=StatelessSessionBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=SimpleStatelessSession");

    public abstract Kernel getKernel();
    public abstract ClassLoader getApplicationClassLoader();
    public abstract String getJ2eeApplicationName();
    public abstract String getJ2eeModuleName();

    public void testEJBModuleObject() throws Exception {
        ObjectName moduleName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EJBModule,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",name=" + getJ2eeModuleName());
        assertRunning(getKernel(), moduleName);
    }

    public void testApplicationObject() throws Exception {
        ObjectName applicationObjectName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=J2EEApplication,name=" + getJ2eeApplicationName() + ",J2EEServer=" + SERVER_NAME);
        if (!getJ2eeApplicationName().equals("null")) {
            assertRunning(getKernel(), applicationObjectName);
        } else {
            Set applications = getKernel().listGBeans(applicationObjectName);
            assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
        }
    }

    public void testStatelessContainer() throws Exception {
        assertRunning(getKernel(), STATELESS_BEAN_NAME);

        // use reflection to invoke a method on the stateless bean, because we don't have access to the classes here
        Object statelessHome = getKernel().getAttribute(STATELESS_BEAN_NAME, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
        Object statelessLocalHome = getKernel().getAttribute(STATELESS_BEAN_NAME, "ejbLocalHome");
        Object statelessLocal = statelessLocalHome.getClass().getMethod("create", null).invoke(statelessLocalHome, null);
        statelessLocal.getClass().getMethod("startTimer", null).invoke(statelessLocal, null);
        Thread.sleep(200L);
        assertEquals(new Integer(1), statelessLocal.getClass().getMethod("getTimeoutCount", null).invoke(statelessLocal, null));
    }

    public void testInClassLoaderInvoke() throws Exception {
        Object statelessHome;
        Object stateless;
        EJBProxyReference proxyReference = EJBProxyReference.createRemote(STATELESS_BEAN_NAME.getCanonicalName(),
                        true,
                "org.openejb.test.simple.slsb.SimpleStatelessSessionHome", "org.openejb.test.simple.slsb.SimpleStatelessSession");
        proxyReference.setKernel(getKernel());
        proxyReference.setClassLoader(getApplicationClassLoader());
        statelessHome = proxyReference.getContent();
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
    }

    public void testCrossClassLoaderInvoke() throws Exception {
        Object statelessHome;
        Object stateless;
        EJBProxyReference proxyReference = EJBProxyReference.createRemote(STATELESS_BEAN_NAME.getCanonicalName(),
                        true,
                "org.openejb.test.simple.slsb.SimpleStatelessSessionHome", "org.openejb.test.simple.slsb.SimpleStatelessSession");
        proxyReference.setKernel(getKernel());
        proxyReference.setClassLoader(new URLClassLoader(new URL[] {new File(System.getProperty("basedir", System.getProperty("user.dir")), "target/test-ejb-jar.jar").toURL()}, getClass().getClassLoader()));
        statelessHome = proxyReference.getContent();
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
    }

    public void testStatefulContainer() throws Exception {
        ObjectName statefulBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=StatefulSessionBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=SimpleStatefulSession");
        assertRunning(getKernel(), statefulBeanName);

        Object statefulHome = getKernel().getAttribute(statefulBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", statefulHome instanceof EJBHome);
        Object stateful = statefulHome.getClass().getMethod("create", null).invoke(statefulHome, null);
        stateful.getClass().getMethod("setValue", new Class[]{String.class}).invoke(stateful, new Object[]{"SomeValue"});
        assertEquals("SomeValue", stateful.getClass().getMethod("getValue", null).invoke(stateful, null));
    }

    public void testBMPContainer() throws Exception {
        ObjectName bmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=SimpleBMPEntity");
        assertRunning(getKernel(), bmpBeanName);

        Object bmpHome = getKernel().getAttribute(bmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", bmpHome instanceof EJBHome);
        Object bmp = bmpHome.getClass().getMethod("create", null).invoke(bmpHome, null);
        bmp.getClass().getMethod("setName", new Class[]{String.class}).invoke(bmp, new Object[]{"MyNameValue"});
        assertEquals("MyNameValue", bmp.getClass().getMethod("getName", null).invoke(bmp, null));
    }

    public void testCMPContainer() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=SimpleCMPEntity");
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{Integer.class}).invoke(cmpHome, new Object[]{new Integer(42)});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFistName"});
        assertEquals("MyFistName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void testPKGenCustomDBName() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=PKGenCMPEntity");
        assertRunning(getKernel(), cmpBeanName);
        assertRunning(getKernel(), new ObjectName("geronimo.server:name=CMPPKGenerator"));

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void testPKGenCustomDBParts() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=PKGenCMPEntity2");
        assertRunning(getKernel(), cmpBeanName);
        assertRunning(getKernel(), new ObjectName("geronimo.server:name=CMPPKGenerator2"));

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void testPKGenSequence() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=PKGenCMPEntity3");
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }
/* Axion JDBC driver doesn't supoort returning generated keys -- try this again with a different DB
    public void testPKGenAutoIncrement() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=PKGenCMPEntity4");
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }
*/
    public void testPKGenSQL() throws Exception {
        ObjectName cmpBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=PKGenCMPEntity5");
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void testMDBContainer() throws Exception {
        ObjectName mdbBeanName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=MessageDrivenBean,J2EEServer=" + SERVER_NAME + ",J2EEApplication=" + getJ2eeApplicationName() + ",EJBModule=" + getJ2eeModuleName() + ",name=SimpleMessageDriven");
        assertRunning(getKernel(), mdbBeanName);
    }

    public static void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, kernel.getGBeanState(objectName));
    }
}
