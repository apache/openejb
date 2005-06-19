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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb.util.proxy;

import java.util.HashMap;


/**
 *
 * @author David Blevins
 * @author Richard Monson-Haefel
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class ProxyManager {


    //=============================================================
    //  Methods and members for the ProxyManager abstract factory
    //
    private static volatile ProxyFactory defaultFactory;
    private static final HashMap factories = new HashMap();
    private static volatile String defaultFactoryName;

    public static synchronized ProxyFactory registerFactory(String factoryName, ProxyFactory factory){
        return (ProxyFactory)factories.put( factoryName, factory );
    }

    public static synchronized ProxyFactory unregisterFactory(String factoryName){
        return (ProxyFactory)factories.remove( factoryName );
    }

    public static void checkDefaultFactory(){
        if (defaultFactory == null) throw new IllegalStateException("[Proxy Manager] No default proxy factory specified.");
    }

    public static ProxyFactory getFactory(String factoryName){
        return (ProxyFactory)factories.get(factoryName);
    }

    /**
     * Sets the default factory.
     *
     * The factory must already be registered.
     *
     * @param factoryName
     */
    public static synchronized ProxyFactory setDefaultFactory(String factoryName){
        ProxyFactory newFactory = getFactory(factoryName);
        if (newFactory == null) return defaultFactory;

        ProxyFactory oldFactory = defaultFactory;
        defaultFactory = newFactory;
        defaultFactoryName = factoryName;

        return oldFactory;
    }

    public static ProxyFactory getDefaultFactory(){
        return defaultFactory;
    }

    public static String getDefaultFactoryName(){
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
        checkDefaultFactory();
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
        checkDefaultFactory();
        return defaultFactory.setInvocationHandler(proxy, handler);
    }

    /**
     * Loads and returns the proxy implementation for the specified interface.
     *
     * The Class object is loaded using ProxyClassLoader.loadClass.  If the class
     * definition is not found, the findClass method will be called by the VM; at which
     * point, the proxy class byte code will be generated by ProxyFactory and resolved by
     * the VM.
     *
     * @param interfaceType
     * @return Class
     * @exception IllegalAccessException
     */
    public static Class getProxyClass(Class interfaceType) throws IllegalAccessException{
        return getProxyClass(new Class[]{interfaceType});
    }
    public static Class getProxyClass(Class[] interfaces) throws IllegalAccessException{
        checkDefaultFactory();
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
        checkDefaultFactory();
        return defaultFactory.newProxyInstance(interfaces, h);
    }

    /**
     *
     * @param cl
     * @return boolean
     */
    public static boolean isProxyClass(Class cl) {
        checkDefaultFactory();
        return defaultFactory.isProxyClass(cl);
    }

    /**
    * Create a new proxy instance given a proxy class.
    */
    public static Object newProxyInstance(Class proxyClass) throws IllegalAccessException {
        checkDefaultFactory();
        return defaultFactory.newProxyInstance(proxyClass);
    }
    //
    //  Methods and members for the ProxyFactory abstract factory
    //===================================================
}
