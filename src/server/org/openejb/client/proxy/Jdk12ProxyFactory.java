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
package org.openejb.client.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.opentools.proxies.Proxy;

/**
 * ProxyFactory for JDK 1.2.
 * 
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class Jdk12ProxyFactory implements ProxyFactory {

    /**
     * Paramaters for the contstructor of the proxy class.
     */
    private final static Class[] constructorParams = { org.opentools.proxies.InvocationHandler.class};
    
    /**
     * Prepare the factory for use.  Called once when the ProxyFactory 
     * is instaniated.
     * 
     * @param props
     */
    public void init(Properties props) {}

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        
        Jdk12InvocationHandler handler = (Jdk12InvocationHandler)Proxy.getInvocationHandler(proxy);
        
        if ( handler == null ) return null;
        
        return handler.getInvocationHandler();
    }

    /**
     * Sets the invocation handler for the specified proxy instance.
     */
    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException {
        
        Jdk12InvocationHandler jdk12 = (Jdk12InvocationHandler)Proxy.getInvocationHandler(proxy);
        
        if ( jdk12 == null ) throw new IllegalArgumentException("Proxy "+proxy+" unknown!");
        
        return jdk12.setInvocationHandler(handler);
    }

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader
     * and an array of interfaces.
     * 
     * @param interfce
     * @return 
     * @exception IllegalArgumentException
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException {
        return Proxy.getProxyClass(interfce.getClassLoader(), new Class[]{interfce});
    }
    
    /**
     * Returns the java.lang.Class object for a proxy class given a class loader 
     * and an array of interfaces.
     * 
     * @param interfaces
     * @return 
     * @exception IllegalArgumentException
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException {
        if ( interfaces.length < 1 ) {
            throw new IllegalArgumentException("There must be at least one interface to implement.");
        }
        return Proxy.getProxyClass(interfaces[0].getClassLoader(), interfaces);
    }

    /**
     * Returns true if and only if the specified class was dynamically generated 
     * to be a proxy class using the getProxyClass method or the newProxyInstance
     * method.
     * 
     * @param cl
     * @return 
     */
    public boolean isProxyClass(Class cl) {
        return Proxy.isProxyClass(cl);
    }


    /**
     * Creates a new proxy instance using the handler of the proxy passed in.
     * 
     * @param proxyClass
     * @return 
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException {
        
        if ( !Proxy.isProxyClass(proxyClass) ) {
            throw new IllegalArgumentException("This class is not a proxy.");
        }
        
        try {
            
            Constructor cons = proxyClass.getConstructor(constructorParams);
            return cons.newInstance( new Object[] { new Jdk12InvocationHandler()} );
        
        } catch ( NoSuchMethodException e ) {
            throw new InternalError(e.toString());
        } catch ( IllegalAccessException e ) {
            throw new InternalError(e.toString());
        } catch ( InstantiationException e ) {
            throw new InternalError(e.toString());
        } catch ( InvocationTargetException e ) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Returns an instance of a proxy class for the specified interface that 
     * dispatches method invocations to the specified invocation handler.
     * 
     * @param interfce
     * @param h
     * @return 
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException {
        
        Jdk12InvocationHandler handler = new Jdk12InvocationHandler(h);
        
        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, handler);
    }

    /**
     * Returns an instance of a proxy class for the specified interface that 
     * dispatches method invocations to the specified invocation handler.
     * 
     * @param interfaces
     * @param h
     * @return 
     * @exception IllegalArgumentException
     */
    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        if ( interfaces.length < 1 ) {
            throw new IllegalArgumentException("There must be at least one interface to implement.");
        }
        
        Jdk12InvocationHandler handler = new Jdk12InvocationHandler(h);
        
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
    }
}