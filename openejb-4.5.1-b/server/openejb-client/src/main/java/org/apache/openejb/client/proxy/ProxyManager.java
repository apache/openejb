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
package org.apache.openejb.client.proxy;

import org.apache.openejb.client.ClientRuntimeException;

import java.util.Properties;

public class ProxyManager {

    private static ProxyFactory defaultFactory;
    private static String defaultFactoryName;

    static {
        String version = null;
        Class factory = null;
        try {
            version = System.getProperty("java.vm.version");
        } catch (Exception e) {

            throw new ClientRuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }
        ClassLoader cl = getContextClassLoader();

        if (version.startsWith("1.1")) {
            throw new ClientRuntimeException("This VM version is not supported: " + version);
        } else if (version.startsWith("1.2")) {
            defaultFactoryName = "JDK 1.2 ProxyFactory";

            try {
                Class.forName("org.opentools.proxies.Proxy", true, cl);
            } catch (Exception e) {

                throw new ClientRuntimeException("No ProxyFactory Can be installed. Unable to load the class org.opentools.proxies.Proxy.  This class is needed for generating proxies in JDK 1.2 VMs.");
            }

            try {
                factory = Class.forName("org.apache.openejb.client.proxy.Jdk12ProxyFactory", true, cl);
            } catch (Exception e) {

                throw new ClientRuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk12ProxyFactory.");
            }
        } else {
            defaultFactoryName = "JDK 1.3 ProxyFactory";

            try {
                factory = Class.forName("org.apache.openejb.client.proxy.Jdk13ProxyFactory", true, cl);
            } catch (Exception e) {

                throw new ClientRuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk13ProxyFactory.");
            }
        }

        try {

            defaultFactory = (ProxyFactory) factory.newInstance();
            defaultFactory.init(new Properties());

        } catch (Exception e) {

            throw new ClientRuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk13ProxyFactory.");
        }

    }

    public static ProxyFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactoryName;
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        return defaultFactory.getInvocationHandler(proxy);
    }

    public static Object setInvocationHandler(Object proxy, InvocationHandler handler) {
        return defaultFactory.setInvocationHandler(proxy, handler);
    }

    public static Class getProxyClass(Class interfaceType) throws IllegalAccessException {
        return getProxyClass(new Class[]{interfaceType});
    }

    public static Class getProxyClass(Class[] interfaces) throws IllegalAccessException {
        return defaultFactory.getProxyClass(interfaces);
    }

    public static Object newProxyInstance(Class interfaceType, InvocationHandler h) throws IllegalAccessException {
        return newProxyInstance(new Class[]{interfaceType}, h);
    }

    public static Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(interfaces, h);
    }

    public static boolean isProxyClass(Class cl) {
        return defaultFactory.isProxyClass(cl);
    }

    public static Object newProxyInstance(Class proxyClass) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(proxyClass);
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }
}
