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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cdi.tck;

import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.jboss.testharness.api.DeploymentException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.xbean.finder.ClassFinder;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;

/**
 * @version $Rev$ $Date$
 */
public class ServiceProviders {

    public static class ManagersProvider implements org.jboss.jsr299.tck.spi.Managers {
        public BeanManager getManager() {
            // TODO This doesn't really work.  We need some better way to get the AppContext
            final ThreadContext threadContext = ThreadContext.getThreadContext();
            final CoreDeploymentInfo deployment = threadContext.getDeploymentInfo();
            return deployment.getModuleContext().getAppContext().getBeanManager();
        }

    }

    public static class BeansProvider implements org.jboss.jsr299.tck.spi.Beans {
        public boolean isProxy(Object instance) {
            return false;
        }
    }

    public static class ContextsProvider implements org.jboss.jsr299.tck.spi.Contexts {
        public void setActive(Context context) {
        }

        public void setInactive(Context context) {
        }

        public Context getRequestContext() {
            return null;
        }

        public Context getDependentContext() {
            return null;
        }

        public void destroyContext(Context context) {
        }
    }

    public static class StandaloneContainersProvider implements org.jboss.testharness.spi.StandaloneContainers {

        private DeploymentException deploymentException;

        public void deploy(Collection<Class<?>> classes) throws DeploymentException {
            System.out.println("StandaloneContainersImpl.deploy(classes)");
            for (Class<?> clazz : classes) {
                System.out.println("clazz = " + clazz);
            }
        }

        public boolean deploy(Collection<Class<?>> classes, Collection<URL> urls) {
            try {
                EjbModule ejbModule = new EjbModule(new EjbJar("beans"));
                ejbModule.setFinder(new ClassFinder(new ArrayList(classes)));

                Map<String,Object> dds = ejbModule.getAltDDs();

                for (URL url : urls) {
                    final File file = new File(url.getFile());
                    dds.put(file.getName(), url);
                }

                Assembler assembler = new Assembler();
                ConfigurationFactory config = new ConfigurationFactory();
                assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
                assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
                
                assembler.createApplication(config.configureApplication(ejbModule));
            } catch (Exception e) {
                e.printStackTrace();
                deploymentException = new DeploymentException("Deploy failed", e);
                return false;
            }
//            System.out.println("StandaloneContainersImpl.deploy(classes, urls)");
//            for (Class<?> clazz : classes) {
//                System.out.println("clazz = " + clazz);
//            }
//            for (URL url : urls) {
//                System.out.println("url = " + url);
//            }
            return true;
        }

        public DeploymentException getDeploymentException() {
            return deploymentException;
        }

        public void undeploy() {
        }

        public void setup() {

        }

        public void cleanup() {
        }
    }

    public static class ELProvider implements org.jboss.jsr299.tck.spi.EL {
        public <T> T evaluateValueExpression(String expression, Class<T> expectedType) {
            return null;
        }

        public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams) {
            return null;
        }

        public ELContext createELContext() {
            return null;
        }
    }

}
