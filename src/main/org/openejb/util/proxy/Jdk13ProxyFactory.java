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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.openejb.OpenEJBException;

/**
 * Implementation of ProxyFactory for JDK 1.3 Proxies.  This only
 * compiles on JDK 1.3 or better.  It is very fast because it builds
 * the proxies out of raw bytecode.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class Jdk13ProxyFactory implements ProxyFactory {
    public void init(Properties props) throws OpenEJBException {
        String version = "";
        String badVersion = "1.3.0-";
        try{
            version = System.getProperty("java.vm.version");
        } catch(Exception e){
        }
        if (version.indexOf(badVersion) != -1) {
            String message = ""+
                "INCOMPATIBLE VM: \n\n"+
                "The Java Virtual Machine you are using contains a bug\n"+
                "in the proxy generation logic.  This bug has been    \n"+
                "documented by Sun and has been fixed in later VMs.   \n"+
                "Please download the latest 1.3 Virtual Machine.      \n"+
                "For more details see:                                    \n"+
                "http://developer.java.sun.com/developer/bugParade/bugs/4346224.html\n  ";
            throw new OpenEJBException(message);
        }
    }

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public org.openejb.util.proxy.InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        Jdk13InvocationHandler handler = (Jdk13InvocationHandler)Proxy.getInvocationHandler(proxy);
        if(handler == null)
            return null;
        return handler.getInvocationHandler();
    }

    /**
     * Sets the invocation handler for the specified proxy instance.
     */
    public Object setInvocationHandler(Object proxy, org.openejb.util.proxy.InvocationHandler handler) throws IllegalArgumentException {
        Jdk13InvocationHandler jdk13 = (Jdk13InvocationHandler)Proxy.getInvocationHandler(proxy);
        if(jdk13 == null)
            throw new IllegalArgumentException("Proxy "+proxy+" unknown!");
        return jdk13.setInvocationHandler(handler);
    }

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException {
        return Proxy.getProxyClass(interfce.getClassLoader(), new Class[]{interfce});
    }
    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException {
        if(interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        return Proxy.getProxyClass(interfaces[0].getClassLoader(), interfaces);
    }


    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl) {
        return Proxy.isProxyClass(cl);
    }

    private final static Class[] constructorParams = { java.lang.reflect.InvocationHandler.class };

    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException {
        if(!Proxy.isProxyClass(proxyClass))
            throw new IllegalArgumentException();
        try {
            Constructor cons = proxyClass.getConstructor(constructorParams);
            return (Object) cons.newInstance(new Object[] { new Jdk13InvocationHandler() });
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, org.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);
        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, handler);
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, org.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        if(interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
    }
}

