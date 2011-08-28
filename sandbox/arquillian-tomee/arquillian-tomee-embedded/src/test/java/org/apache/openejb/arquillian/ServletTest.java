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

import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.Node;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.NodeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletTest {

    @Test
    public void ejbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Remote: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Test
    public void localEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Local: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Test
    public void localBeanEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "OpenEJB shops at Apache Marketplace";
        validateTest(expectedOutput);
    }

    @Test
    public void pojoInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "OpenEJB is on the wheel of a 2011 Lexus IS 350";
        validateTest(expectedOutput);
    }

    @Test
    public void transactionInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Transaction injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistentContextInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Transaction manager injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void persistenceUnitInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Transaction manager factory injection successful";
        validateTest(expectedOutput);
    }

    @Test
    public void stringEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "tomee@apache.org";
        validateTest(expectedOutput);
    }

    @Test
    public void integerTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Connection Pool: 20";
        validateTest(expectedOutput);
    }

    @Test
    public void longTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Start Count: 200000";
        validateTest(expectedOutput);
    }

    @Test
    public void shortTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Init Size: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void byteTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Total Quantity: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void booleanTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Enable Email: true";
        validateTest(expectedOutput);
    }

    @Test
    public void charTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Option Default: X";
        validateTest(expectedOutput);
    }

    @Test
    public void classEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "java.lang.String";
        validateTest(expectedOutput);
    }

    @Test
    public void enumEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "DefaultCode: OK";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(ServletToTest.class, "/Test");

        addEnvEntry(descriptor, "returnEmail", "java.lang.String", "tomee@apache.org");
        addEnvEntry(descriptor, "connectionPool", "java.lang.Integer", "20");
        addEnvEntry(descriptor, "startCount", "java.lang.Long", "200000");
        addEnvEntry(descriptor, "initSize", "java.lang.Short", "5");
        addEnvEntry(descriptor, "enableEmail", "java.lang.Boolean", "true");
        addEnvEntry(descriptor, "totalQuantity", "java.lang.Byte", "5");
        addEnvEntry(descriptor, "optionDefault", "java.lang.Character", "X");
        addEnvEntry(descriptor, "auditWriter", "java.lang.Class", "java.lang.String");
        addEnvEntry(descriptor, "defaultCode", "java.lang.Enum", "org.apache.openejb.arquillian.ServletTest$Code.OK");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(ServletToTest.class)
                .addClass(Car.class)
                .addClass(CompanyRemote.class)
                .addClass(CompanyLocal.class)
                .addClass(Company.class)
                .addClass(DefaultCompany.class)
                .addClass(SuperMarket.class)
                .addClass(Address.class)
                .addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"))
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    public static enum Code {
        OK,
        ERROR;
    }

    public static class ServletToTest extends HttpServlet {

        @Inject
        private Car car;

        @EJB
        private CompanyLocal localCompany;

        @EJB
        private CompanyRemote remoteCompany;

        @EJB
        private SuperMarket market;

        @Resource
        private UserTransaction transaction;

        @PersistenceUnit
        private EntityManagerFactory entityMgrFactory;

        @PersistenceContext
        private EntityManager entityManager;

        @Resource(name = "returnEmail")
        private String returnEmail;

        @Resource(name = "connectionPool")
        private Integer connectionPool;

        @Resource(name = "startCount")
        private Long startCount;

        @Resource(name = "initSize")
        private Short initSize;

        @Resource(name = "totalQuantity")
        private Byte totalQuantity;

        @Resource(name = "enableEmail")
        private Boolean enableEmail;

        @Resource(name = "optionDefault")
        private Character optionDefault;

        /* TODO: Enable this resource after functionality is fixed
        @Resource
        */
        private Code defaultCode;

        /* TODO: Enable this resource after functionality is fixed
                @Resource
                @SuppressWarnings("unchecked")
        */
        private Class auditWriter;


        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String name = req.getParameter("name");
            if (StringUtils.isEmpty(name)) {
                name = "OpenEJB";
            }

            if (car != null) {
                resp.getOutputStream().println(car.drive(name));
            }
            if (localCompany != null) {
                resp.getOutputStream().println("Local: " + localCompany.employ(name));
            }
            if (remoteCompany != null) {
                resp.getOutputStream().println("Remote: " + remoteCompany.employ(name));
            }
            if (market != null) {
                resp.getOutputStream().println(market.shop(name));
            }
            if (transaction != null) {
                try {
                    transaction.begin();
                    transaction.commit();
                    resp.getOutputStream().println("Transaction injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityManager != null) {
                Address a = new Address();
                try {
                    entityManager.contains(a);
                    resp.getOutputStream().println("Transaction manager injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (entityMgrFactory != null) {
                Address a = new Address();
                try {
                    EntityManager em = entityMgrFactory.createEntityManager();
                    em.contains(a);
                    resp.getOutputStream().println("Transaction manager factory injection successful");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (connectionPool != null) {
                resp.getOutputStream().println("Connection Pool: " + connectionPool);
            }
            if (startCount != null) {
                resp.getOutputStream().println("Start Count: " + startCount);
            }
            if (initSize != null) {
                resp.getOutputStream().println("Init Size: " + initSize);
            }
            if (totalQuantity != null) {
                resp.getOutputStream().println("Total Quantity: " + totalQuantity);
            }
            if (enableEmail != null) {
                resp.getOutputStream().println("Enable Email: " + enableEmail);
            }
            if (optionDefault != null) {
                resp.getOutputStream().println("Option Default: " + optionDefault);
            }
            if (StringUtils.isNotEmpty(returnEmail) && returnEmail.equals("tomee@apache.org")) {
                resp.getOutputStream().println(returnEmail);
            }
            if (auditWriter != null) {
                resp.getOutputStream().println(auditWriter.getClass().getName());
            }
            if (defaultCode != null) {
                resp.getOutputStream().println("DefaultCode: " + defaultCode);
            }
        }


    }

    public static class Car {
        private final String make = "Lexus", model = "IS 350";
        private final int year = 2011;

        public String drive(String name) {
            return name + " is on the wheel of a " + year + " " + make + " " + model;
        }
    }


    public static interface Company {
        public String employ(String employeeName);
    }

    @Remote
    public static interface CompanyRemote extends Company {
    }

    @Local
    public static interface CompanyLocal extends Company {
    }

    @Stateless
    public static class DefaultCompany implements CompanyRemote, CompanyLocal {

        private final String name = "TomEE Software Inc.";

        public String employ(String employeeName) {
            return employeeName + " is employed at " + name;
        }

    }

    @Stateless
    @LocalBean
    public static class SuperMarket {

        private final String name = "Apache Marketplace";

        public String shop(String employeeName) {
            return employeeName + " shops at " + name;
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

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node rootNode = ((NodeProvider) descriptor).getRootNode();
        Node appNode = rootNode.get("/web-app").iterator().next();
        appNode.create("/env-entry")
                .create("env-entry-name").text(name)
                .parent()
                .create("env-entry-type").text(type)
                .parent()
                .create("env-entry-value").text(value)
/*
                .parent()
                .create("injection-target")
                .create("injection-target-class").text("org.apache.openejb.arquillian.ServletTest$ServletToTest")
                .parent()
                .create("injection-target-name").text(name)
*/
        ;

    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:9080/test/Test").openStream();
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



