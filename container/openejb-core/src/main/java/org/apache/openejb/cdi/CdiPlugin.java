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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.SessionBeanType;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.ejb.common.util.EjbDefinitionUtility;
import org.apache.webbeans.ejb.common.util.EjbUtility;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;

public class CdiPlugin extends AbstractOwbPlugin implements OpenWebBeansJavaEEPlugin,OpenWebBeansEjbPlugin {

    private Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"),CdiPlugin.class);
    
    private AppInfo appModule;
    private ClassLoader classLoader;
    private final Map<Integer, Set<Class<?>>> beans = new HashMap<Integer, Set<Class<?>>>();
    private final Map<Class<?>, String> beanIds = new HashMap<Class<?>, String>();
    private final SecurityService SECURITY_SERVICE = new CdiSecurityService();
    public final TransactionService TRANSACTION_SERVICE = new CdiTransactionService();

    @Override
    public void shutDown() throws Exception {
        super.shutDown();
        this.beanIds.clear();
        this.beans.clear();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setAppModule(AppInfo appModule) {
        this.appModule = appModule;
    }

    public void configureDeployments() {
        List<EjbJarInfo> ejbModules = appModule.ejbJars;
        for (EjbJarInfo ejbModule : ejbModules) {
            List<EnterpriseBeanInfo> enterpriseBeans = ejbModule.enterpriseBeans;
            for (EnterpriseBeanInfo eb : enterpriseBeans) {
                try {
                    Class<?> clazz = this.classLoader.loadClass(eb.ejbClass);
                    beanIds.put(clazz, eb.ejbDeploymentId);
                    putClass(eb.type, clazz);
                } catch (Exception e) {
                    String error = "Error is occured while getting information from ejb modules";
                    logger.error(error, e);
                    throw new RuntimeException(error, e);
                }
            }
        }
    }

    private void putClass(Integer type, Class<?> clazz) {
        Set<Class<?>> classes = beans.get(type);
        if (classes == null) {
            classes = new HashSet<Class<?>>();
            beans.put(type, classes);
        }

        classes.add(clazz);
    }

    @Override
    public <T> T getSupportedService(Class<T> serviceClass) {
        if (serviceClass == TransactionService.class) {
            return serviceClass.cast(TRANSACTION_SERVICE);
        } else if (serviceClass == SecurityService.class) {
            return serviceClass.cast(SECURITY_SERVICE);
        }

        return null;
    }

    @Override
    public void isManagedBean(Class<?> clazz) throws Exception {
        if (isSessionBean(clazz)) {
            throw new WebBeansConfigurationException("Managed Bean implementation class : "
                    + clazz.getName() + " can not be sesion bean class");
        }

    }

    @Override
    public boolean supportService(Class<?> serviceClass) {
        if ((serviceClass == TransactionService.class) || serviceClass == SecurityService.class) {
            return true;
        }

        return false;
    }

    @Override
    public <T> Bean<T> defineSessionBean(Class<T> clazz,
            ProcessAnnotatedType<T> processAnnotateTypeEvent) {
        if (!isSessionBean(clazz)) {
            throw new IllegalArgumentException("Given class is not an session bean class");
        }

        ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo info = null;
        SessionBeanType type = SessionBeanType.STATELESS;

        info = cs.getDeploymentInfo(this.beanIds.get(clazz));

        CdiEjbBean<T> bean = new CdiEjbBean<T>(clazz);
        bean.setDeploymentInfo(info);
        bean.setEjbType(type);

        EjbUtility.fireEvents(clazz, bean, processAnnotateTypeEvent);

        return bean;

    }

    @Override
    public Object getSessionBeanProxy(Bean<?> bean, Class<?> iface,
            CreationalContext<?> creationalContext) {
        return EjbDefinitionUtility.defineEjbBeanProxy((CdiEjbBean<?>) bean, iface,
                creationalContext);
    }

    @Override
    public boolean isSessionBean(Class<?> clazz) {

        for (Entry<Integer, Set<Class<?>>> entries : this.beans.entrySet()) {
            Set<Class<?>> classes = entries.getValue();
            if (classes.contains(clazz)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSingletonBean(Class<?> clazz) {
        for (Entry<Integer, Set<Class<?>>> entries : this.beans.entrySet()) {
            Set<Class<?>> classes = entries.getValue();
            for (Class<?> clazzFound : classes) {
                if (clazzFound == clazz && entries.getKey().equals(EnterpriseBeanInfo.SINGLETON)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isStatefulBean(Class<?> clazz) {
        for (Entry<Integer, Set<Class<?>>> entries : this.beans.entrySet()) {
            Set<Class<?>> classes = entries.getValue();
            for (Class<?> clazzFound : classes) {
                if (clazzFound == clazz && entries.getKey().equals(EnterpriseBeanInfo.STATEFUL)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isStatelessBean(Class<?> clazz) {
        for (Entry<Integer, Set<Class<?>>> entries : this.beans.entrySet()) {
            Set<Class<?>> classes = entries.getValue();
            for (Class<?> clazzFound : classes) {
                if (clazzFound == clazz && entries.getKey().equals(EnterpriseBeanInfo.STATELESS)) {
                    return true;
                }
            }
        }
        return false;
    }
}
