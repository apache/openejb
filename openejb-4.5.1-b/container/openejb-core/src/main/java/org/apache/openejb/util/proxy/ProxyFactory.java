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
package org.apache.openejb.util.proxy;

import org.apache.openejb.OpenEJBException;

import java.util.Properties;

public interface ProxyFactory {

    public void init(Properties props) throws OpenEJBException;

    public java.lang.reflect.InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException;

    public Class getProxyClass(Class interfce) throws IllegalArgumentException;

    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException;

    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl);

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, java.lang.reflect.InvocationHandler h) throws IllegalArgumentException;

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, java.lang.reflect.InvocationHandler h) throws IllegalArgumentException;

}

