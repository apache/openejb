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
 * Allows us to implement different versions of Proxies
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public interface ProxyFactory {

    /**
     * Prepares the ProxyFactory for use.  Called once right after
     * the ProxyFactory is instantiated.
     * 
     * @param props
     */
    public void init(Properties props) ;

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException;

    /**
     * Sets the invocation handler for the specified proxy instance.
     */
    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException;

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader 
     * and an array of interfaces.
     * 
     * @param interfce
     * @return Class
     * @exception IllegalArgumentException
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException;

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader 
     * and an array of interfaces.
     * 
     * @param interfaces
     * @return Class
     * @exception IllegalArgumentException
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException;

    /**
     * Returns true if and only if the specified class was dynamically generated 
     * to be a proxy class using the getProxyClass method or the newProxyInstance
     * method.
     * 
     * @param cl
     * @return boolean
     */
    public boolean isProxyClass(Class cl);

    /**
     * Returns an instance of a proxy class for the specified interface that 
     * dispatches method invocations to the specified invocation handler.
     * 
     * @param interfce
     * @param h
     * @return Object
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException;

    /**
     * Returns an instance of a proxy class for the specified interface that 
     * dispatches method invocations to the specified invocation handler.
     * 
     * @param interfaces
     * @param h
     * @return Object
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException;

    /**
     * Returns a new proxy instance from the specified proxy class.  The
     * interface(s) implemented by the proxy instance are determined by
     * the proxy class.  The class name may or may not be meaningful,
     * depending on the implementation.
     * 
     * @param proxyClass
     * @return Object
     * @exception java.lang.IllegalArgumentException
     *                   Occurs when the specified class is not a proxy class.
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException;
}

