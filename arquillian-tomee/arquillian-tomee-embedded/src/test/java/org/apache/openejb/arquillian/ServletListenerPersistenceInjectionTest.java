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
package org.apache.openejb.arquillian;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.transaction.UserTransaction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletListenerPersistenceInjectionTest {

    public static final String TEST_NAME = ServletListenerPersistenceInjectionTest.class.getSimpleName();

    @Test
    public void transactionInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Transaction injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistentContextInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Transaction manager injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistenceUnitInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Transaction manager factory injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void transactionInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Transaction injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistentContextInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Transaction manager injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistenceUnitInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Transaction manager factory injection successful";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .listener(PersistenceServletContextListener.class)
                .listener(PersistenceServletSessionListener.class)
                .servlet(ServletToCheckListener.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PersistenceServletContextListener.class)
                .addClass(PersistenceServletSessionListener.class)
                .addClass(ServletToCheckListener.class)
                .addClass(Address.class)
                .addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"))
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    public static enum ContextAttributeName {
        KEY_EntityManagerFactory,
        KEY_EntityManager,
        KEY_Transaction,;
    }

    public static class ServletToCheckListener extends HttpServlet {

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final ServletContext ctxt = req.getServletContext();
            for (ContextAttributeName s : ContextAttributeName.values()) {
                resp.getOutputStream().println("Context: " + ctxt.getAttribute(s.name()));
            }

            final HttpSession session = req.getSession();
            for (ContextAttributeName s : ContextAttributeName.values()) {
                resp.getOutputStream().println("Session: " + session.getAttribute(s.name()));
            }
        }
    }

    public static class PersistenceServletContextListener implements ServletContextListener {

        @Resource
        private UserTransaction transaction;

        @PersistenceUnit
        private EntityManagerFactory entityMgrFactory;

        @PersistenceContext
        private EntityManager entityManager;

        public void contextInitialized(ServletContextEvent event) {
            final ServletContext context = event.getServletContext();

            if (transaction != null) {
                try {
                    transaction.begin();
                    transaction.commit();
                    context.setAttribute(ContextAttributeName.KEY_Transaction.name(), "Transaction injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityManager != null) {
                Address a = new Address();
                try {
                    entityManager.contains(a);
                    context.setAttribute(ContextAttributeName.KEY_EntityManager.name(), "Transaction manager injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityMgrFactory != null) {
                Address a = new Address();
                try {
                    EntityManager em = entityMgrFactory.createEntityManager();
                    em.contains(a);
                    context.setAttribute(ContextAttributeName.KEY_EntityManagerFactory.name(), "Transaction manager factory injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }

        public void contextDestroyed(ServletContextEvent event) {
        }

    }

    public static class PersistenceServletSessionListener implements HttpSessionListener {

        @Resource
        private UserTransaction transaction;

        @PersistenceUnit
        private EntityManagerFactory entityMgrFactory;

        @PersistenceContext
        private EntityManager entityManager;

        public void sessionCreated(HttpSessionEvent event) {
            final HttpSession context = event.getSession();

            if (transaction != null) {
                try {
                    transaction.begin();
                    transaction.commit();
                    context.setAttribute(ContextAttributeName.KEY_Transaction.name(), "Transaction injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityManager != null) {
                Address a = new Address();
                try {
                    entityManager.contains(a);
                    context.setAttribute(ContextAttributeName.KEY_EntityManager.name(), "Transaction manager injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityMgrFactory != null) {
                Address a = new Address();
                try {
                    EntityManager em = entityMgrFactory.createEntityManager();
                    em.contains(a);
                    context.setAttribute(ContextAttributeName.KEY_EntityManagerFactory.name(), "Transaction manager factory injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }

        public void sessionDestroyed(HttpSessionEvent event) {
        }

    }

    @Entity
    public static class Address {
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        private String street = "123 Lakeview St.", city = "Paradise", state = "ZZ", zip = "00000";

        public String toString() {
            return "Street: " + street + ", City: " + city + ", State: " + state + ", Zip: " + zip;
        }
    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:9080/" + TEST_NAME + "/" + TEST_NAME).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead = -1;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput, output.contains(expectedOutput));
    }

}



