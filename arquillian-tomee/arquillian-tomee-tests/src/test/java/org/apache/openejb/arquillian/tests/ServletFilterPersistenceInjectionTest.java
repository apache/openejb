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
package org.apache.openejb.arquillian.tests;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.servlet.*;
import javax.transaction.UserTransaction;
import java.io.IOException;

@RunWith(Arquillian.class)
public class ServletFilterPersistenceInjectionTest extends TestSetup {

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
    public static WebArchive getArchive() {
        return new ServletFilterPersistenceInjectionTest().createDeployment(PersistenceServletFilter.class, Address.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PersistenceServletFilter.class, "/" + getTestContextName());
    }

    public void decorateArchive(WebArchive archive) {
        archive.addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"));
    }

    public static class PersistenceServletFilter implements Filter {

        @Resource
        private UserTransaction transaction;

        @PersistenceUnit
        private EntityManagerFactory entityMgrFactory;

        @PersistenceContext
        private EntityManager entityManager;

        public void init(FilterConfig config) {
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            run(req, resp, this);
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

}



