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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.servlet.*;
import java.io.IOException;

@RunWith(Arquillian.class)
public class ServletFilterEjbLocalInjectionTest extends TestSetup {

    @Test
    public void localEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testLocalEjb=true";
        validateTest(expectedOutput);
    }

    @Test
    public void localBeanEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testLocalBean=true";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ServletFilterEjbLocalInjectionTest().createDeployment(PojoServletFilter.class,
                CompanyLocal.class, Company.class, DefaultCompany.class, SuperMarket.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PojoServletFilter.class, "/" + getTestContextName());
    }

    public static class PojoServletFilter implements Filter {

        @EJB
        private CompanyLocal localCompany;

        @EJB
        private SuperMarket market;

        public void init(FilterConfig config) {
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void testLocalBean() {
            Assert.assertNotNull(market);
            market.shop("test");
        }

        public void testLocalEjb() {
            Assert.assertNotNull(localCompany);
            localCompany.employ("test");
        }


    }

    public static interface Company {
        public String employ(String employeeName);
    }

    @Local
    public static interface CompanyLocal extends Company {
    }

    @Stateless
    public static class DefaultCompany implements CompanyLocal {

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

}



