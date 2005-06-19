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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.client.proxy;

import java.util.Properties;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class ProxyManager {


    //=============================================================
    //  Methods and members for the ProxyManager abstract factory
    //
    private static ProxyFactory defaultFactory;
    private static String defaultFactoryName;

    static {
        String version = null;
        Class factory  = null;        
        try {
            version = System.getProperty("java.vm.version");
        } catch ( Exception e ) {
            //TODO: Better exception handling
            throw new RuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }
        ClassLoader cl = getContextClassLoader();

        if ( version.startsWith("1.1") ) {
            throw new RuntimeException("This VM version is not supported: "+version);
        } else if ( version.startsWith("1.2") ) {
            defaultFactoryName = "JDK 1.2 ProxyFactory";

            try {
                Class.forName("org.opentools.proxies.Proxy", true, cl);
            } catch ( Exception e ) {
                //TODO: Better exception handling
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.opentools.proxies.Proxy.  This class is needed for generating proxies in JDK 1.2 VMs.");
            }

            try {
                factory = Class.forName("org.openejb.client.proxy.Jdk12ProxyFactory", true, cl);
            } catch ( Exception e ) {
                //TODO: Better exception handling
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.openejb.client.proxy.Jdk12ProxyFactory.");
            }
        } else {
            defaultFactoryName = "JDK 1.3 ProxyFactory";

            try {
                factory = Class.forName("org.openejb.client.proxy.Jdk13ProxyFactory", true, cl);
            } catch ( Exception e ) {
                //TODO: Better exception handling
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.openejb.client.proxy.Jdk13ProxyFactory.");
            }
        } 

        try {
            
            defaultFactory = (ProxyFactory)factory.newInstance();
            defaultFactory.init( new Properties() );

        } catch ( Exception e ) {
            //TODO: Better exception handling
            throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.openejb.client.proxy.Jdk13ProxyFactory.");
        }

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
     *
     * Executes on the default ProxyFactory instance.
     *
     * @param proxy  The Proxy object to retreive the InvocationHandler from.
     * @return The implementation of InvocationHandler handling invocations on the specified Proxy object.
     */
    public static InvocationHandler getInvocationHandler(Object proxy) {
        return defaultFactory.getInvocationHandler(proxy);
    }

    /**
     * Casts the object passed in to the appropriate proxy type and sets
     * the InvocationHandler assigned to it.
     *
     * @param proxy  The Proxy object to retreive the InvocationHandler from.
     * @return The Proxy object with the new InvocationHandler.
     */
    public static Object setInvocationHandler(Object proxy, InvocationHandler handler) {
        return defaultFactory.setInvocationHandler(proxy, handler);
    }

    /**
     * Loads and returns the proxy implementation for the specified interface.
     *
     * @param interfaceType
     * @return Class
     * @exception IllegalAccessException
     */
    public static Class getProxyClass(Class interfaceType) throws IllegalAccessException{
        return getProxyClass(new Class[]{interfaceType});
    }

    public static Class getProxyClass(Class[] interfaces) throws IllegalAccessException{
        return defaultFactory.getProxyClass( interfaces);
    }

    /**
     * Throws a RuntimeException if there is a problem
     * instantiating the new proxy instance.
     *
     * @param interfaceType
     *               A bean's home or remote interface that the Proxy
     *               object should implement.
     * @param h
     * @return Object
     * @exception IllegalAccessException
     */
    public static Object newProxyInstance(Class interfaceType, InvocationHandler h) throws IllegalAccessException {
        return newProxyInstance(new Class[]{interfaceType}, h);
    }

    public static Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(interfaces, h);
    }

    /**
     *
     * @param cl
     * @return boolean
     */
    public static boolean isProxyClass(Class cl) {
        return defaultFactory.isProxyClass(cl);
    }

    /**
    * Create a new proxy instance given a proxy class.
    */
    public static Object newProxyInstance(Class proxyClass) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(proxyClass);
    }
    //
    //  Methods and members for the ProxyFactory abstract factory
    //===================================================
    
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
