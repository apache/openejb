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
package org.apache.openejb.deployment;

import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import javax.ejb.EJBHome;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.openejb.proxy.EJBProxyReference;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractDeploymentTest extends TestCase implements DeploymentTestContants {
    public DeploymentHelper deploymentHelper = new DeploymentHelper();
    public abstract Kernel getKernel();
    public abstract ClassLoader getApplicationClassLoader();
    public abstract String getJ2eeApplicationName();
    public abstract String getJ2eeModuleName();

    public void XXtestEJBModuleObject() throws Exception {
        Map properties = new HashMap();
        properties.put("J2EEApplication", getJ2eeApplicationName());
        properties.put("j2eeType", "EJBModule");
        properties.put("name", getJ2eeModuleName());
        AbstractNameQuery query = new AbstractNameQuery(null, properties);
        assertRunning(getKernel(), query);
    }

    public void XXtestApplicationObject() throws Exception {
        Map properties = new HashMap();
        properties.put("j2eeType", "J2EEApplication");
        properties.put("name", getJ2eeApplicationName());
        AbstractNameQuery query = new AbstractNameQuery(null, properties);

        if (!getJ2eeApplicationName().equals("null")) {
            assertRunning(getKernel(), query);
        } else {
            Set applications = getKernel().listGBeans(query);
            assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
        }
    }

    public void XXtestStatelessContainer() throws Exception {
        AbstractNameQuery statelessBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleStatelessSession", "StatelessSessionBean", getJ2eeModuleName());
        AbstractName statelessBeanName = findSingle(getKernel(), statelessBeanQuery);
        assertRunning(getKernel(), statelessBeanName);

        // use reflection to invoke a method on the stateless bean, because we don't have access to the classes here
        Object statelessHome = getKernel().getAttribute(statelessBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
        Object statelessLocalHome = getKernel().getAttribute(statelessBeanName, "ejbLocalHome");
        Object statelessLocal = statelessLocalHome.getClass().getMethod("create", null).invoke(statelessLocalHome, null);
        statelessLocal.getClass().getMethod("startTimer", null).invoke(statelessLocal, null);
        Thread.sleep(200L);
        assertEquals(new Integer(1), statelessLocal.getClass().getMethod("getTimeoutCount", null).invoke(statelessLocal, null));
    }

    public void XXtestInClassLoaderInvoke() throws Exception {
        AbstractNameQuery statelessBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleStatelessSession", "StatelessSessionBean", getJ2eeModuleName());
        AbstractName statelessBeanName = findSingle(getKernel(), statelessBeanQuery);

        Object statelessHome;
        Object stateless;
        EJBProxyReference proxyReference = EJBProxyReference.createRemote(deploymentHelper.BOOTSTRAP_ID,
                new AbstractNameQuery(statelessBeanName),
                true,
                "org.apache.openejb.test.simple.slsb.SimpleStatelessSessionHome",
                "org.apache.openejb.test.simple.slsb.SimpleStatelessSession");
        proxyReference.setKernel(getKernel());
        proxyReference.setClassLoader(getApplicationClassLoader());
        statelessHome = proxyReference.getContent();
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
    }

    public void XXtestCrossClassLoaderInvoke() throws Exception {
        AbstractNameQuery statelessBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleStatelessSession", "StatelessSessionBean", getJ2eeModuleName());
        AbstractName statelessBeanName = findSingle(getKernel(), statelessBeanQuery);

        Object statelessHome;
        Object stateless;
        EJBProxyReference proxyReference = EJBProxyReference.createRemote(deploymentHelper.BOOTSTRAP_ID,
                new AbstractNameQuery(statelessBeanName),
                true,
                "org.apache.openejb.test.simple.slsb.SimpleStatelessSessionHome",
                "org.apache.openejb.test.simple.slsb.SimpleStatelessSession");
        proxyReference.setKernel(getKernel());
        proxyReference.setClassLoader(new URLClassLoader(new URL[] {new File("target/test-ejb-jar.jar").toURL()}, getClass().getClassLoader()));
        statelessHome = proxyReference.getContent();
        assertTrue("Home is not an instance of EJBHome", statelessHome instanceof EJBHome);
        stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
        assertEquals("TestResult", stateless.getClass().getMethod("echo", new Class[]{String.class}).invoke(stateless, new Object[]{"TestResult"}));
    }

    public void testStatefulContainer() throws Exception {
        AbstractNameQuery statefulBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleStatefulSession", "StatefulSessionBean", getJ2eeModuleName());
        AbstractName statefulBeanName = findSingle(getKernel(), statefulBeanQuery);
        assertRunning(getKernel(), statefulBeanName);

        Object statefulHome = getKernel().getAttribute(statefulBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", statefulHome instanceof EJBHome);
        Object stateful = statefulHome.getClass().getMethod("create", null).invoke(statefulHome, null);
        stateful.getClass().getMethod("setValue", new Class[]{String.class}).invoke(stateful, new Object[]{"SomeValue"});
        assertEquals("SomeValue", stateful.getClass().getMethod("getValue", null).invoke(stateful, null));
    }

    public void XXtestBMPContainer() throws Exception {
        AbstractNameQuery bmpBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleBMPEntity", "EntityBean", getJ2eeModuleName());
        AbstractName bmpBeanName = findSingle(getKernel(), bmpBeanQuery);
        assertRunning(getKernel(), bmpBeanName);

        Object bmpHome = getKernel().getAttribute(bmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", bmpHome instanceof EJBHome);
        Object bmp = bmpHome.getClass().getMethod("create", null).invoke(bmpHome, null);
        bmp.getClass().getMethod("setName", new Class[]{String.class}).invoke(bmp, new Object[]{"MyNameValue"});
        assertEquals("MyNameValue", bmp.getClass().getMethod("getName", null).invoke(bmp, null));
    }

    public void XXtestCMPContainer() throws Exception {
        AbstractNameQuery cmpBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleCMPEntity", "EntityBean", getJ2eeModuleName());
        AbstractName cmpBeanName = findSingle(getKernel(), cmpBeanQuery);
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{Integer.class}).invoke(cmpHome, new Object[]{new Integer(42)});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFistName"});
        assertEquals("MyFistName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void XXtestPKGenCustomDBName() throws Exception {
        AbstractNameQuery cmpBeanQuery = DeploymentHelper.createEjbNameQuery("PKGenCMPEntity", "EntityBean", getJ2eeModuleName());
        AbstractName cmpBeanName = findSingle(getKernel(), cmpBeanQuery);
        assertRunning(getKernel(), cmpBeanName);
        assertAllRunning(getKernel(), new AbstractNameQuery(null, Collections.singletonMap("name", "CMPPKGenerator")));

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void XXtestPKGenCustomDBParts() throws Exception {
        AbstractNameQuery cmpBeanQuery = DeploymentHelper.createEjbNameQuery("PKGenCMPEntity2", "EntityBean", getJ2eeModuleName());
        AbstractName cmpBeanName = findSingle(getKernel(), cmpBeanQuery);
        assertRunning(getKernel(), cmpBeanName);
        assertAllRunning(getKernel(), new AbstractNameQuery(null, Collections.singletonMap("name", "CMPPKGenerator2")));

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void XXtestPKGenSequence() throws Exception {
        AbstractNameQuery cmpBeanQuery = DeploymentHelper.createEjbNameQuery("PKGenCMPEntity3", "EntityBean", getJ2eeModuleName());
        AbstractName cmpBeanName = findSingle(getKernel(), cmpBeanQuery);
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
    public void XXtestPKGenSQL() throws Exception {
        AbstractNameQuery cmpBeanQuery = DeploymentHelper.createEjbNameQuery("PKGenCMPEntity5", "EntityBean", getJ2eeModuleName());
        AbstractName cmpBeanName = findSingle(getKernel(), cmpBeanQuery);
        assertRunning(getKernel(), cmpBeanName);

        Object cmpHome = getKernel().getAttribute(cmpBeanName, "ejbHome");
        assertTrue("Home is not an instance of EJBHome", cmpHome instanceof EJBHome);
        Object cmp = cmpHome.getClass().getMethod("create", new Class[]{}).invoke(cmpHome, new Object[]{});

        cmp.getClass().getMethod("setFirstName", new Class[]{String.class}).invoke(cmp, new Object[]{"MyFirstName"});
        assertEquals("MyFirstName", cmp.getClass().getMethod("getFirstName", null).invoke(cmp, null));
    }

    public void XXtestMDBContainer() throws Exception {
        AbstractNameQuery mdbBeanQuery = DeploymentHelper.createEjbNameQuery("SimpleMessageDriven", "MessageDrivenBean", getJ2eeModuleName());
        AbstractName mdbBeanName = findSingle(getKernel(), mdbBeanQuery);
        assertRunning(getKernel(), mdbBeanName);
    }

    public static void assertRunning(Kernel kernel, AbstractNameQuery query) throws Exception {
        AbstractName abstractName = findSingle(kernel, query);
        assertEquals("should be running: " + abstractName, State.RUNNING_INDEX, kernel.getGBeanState(abstractName));
    }
    public static void assertAllRunning(Kernel kernel, AbstractNameQuery query) throws Exception {
        Set names = kernel.listGBeans(query);
        if (names.isEmpty()) {
            fail("No matches for " + query);
        }
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            assertEquals("should be running: " + abstractName, State.RUNNING_INDEX, kernel.getGBeanState(abstractName));
        }
    }

    private static AbstractName findSingle(Kernel kernel, AbstractNameQuery query) {
        Set names = kernel.listGBeans(query);
        assertEquals("should only match one name but matched " + names, 1, names.size());
        return (AbstractName) names.iterator().next();
    }

    public static void assertRunning(Kernel kernel, AbstractName abstractName) throws Exception {
        assertEquals("should be running: " + abstractName, State.RUNNING_INDEX, kernel.getGBeanState(abstractName));
    }
}
