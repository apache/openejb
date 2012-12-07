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
package org.apache.openejb.core.managed;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;

import javax.ejb.RemoveException;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public class ManagedHomeHandler extends EjbHomeProxyHandler {

    public ManagedHomeHandler(BeanContext beanContext, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        super(beanContext, interfaceType, interfaces, mainInterface);
    }

    public Object createProxy(Object primaryKey, Class mainInterface) {
        Object proxy = super.createProxy(primaryKey, mainInterface);
        EjbObjectProxyHandler handler = null;

        try {
            handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);
        } catch (Exception e) {
            // try getting the invocation handler from the localbean
            try {
                Field field = proxy.getClass().getDeclaredField("invocationHandler");
                field.setAccessible(true);
                handler = (EjbObjectProxyHandler) field.get(proxy);
            } catch (Exception e1) {
            }
        }

        registerHandler(handler.getRegistryId(), handler);
        return proxy;
    }

    protected Object findX(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(BeanContext beanContext, Object pk, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        return new ManagedObjectHandler(getBeanContext(), pk, interfaceType, interfaces, mainInterface);
    }

}
