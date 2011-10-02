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
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.servlet.*;
import java.io.IOException;

@RunWith(Arquillian.class)
public class ServletFilterEjbRemoteInjectionTest extends TestSetup {

    @Test
    public void ejbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testEjb=true";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ServletFilterEjbRemoteInjectionTest().createDeployment(RemoteServletFilter.class, CompanyRemote.class, DefaultCompany.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(RemoteServletFilter.class, "/" + getTestContextName());
    }

    public static class RemoteServletFilter implements Filter {

        @EJB
        private CompanyRemote remoteCompany;

        public void init(FilterConfig config) {
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void testEjb () {
            Assert.assertNotNull(remoteCompany);
            remoteCompany.employ("test");
        }

    }

    @Remote
    public static interface CompanyRemote {
        public String employ(String employeeName);
    }

    @Stateless
    public static class DefaultCompany implements CompanyRemote {

        private final String name = "TomEE Software Inc.";

        public String employ(String employeeName) {
            return employeeName + " is employed at " + name;
        }

    }

}



