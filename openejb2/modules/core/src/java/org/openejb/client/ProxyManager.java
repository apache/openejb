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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * $Id$
 */
package org.openejb.client;

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
