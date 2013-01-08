/*
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
package org.apache.openejb.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.AppContext;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.cdi.ConstructorInjectionBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.AbstractInjectable;

public class WebContext {
    private String id;
    private ClassLoader classLoader;
    private final Collection<Injection> injections = new ArrayList<Injection>();
    private Context jndiEnc;
    private final AppContext appContext;
    private Map<String,Object> bindings;
    private Map<Object, CreationalContext<?>> creatonalContexts = new ConcurrentHashMap<Object, CreationalContext<?>>();
    private WebBeansContext webbeansContext;
    private String contextRoot;
    private Context initialContext;

    public Context getInitialContext() {
        if (initialContext != null) return initialContext;
        try {
            initialContext = (Context) new InitialContext().lookup("java:");
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
        return initialContext;
    }

    public void setInitialContext(final Context initialContext) {
        this.initialContext = initialContext;
    }

    public WebContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Collection<Injection> getInjections() {
        return injections;
    }

    public Context getJndiEnc() {
        return jndiEnc;
    }

    public void setJndiEnc(Context jndiEnc) {
        this.jndiEnc = jndiEnc;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public Object newInstance(Class beanClass) throws OpenEJBException {

            final WebBeansContext webBeansContext = getWebBeansContext();

            final ConstructorInjectionBean<Object> beanDefinition = new ConstructorInjectionBean<Object>(webBeansContext, beanClass).complete();

            final CreationalContext<Object> creationalContext = webBeansContext.getBeanManagerImpl().createCreationalContext(beanDefinition);

            // Create bean instance
            final Object o = beanDefinition.create(creationalContext);
            final Context unwrap = InjectionProcessor.unwrap(getInitialContext());
            final InjectionProcessor injectionProcessor = new InjectionProcessor(o, injections, unwrap);

            final Object beanInstance = injectionProcessor.createInstance();

            final Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();

            try {
                AbstractInjectable.instanceUnderInjection.set(null);

                InjectionTargetBean<Object> bean = InjectionTargetBean.class.cast(beanDefinition);

                bean.injectResources(beanInstance, creationalContext);
                bean.injectSuperFields(beanInstance, creationalContext);
                bean.injectSuperMethods(beanInstance, creationalContext);
                bean.injectFields(beanInstance, creationalContext);
                bean.injectMethods(beanInstance, creationalContext);
            } finally {
                if (oldInstanceUnderInjection != null) {
                    AbstractInjectable.instanceUnderInjection.set(oldInstanceUnderInjection);
                } else {
                    AbstractInjectable.instanceUnderInjection.remove();
                }
            }

            creatonalContexts.put(beanInstance, creationalContext);
            return beanInstance;
    }

    private WebBeansContext getWebBeansContext() {
        if (webbeansContext == null) {
            return getAppContext().getWebBeansContext();
        }
        return webbeansContext;
    }

    public Object inject(Object o) throws OpenEJBException {

        try {
            final WebBeansContext webBeansContext = getWebBeansContext();

            final ConstructorInjectionBean<Object> beanDefinition = new ConstructorInjectionBean(webBeansContext, o.getClass()).complete();

            final CreationalContext<Object> creationalContext = webBeansContext.getBeanManagerImpl().createCreationalContext(beanDefinition);

            // Create bean instance
            final Context initialContext = (Context) new InitialContext().lookup("java:");
            final Context unwrap = InjectionProcessor.unwrap(initialContext);
            final InjectionProcessor injectionProcessor = new InjectionProcessor(o, injections, unwrap);

            final Object beanInstance = injectionProcessor.createInstance();

            final Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();

            try {
                AbstractInjectable.instanceUnderInjection.set(null);

                InjectionTargetBean<Object> bean = InjectionTargetBean.class.cast(beanDefinition);

                bean.injectResources(beanInstance, creationalContext);
                bean.injectSuperFields(beanInstance, creationalContext);
                bean.injectSuperMethods(beanInstance, creationalContext);
                bean.injectFields(beanInstance, creationalContext);
                bean.injectMethods(beanInstance, creationalContext);
            } finally {
                if (oldInstanceUnderInjection != null) {
                    AbstractInjectable.instanceUnderInjection.set(oldInstanceUnderInjection);
                } else {
                    AbstractInjectable.instanceUnderInjection.remove();
                }
            }

            // if the bean is dependent simply cleanup the creational context once it is created
            final Class<? extends Annotation> scope = beanDefinition.getScope();
            if (scope == null || Dependent.class.equals(scope)) {
                creationalContext.release();
            }

            return beanInstance;
        } catch (NamingException e) {
            throw new OpenEJBException(e);
        }
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setWebbeansContext(WebBeansContext webbeansContext) {
        this.webbeansContext = webbeansContext;
    }

    public WebBeansContext getWebbeansContext() {
        return webbeansContext;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void destroy(final Object o) {
        final CreationalContext<?> ctx = creatonalContexts.remove(o);
        if (ctx != null) {
            ctx.release();
        }
    }
}
