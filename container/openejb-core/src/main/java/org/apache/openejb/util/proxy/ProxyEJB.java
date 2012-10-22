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
package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.ivm.IntraVmProxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyEJB {
    private ProxyEJB() {
        // no-op
    }

    public static Object subclassProxy(final BeanContext beanContext) {
        try {
            return LocalBeanProxyFactory.newProxyInstance(beanContext.getModuleContext().getClassLoader(), new Handler(beanContext),
                        beanContext.getBeanClass(), IntraVmProxy.class, Serializable.class);
        } catch (InternalError ie) { // try without intravmproxy which is maybe not loadable (in OSGi it can happen)
            return LocalBeanProxyFactory.newProxyInstance(beanContext.getModuleContext().getClassLoader(), new Handler(beanContext),
                    beanContext.getBeanClass(), Serializable.class);
        }
    }

    // same as proxy() but it doesn't add IvmProxy and Serializable interfaces (use in bridges proxies like OSGi services)
    public static Object simpleProxy(final BeanContext beanContext, final Class<?>[] itfs) {
        if (beanContext.isLocalbean()) {
            return LocalBeanProxyFactory.newProxyInstance(itfs[0].getClassLoader(), new Handler(beanContext), itfs[0]);
        }
        return Proxy.newProxyInstance(itfs[0].getClassLoader(), itfs, new Handler(beanContext));
    }

    public static Object proxy(final BeanContext beanContext, final Class<?>[] itfs) {
        if (beanContext.isLocalbean()) {
            return LocalBeanProxyFactory.newProxyInstance(itfs[0].getClassLoader(), new Handler(beanContext), itfs[0], IntraVmProxy.class, Serializable.class);
        }
        return Proxy.newProxyInstance(itfs[0].getClassLoader(), itfs, new Handler(beanContext));
    }

    private static class Handler implements java.lang.reflect.InvocationHandler {
        private BeanContext beanContext;

        public Handler(BeanContext bc) {
            beanContext = bc;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final RpcContainer container = RpcContainer.class.cast(beanContext.getContainer());
            return container.invoke(beanContext.getDeploymentID(),
                    beanContext.getInterfaceType(method.getDeclaringClass()),
                    method.getDeclaringClass(), method, args, null);
        }
    }
}
