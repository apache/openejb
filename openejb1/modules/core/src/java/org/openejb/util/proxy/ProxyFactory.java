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

/**
 *  EXAMPLE USAGE
 *
 *
 * ProxyManager.getProxyClass( Customer.class );
 *
 *
 *
 *  CUSTOMER INTERFACE
 *
 * public interface Customer extends javax.ejb.EJBObject {
 *
 *     public boolean setAddress(String street, int x, short y) throws RemoteException;
 *
 *
 *
 *  GENERATED PROXY CLASS
 *
 * public class CustomerProxy extends Proxy implements java.io.Serializable,Customer{
 *
 *     protected static transient java.lang.reflect.Method [] methodMap = new java.lang.reflect.Method[6];
 *
 *     protected CustomerProxy(){}
 *
 *     ...// EJBObject methods
 *
 *     public boolean setAddress( java.lang.String parm0,int parm1,short parm2) throws java.rmi.RemoteException{
 *         // obtain method
 *         java.lang.reflect.Method method = methodMap[5];
 *         if(method == null){
 *             try{
 *                 method=Customer.class.getMethod("setAddress",new Class [] { java.lang.String.class,int.class,short.class});
 *                 methodMap[5] = method;
 *             }catch(NoSuchMethodException nsme){ throw new RuntimeException();}
 *         }
 *         // package arguments
 *         Object [] args = new Object[3];
 *         args[0] = parm0;
 *         args[1] = new java.lang.Integer(parm1);
 *         args[2] = new java.lang.Short(parm2);
 *
 *         try{
 *             java.lang.Boolean retval = (java.lang.Boolean)handler.invoke(this,method,args);
 *             return retval.booleanValue( );
 *         }catch(Throwable t){
 *             // rethrow exceptions
 *             if(t instanceof java.rmi.RemoteException)
 *                 throw (java.rmi.RemoteException)t;
 *             if(t instanceof RuntimeException)
 *                 throw (RuntimeException)t;
 *             else
 *                 throw (Error)t;
 *         }
 *     }
 *
 * @author David Blevins
 * @author Richard Monson-Haefel
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */

import java.util.Properties;

import org.openejb.OpenEJBException;

public interface ProxyFactory {

    public void init(Properties props) throws OpenEJBException;

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException;

    /**
     * Sets the invocation handler for the specified proxy instance.
     */
    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException;

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException;

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException;

    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl);

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException;

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException;

    /**
    * Returns a new proxy instance from the specified proxy class.  The
    * interface(s) implemented by the proxy instance are determined by
    * the proxy class.  The class name may or may not be meaningful,
    * depending on the implementation.
    * @throws java.lang.IllegalArgumentException
    *     Occurs when the specified class is not a proxy class.
    */
    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException;
}

