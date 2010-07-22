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
package org.apache.openejb.cdi;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.cdi.CdiAppScannerService;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Before;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

@SuppressWarnings("deprecation")
public class CdiDecoratorTest extends TestCase {

    private InitialContext ctx;

    @Before
    public void setUp() throws Exception {

        CdiAppScannerService.BEANS_XML_LOCATION = "org/apache/openejb/cdi/decorator/META-INF/beans.xml";
        CdiAppScannerService.APPEND_PACKAGE_NAME = "org.apache.openejb.cdi.decorator";
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(HelloStateless.class));
        ejbJar.addEnterpriseBean(new StatelessBean(LocalHello.class));

        Beans beans = new Beans();
        beans.addInterceptor(HelloLocalInterceptor.class);
        beans.addDecorator(HelloDecorator.class);
        beans.addManagedClass(HelloCdiBean.class);

        EjbModule module = new EjbModule(ejbJar);
        module.setBeans(beans);

        assembler.createApplication(config.configureApplication(module));

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        ctx = new InitialContext(properties);
    }

    public void testSimple() {
        try {
            Hello hello = (Hello) ctx.lookup("HelloStatelessLocal");
            hello.hello();

            assertTrue(HelloCdiBean.RUN);
            assertTrue(LocalHello.RUN);
            assertTrue(HelloStateless.RUN);
            assertTrue(HelloLocalInterceptor.RUN);
            assertTrue(HelloDecorator.RUN);

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    public static interface Hello {
        public void hello();
    }

    @InterceptorBinding
    @Target(value = {ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface LocalEjbInterceptorBinding {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public static @interface LocalEjbQualifier {

    }

    @Stateless
    public static class HelloStateless implements Hello {

        @Inject
        private HelloCdiBean cdiBean;
        
        public static boolean RUN = false;

        @Override
        public void hello() {
            RUN = true;
            System.out.println("In EJB : " + HelloStateless.class.getName());
            cdiBean.sayHelloWorld();
        }
    }

    public static class HelloCdiBean {

        @Inject
        @LocalEjbQualifier
        private Hello helloEjb;
        
        public static boolean RUN = false;

        public void sayHelloWorld() {
            RUN = true;
            System.out.println("In Managed Bean : " + HelloCdiBean.class.getName());
            this.helloEjb.hello();
        }
    }

    @Decorator
    public static class HelloDecorator implements Hello {

        public static boolean RUN = false;

        @Inject
        @Delegate
        @LocalEjbQualifier
        private Hello hello;

        @Override
        public void hello() {
            System.out.println("In CDI Style Decorator  : " + HelloDecorator.class.getName());
            RUN = true;
            this.hello.hello();
        }
    }

    @Interceptor
    @LocalEjbInterceptorBinding
    public static class HelloLocalInterceptor {

        public static boolean RUN = false;

        @AroundInvoke
        public Object aroundInvoke(InvocationContext ctx) throws Exception {
            System.out.println("In CDI Style Interceptor  : " + HelloLocalInterceptor.class.getName());
            RUN = true;
            return ctx.proceed();
        }
    }

    @LocalBean
    @LocalEjbQualifier
    @LocalEjbInterceptorBinding
    public static class LocalHello implements Hello {

        public static boolean RUN = false;

        @Override
        public void hello() {
            System.out.println("In EJB : " + LocalHello.class.getName());
            RUN = true;
        }
    }
}
