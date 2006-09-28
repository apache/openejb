/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: CgLibProxyFactory.java 445793 2005-10-24 01:21:03Z djencks $
 */
package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;

/**
 *
 */
public class CgLibProxyFactory implements ProxyFactory {


    public void init(Properties props) {
    }

    public InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException {
        if (proxy instanceof CgLibProxy) {
            return ((CgLibProxy) proxy).handler.getInvocationHandler();
        } else {
            throw new IllegalArgumentException("Object passed in is not a CgLibProxy");
        }
    }

    public boolean isProxyClass(Class cl) {
        return cl.isAssignableFrom(CgLibProxy.class);
    }

    public Object newProxyInstance(Class[] interfaces, InvocationHandler handler, ClassLoader classLoader)
            throws IllegalArgumentException {
        return newProxyInstance(CgLibProxy.class, interfaces, handler, classLoader);
    }

    public Object newProxyInstance(Class superClass, Class[] interfaces, InvocationHandler handler, ClassLoader classLoader)
            throws IllegalArgumentException {

        CgLibInvocationHandler interceptor = new CgLibInvocationHandler(handler);
        Enhancer enhancer = getEnhancer(superClass, interfaces);
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, interceptor});
        enhancer.setClassLoader(classLoader);

        return enhancer.create(new Class[]{CgLibInvocationHandler.class}, new Object[]{interceptor});
    }

    private Enhancer getEnhancer(Class superClass, Class[] interfaces) {
        Enhancer enhancer;
        enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(superClass));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        return enhancer;
    }

    private static class NoOverrideCallbackFilter implements CallbackFilter {
        private Class superClass;

        public NoOverrideCallbackFilter(Class superClass) {
            this.superClass = superClass;
        }

        public int accept(Method method) {
            // we don't intercept non-public methods like finalize
            if (!Modifier.isPublic(method.getModifiers())) {
                return 0;
            }

            if (method.getName().equals("remove") && Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }

            try {
                // if the super class defined this method don't intercept
                superClass.getMethod(method.getName(), method.getParameterTypes());
                return 0;
            } catch (Throwable e) {
                return 1;
            }
        }
        
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other == this) {
                return true;
            }

            NoOverrideCallbackFilter otherFilter = null;
            if (other instanceof NoOverrideCallbackFilter) {
                otherFilter = (NoOverrideCallbackFilter) other;
            } else {
                return false;
            }

            return superClass.equals(otherFilter.superClass);
        }

        public int hashCode() {
            return superClass.hashCode();
        }


    }

}
