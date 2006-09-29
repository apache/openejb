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

import java.io.Serializable;
import java.lang.reflect.Method;


import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CgLibInvocationHandler implements MethodInterceptor, InvocationHandler, Serializable {

    Object[] NO_ARGS = new Object[0];
    
    private InvocationHandler delegate;

    public CgLibInvocationHandler() {}

    public CgLibInvocationHandler(InvocationHandler delegate) {
        setInvocationHandler(delegate);
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy mProxy) throws Throwable {
        return invoke(proxy, method, args);
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(args == null) {
            args = NO_ARGS;
        }
        
        if ( delegate == null ) throw new NullPointerException("No invocation handler for proxy "+proxy);
        
        return delegate.invoke(proxy, method, args);
    }
    
    public InvocationHandler getInvocationHandler() {
        return delegate;
    }

    public InvocationHandler setInvocationHandler(InvocationHandler handler) {
        InvocationHandler old = delegate;
        delegate = handler;
        return old;
    }
    
   }

