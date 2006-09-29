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
package org.apache.openejb.client;

import java.util.Properties;


/**
 * @since 11/25/2001
 */
public class ProxyManager {
    //=============================================================
    //  Methods and members for the ProxyManager abstract factory
    //
    private static ProxyFactory defaultFactory;
    private static String defaultFactoryName = "GcLib ProxyFactory";

    static {
        loadProxyFactory(CgLibProxyFactory.class.getName());
    }

    public static ProxyFactory getInstance() {
        return defaultFactory;
    }

    public static ProxyFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactoryName;
    }

    /**
     * Casts the object passed in to the appropriate proxy type and retreives
     * the InvocationHandler assigned to it.
     * <p/>
     * Executes on the default ProxyFactory instance.
     *
     * @param proxy The Proxy object to retreive the InvocationHandler from.
     * @return The implementation of InvocationHandler handling invocations on the specified Proxy object.
     */
    public static InvocationHandler getInvocationHandler(Object proxy) {
        return defaultFactory.getInvocationHandler(proxy);
    }

    public static Object newProxyInstance(Class[] interfaces, InvocationHandler h, ClassLoader classLoader) {
        return defaultFactory.newProxyInstance(interfaces, h, classLoader);
    }

    public static boolean isProxyClass(Class cl) {
        return defaultFactory.isProxyClass(cl);
    }

    //
    //  Methods and members for the ProxyFactory abstract factory
    //===================================================
    
    private static void loadProxyFactory(String factoryClassName) {
        Class factory = null;
        try {
            factory = Thread.currentThread().getContextClassLoader().loadClass(factoryClassName);
            defaultFactory = (ProxyFactory) factory.newInstance();
            defaultFactory.init(new Properties());
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class " + factoryClassName);
        } catch (InstantiationException e1) {
            throw new RuntimeException("No ProxyFactory Can be installed. Unable to instantiate " + factoryClassName);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException("No ProxyFactory Can be installed. Unable to access " + factoryClassName);
        }
    }
}
