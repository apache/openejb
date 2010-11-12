/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateless;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.EJBException;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

/**
 * @version $Revision: 961763 $ $Date: 2010-07-08 09:39:00 -0400 (Thu, 08 Jul 2010) $
 */
public class StatelessPoolLeakTest extends TestCase {

    public void test() throws Exception {

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("CounterBeanLocal");
        final Counter counter = (Counter) object;

        assertLeakFree(counter);

        assertLeakFree(counter);

        assertLeakFree(counter);
    }

    private void assertLeakFree(Counter counter) {
        assertCreateFailure(counter);

        // perform another failure, should also be a create exception
        // if a leak is present, this will result in a pool Timeout
        assertCreateFailure(counter);

        assertInstanceCreated(counter);

        // Make the bean throw a runtime exception
        // which will cause the pool to be empty again
        assertBusinessFailure(counter);

        assertInstanceCreated(counter);

        assertBusinessFailure(counter);
    }

    private void assertBusinessFailure(Counter counter) {
        CounterBean.failOnCreate.set(false);
        CounterBean.failOnBusinessMethod.set(true);

        final int expected = CounterBean.instances.get();
        assertFailure(counter);
        assertEquals(expected, CounterBean.instances.get());
    }

    private void assertInstanceCreated(Counter counter) {
        CounterBean.failOnCreate.set(false);
        CounterBean.failOnBusinessMethod.set(false);

        final int expected = CounterBean.instances.get() + 1;
        // Should now be successful
        counter.count();
        assertEquals(expected, CounterBean.instances.get());
    }

    private void assertCreateFailure(Counter counter) {
        CounterBean.failOnCreate.set(true);
        CounterBean.failOnBusinessMethod.set(false);

        // perform one failure, should be a create exception
        final int expected = CounterBean.instances.get() + 1;
        assertFailure(counter);
        assertEquals(expected, CounterBean.instances.get());
    }

    private void assertFailure(Counter counter) {
        try {
            counter.count();
            fail("Exception should have failed on create");
        } catch (Throwable t) {

            if (!EJBException.class.equals(t.getClass())) {
                fail("An EJBException should have been thrown:" + t.getClass().getName());
            }

            if (!CustomException.class.equals(t.getCause().getClass())) {
                fail("A CustomException should have been the cause: " + t.getCause().getClass().getName());
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("MaxSize", "1");
        statelessContainerInfo.properties.setProperty("MinSize", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        StatelessBean bean = new StatelessBean(CounterBean.class);
        bean.addBusinessLocal(Counter.class.getName());

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        CounterBean.instances.set(0);
        assembler.createApplication(config.configureApplication(ejbJar));

    }

    public static interface Counter {
        int count();
    }

    public static class CustomException extends RuntimeException {

    }
    
    @Stateless
    public static class CounterBean implements Counter {

        public static AtomicInteger instances = new AtomicInteger();
        public static AtomicBoolean failOnCreate = new AtomicBoolean();
        public static AtomicBoolean failOnBusinessMethod = new AtomicBoolean();

        private int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        @PostConstruct
        public void postConstruct() {
            if (failOnCreate.get()) throw new CustomException();
        }
        
        public int count(){
            if (failOnBusinessMethod.get()) throw new CustomException();
        	return instances.get();
        }
    }
}
