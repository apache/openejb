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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;

@RunWith(Arquillian.class)
public class ServletFilterCdiInjectionTest extends TestSetup {

    public static final String TEST_NAME = ServletFilterCdiInjectionTest.class.getSimpleName();

    @Test
    public void pojoInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testCdi=true";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ServletFilterCdiInjectionTest().createDeployment(PojoServletFilter.class, Car.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PojoServletFilter.class, "/" + getTestContextName());
    }

    public static class PojoServletFilter implements Filter {

        @Inject
        private Car car;

        public void init(FilterConfig config) {
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void testCdi() {
            Assert.assertNotNull(car);
            car.drive("test");
        }
    }

    public static class Car {
        private final String make = "Lexus", model = "IS 350";
        private final int year = 2011;

        public String drive(String name) {
            return name + " is on the wheel of a " + year + " " + make + " " + model;
        }
    }


}



