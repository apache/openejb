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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletResourceEnvEntryInjectionTest extends TestSetup  {

    public static final String TEST_NAME = ServletResourceEnvEntryInjectionTest.class.getSimpleName();

    @Test
    public void testRed() throws Exception {
        validateTest("red", "true");
    }

    @Test
    public void testBlue() throws Exception {
        validateTest("blue", "true");
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ServletResourceEnvEntryInjectionTest().createDeployment(ServletResourceEnvEntryInjectionTest.class, Blue.class, Red.class, Green.class, Orange.class, Purple.class);
    }

    @WebServlet("/blue")
    public static class Blue extends HttpServlet {

        @Resource(name = "java:comp/Validator")
        private Validator validator;

        @Resource(name = "java:comp/ValidatorFactory")
        private ValidatorFactory validatorFactory;

        @Resource(name = "java:comp/TransactionManager")
        private TransactionManager transactionManager;

        @Resource(name = "java:comp/TransactionSynchronizationRegistry")
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource(name = "java:comp/UserTransaction")
        private UserTransaction userTransaction;

        @Resource(name = "java:comp/BeanManager")
        private BeanManager beanManager;

        @Resource(name = "java:app/AppName")
        private String app;

        @Resource(name = "java:module/ModuleName")
        private String module;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void test() throws Exception {

            final Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Assert.assertNotNull(field.getName(), field.get(this));
            }

            final String name = ServletResourceEnvEntryInjectionTest.class.getSimpleName();
            Assert.assertEquals("app", name, app);
            Assert.assertEquals("module", name, module);
        }

    }


    @WebServlet("/red")
    public static class Red  extends HttpServlet {

        @Resource
        private Validator validator;

        @Resource
        private ValidatorFactory validatorFactory;

        @Resource
        private TransactionManager transactionManager;

        @Resource
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource
        private UserTransaction userTransaction;

        @Resource
        private BeanManager beanManager;

        @Resource
        private Purple purple;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void test() throws Exception {

            final Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Assert.assertNotNull(field.getName(), field.get(this));
            }

            purple.test();
        }
    }

    @Singleton
    public static class Green {

        public void foo() {}
    }

    @WebServlet("/orange")
    public static class Orange extends HttpServlet {

        @Resource(name = "java:app/some/longer/path/MyValidatorFactory")
        private ValidatorFactory validatorFactory;

        @Resource(name = "java:app/some/longer/path/MyTransactionManager")
        private TransactionManager transactionManager;

        @Resource(name = "java:app/some/longer/path/MyTransactionSynchronizationRegistry")
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource(name = "java:app/some/longer/path/MyUserTransaction")
        private UserTransaction userTransaction;

        @Resource(name = "java:app/some/longer/path/MyBeanManager")
        private BeanManager beanManager;

        @Resource(name = "java:app/AppName")
        private String app;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            run(req, resp, this);
        }

        public void test() throws Exception {

            assertFields(this);

            Assert.assertEquals("app", "BuiltInEnvironmentEntriesTest", app);
        }

    }

    @ManagedBean
    public static class Purple {

        @Resource
        private Validator validator;

        @Resource
        private ValidatorFactory validatorFactory;

        @Resource
        private TransactionManager transactionManager;

        @Resource
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource
        private UserTransaction userTransaction;

        @Resource
        private BeanManager beanManager;

        public void test() throws IllegalAccessException {
            assertFields(this);
        }
    }
}



