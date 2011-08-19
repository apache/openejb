/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.core.stateful;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

/**
 * @version $Rev$ $Date$
 */
public class StatefulSessionSynchronizationTest extends TestCase {

    private static final List<Call> result = new ArrayList<Call>();

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();

        //Test SessionSynchronization interface
        StatefulBean subBeanA = new StatefulBean(SubBeanA.class);
        ejbJar.addEnterpriseBean(subBeanA);

        //Test SessionSynchronization interface but methods are in the parent class
        StatefulBean subBeanH = new StatefulBean(SubBeanH.class);
        ejbJar.addEnterpriseBean(subBeanH);

        //Test SessionSynchronization interface but methods are in the parent class
        //using @LocalBean
        StatefulBean subBeanI = new StatefulBean(SubBeanI.class);
        ejbJar.addEnterpriseBean(subBeanI);

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);
        InitialContext context = new InitialContext();

        List<Call> expectedResult = Arrays.asList(Call.BEAN_AFTER_BEGIN, Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD,
                Call.BEAN_AROUND_INVOKE_AFTER, Call.BEAN_BEFORE_COMPLETION,
                Call.BEAN_AFTER_COMPLETION);

        {
            BeanInterface beanH = (BeanInterface) context.lookup("SubBeanHLocal");
            beanH.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            BeanInterface beanI = (BeanInterface) context.lookup("SubBeanILocalBean");
            beanI.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }
    }

    public static interface BeanInterface {

        public void simpleMethod();
    }

    public static class BaseBean implements BeanInterface {

        @TransactionAttribute(TransactionAttributeType.REQUIRED)
        public void simpleMethod() {
            result.add(Call.BEAN_METHOD);
        }

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            result.add(Call.BEAN_AROUND_INVOKE_BEGIN);
            Object o = context.proceed();
            result.add(Call.BEAN_AROUND_INVOKE_AFTER);
            return o;
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanA extends BaseBean implements SessionSynchronization {

        @Override
        public void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        @Override
        public void afterCompletion(boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }
    }

    @LocalBean
    @Stateful
    public static class BaseBeanB implements BeanInterface, SessionSynchronization{

        //@TransactionAttribute(TransactionAttributeType.REQUIRED)
        public void simpleMethod() {
            result.add(Call.BEAN_METHOD);
        }

        public void afterBegin() {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        public void afterCompletion(boolean arg0) {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        public void beforeCompletion() {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }

        @AroundInvoke
        public Object aroundInvoke(InvocationContext context) throws Exception {
            result.add(Call.BEAN_AROUND_INVOKE_BEGIN);
            Object o = context.proceed();
            result.add(Call.BEAN_AROUND_INVOKE_AFTER);
            return o;
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanH extends BaseBeanB implements SessionSynchronization {
    }

    @Stateful
    @LocalBean
    public static class SubBeanI extends BaseBeanB {
    }

    public static enum Call {
        BEAN_METHOD, BEAN_AROUND_INVOKE_BEGIN, BEAN_AROUND_INVOKE_AFTER, BEAN_AFTER_BEGIN, BEAN_BEFORE_COMPLETION, BEAN_AFTER_COMPLETION, BAD_VALUE,
    }
}
