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
import org.jboss.shrinkwrap.descriptor.api.Node;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.NodeProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@RunWith(Arquillian.class)
public class ModuleNameInjectionTest extends TestSetup {

    public static final String TEST_NAME = ModuleNameInjectionTest.class.getSimpleName();

    @Test
    public void testCdi() throws Exception {
        validateTest("testCdi=true");
    }

    @Test
    public void testEjb() throws Exception {
        validateTest("testEjb=true");
    }

    @Test
    public void testFilter() throws Exception {
        validateTest("testFilter=true");
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ModuleNameInjectionTest().createDeployment(PojoServletFilter.class, Orange.class, Green.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PojoServletFilter.class, "/" + getTestContextName());
    }

    public static class PojoServletFilter implements Filter, Environment {

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

        @Override
        public String getReturnEmail() {
            return returnEmail;
        }

        @Override
        public Integer getConnectionPool() {
            return connectionPool;
        }

        @Override
        public Long getStartCount() {
            return startCount;
        }

        @Override
        public Short getInitSize() {
            return initSize;
        }

        @Override
        public Byte getTotalQuantity() {
            return totalQuantity;
        }

        @Override
        public Boolean getEnableEmail() {
            return enableEmail;
        }

        @Override
        public Character getOptionDefault() {
            return optionDefault;
        }

        @Inject
        private Green green;

        @EJB
        private Orange orange;

        public void init(FilterConfig config) {
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            run(req, resp, this);
        }


        public void testCdi() {
            assertEnvironment(green);
        }

        public void testEjb() {
            assertEnvironment(orange);
        }

        public void testFilter() {
            assertEnvironment(this);
        }

        public void assertEnvironment(Environment actual) {
            Assert.assertNotNull("component", actual);

            Environment expected = new Green("tomee@apache.org", 20, 200000l, (short) 6, (byte) 5, true, 'X');

            Assert.assertEquals("ReturnEmail", expected.getReturnEmail(), actual.getReturnEmail());
            Assert.assertEquals("ConnectionPool", expected.getConnectionPool(), actual.getConnectionPool());
            Assert.assertEquals("StartCount", expected.getStartCount(), actual.getStartCount());
            Assert.assertEquals("InitSize", expected.getInitSize(), actual.getInitSize());
            Assert.assertEquals("TotalQuantity", expected.getTotalQuantity(), actual.getTotalQuantity());
            Assert.assertEquals("EnableEmail", expected.getEnableEmail(), actual.getEnableEmail());
            Assert.assertEquals("OptionDefault", expected.getOptionDefault(), actual.getOptionDefault());
        }
    }

    public static class Green implements Environment {

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

        @Inject
        public Green() {
        }

        public Green(String returnEmail, Integer connectionPool, Long startCount, Short initSize, Byte totalQuantity, Boolean enableEmail, Character optionDefault) {
            this.returnEmail = returnEmail;
            this.connectionPool = connectionPool;
            this.startCount = startCount;
            this.initSize = initSize;
            this.totalQuantity = totalQuantity;
            this.enableEmail = enableEmail;
            this.optionDefault = optionDefault;
        }

        @Override
        public String getReturnEmail() {
            return returnEmail;
        }

        @Override
        public Integer getConnectionPool() {
            return connectionPool;
        }

        @Override
        public Long getStartCount() {
            return startCount;
        }

        @Override
        public Short getInitSize() {
            return initSize;
        }

        @Override
        public Byte getTotalQuantity() {
            return totalQuantity;
        }

        @Override
        public Boolean getEnableEmail() {
            return enableEmail;
        }

        @Override
        public Character getOptionDefault() {
            return optionDefault;
        }
    }

    @Singleton
    @LocalBean
    public static class Orange implements Environment {

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

        @Override
        public String getReturnEmail() {
            return returnEmail;
        }

        @Override
        public Integer getConnectionPool() {
            return connectionPool;
        }

        @Override
        public Long getStartCount() {
            return startCount;
        }

        @Override
        public Short getInitSize() {
            return initSize;
        }

        @Override
        public Byte getTotalQuantity() {
            return totalQuantity;
        }

        @Override
        public Boolean getEnableEmail() {
            return enableEmail;
        }

        @Override
        public Character getOptionDefault() {
            return optionDefault;
        }
    }


    /**
     * @version $Rev$ $Date$
     */
    public static interface Environment {
        String getReturnEmail();

        Integer getConnectionPool();

        Long getStartCount();

        Short getInitSize();

        Byte getTotalQuantity();

        Boolean getEnableEmail();

        Character getOptionDefault();
    }
}



