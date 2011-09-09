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
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletPersistenceInjectionTest {

    public static final String TEST_NAME = ServletPersistenceInjectionTest.class.getSimpleName();

    @Test
    public void transactionInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testUserTransaction=true";
        validateTest(expectedOutput);
    }

    @Test
    public void persistentContextInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testEntityManager=true";
        validateTest(expectedOutput);
    }

    @Test
    public void persistenceUnitInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testEntityManagerFactory=true";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(PersistenceServlet.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PersistenceServlet.class)
                .addClass(Address.class)
                .addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"))
                .setWebXML(new StringAsset(descriptor.exportAsString()));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    public static class PersistenceServlet extends HttpServlet {

        @Resource
        private UserTransaction transaction;

        @PersistenceUnit
        private EntityManagerFactory entityMgrFactory;

        @PersistenceContext
        private EntityManager entityManager;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final Class<? extends PersistenceServlet> clazz = this.getClass();
            final Method[] methods = clazz.getMethods();

            resp.setContentType("text/plain");
            final PrintWriter writer = resp.getWriter();

            for (Method method : methods) {
                if (method.getName().startsWith("test")) {

                    writer.print(method.getName());

                    writer.print("=");

                    try {
                        method.invoke(this);
                        writer.println("true");
                    } catch (Throwable e) {
                        writer.println("false");
                    }
                }
            }
        }

        public void testEntityManagerFactory() {
            Assert.assertNotNull(entityMgrFactory);

            Address a = new Address();
            EntityManager em = entityMgrFactory.createEntityManager();
            em.contains(a);
        }

        public void testEntityManager() {
            Assert.assertNotNull(entityManager);
            Address a = new Address();
            entityManager.contains(a);
        }

        public void testUserTransaction() throws Exception{
            Assert.assertNotNull(transaction);
            transaction.begin();
            transaction.commit();
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



