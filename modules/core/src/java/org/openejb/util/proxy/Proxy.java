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


import java.lang.reflect.Method;


/**
 * Superclass for dynamically generated proxies.
 *
 * This class implements convenience methods that allow us to
 * generate proxies with considerably less byte-code.
 *
 * @author Richard Monson-Haefel
 * @author David Blevins
 */
public abstract class Proxy implements java.io.Serializable {

    public InvocationHandler handler;

    /**
     *
     * @return The implementation of InvocationHandler handling invocations for this Proxy object.
     */
    public InvocationHandler getInvocationHandler() {
        return handler;
    }

    public InvocationHandler setInvocationHandler(InvocationHandler newHandler) {
        InvocationHandler oldHandler = handler;
        handler = newHandler;
        return oldHandler;
    }

    /**
     * Used as the Class array in the Class.getMethod( String methodName, Class[] argTypes )
     * method when the interface method does not define any arguments.
     */
    protected static final Class[] NO_ARGS_C = new Class[0];

    /**
     * Used as the Object array in the Method.invoke( Object obj, Object[] args )
     * method when the interface method does not define any arguments.
     */
    protected static final Object[] NO_ARGS_O = new Object[0];

    protected final void _proxyMethod$throws_default$returns_void(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        _proxyMethod$throws_default$returns_Object( methodNumber, methodName, argTypes, args);
        return;
    }

    protected final Object _proxyMethod$throws_default$returns_Object(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        java.lang.reflect.Method method = _proxyMethod$lookupMethod( methodNumber, methodName, argTypes);
        try{
            return handler.invoke(this,method,args);
        }catch(Throwable t){
            // rethrow exceptions
            if(t instanceof java.rmi.RemoteException)
                     throw (java.rmi.RemoteException)t;
            if(t instanceof java.lang.RuntimeException)
                     throw (java.lang.RuntimeException)t;
            else throw _proxyError$(t);
        }
    }

    protected final void _proxyMethod$throws_AppException$returns_void(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        _proxyMethod$throws_AppException$returns_Object( methodNumber, methodName, argTypes, args);
        return;
    }

    protected final Object _proxyMethod$throws_AppException$returns_Object(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException , org.openejb.ApplicationException{
        java.lang.reflect.Method method = _proxyMethod$lookupMethod( methodNumber, methodName, argTypes);
        try{
            return handler.invoke(this,method,args);
        }catch(Throwable t){
            // rethrow exceptions
            if(t instanceof java.rmi.RemoteException)
                     throw (java.rmi.RemoteException)t;
            if(t instanceof java.lang.RuntimeException)
                     throw (java.lang.RuntimeException)t;
            if(t instanceof org.openejb.ApplicationException)
                     throw (org.openejb.ApplicationException)t;
            else throw _proxyError$(t);

        }
    }

    //=============================================================================
    // Methods that return primitives and only throw the default remote exception
    //

    protected final int _proxyMethod$throws_default$returns_int(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Integer retval = (Integer)_proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.intValue();
    }

    protected final double _proxyMethod$throws_default$returns_double(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Double retval = (Double) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.doubleValue();
    }


    protected final long _proxyMethod$throws_default$returns_long(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Long retval = (Long) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.longValue();
    }


    protected final boolean _proxyMethod$throws_default$returns_boolean(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Boolean retval = (Boolean) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.booleanValue();
    }


    protected final float _proxyMethod$throws_default$returns_float(int methodNumber,String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Float retval = (Float) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.floatValue();
    }


    protected final char _proxyMethod$throws_default$returns_char(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Character retval = (Character) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.charValue();
    }


    protected final byte _proxyMethod$throws_default$returns_byte(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Byte retval = (Byte) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.byteValue();
    }


    protected final short _proxyMethod$throws_default$returns_short(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Short retval = (Short) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.shortValue();
    }

    //
    // Methods that return primitives and only throw the default remote exception
    //=============================================================================


    //===========================================================
    // Methods that return primitives and throw an AppException
    //

    protected final int _proxyMethod$throws_AppException$returns_int(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Integer retval = (Integer)_proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.intValue();
    }

    protected final double _proxyMethod$throws_AppException$returns_double(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Double retval = (Double) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.doubleValue();
    }


    protected final long _proxyMethod$throws_AppException$returns_long(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Long retval = (Long) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.longValue();
    }


    protected final boolean _proxyMethod$throws_AppException$returns_boolean(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Boolean retval = (Boolean) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.booleanValue();
    }


    protected final float _proxyMethod$throws_AppException$returns_float(int methodNumber,String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Float retval = (Float) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.floatValue();
    }


    protected final char _proxyMethod$throws_AppException$returns_char(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Character retval = (Character) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.charValue();
    }


    protected final byte _proxyMethod$throws_AppException$returns_byte(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Byte retval = (Byte) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.byteValue();
    }


    protected final short _proxyMethod$throws_AppException$returns_short(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.openejb.ApplicationException {
        Short retval = (Short) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.shortValue();
    }

    //
    // Methods that return primitives and throw an AppException
    //===========================================================

    protected abstract Method _proxyMethod$lookupMethod(int index, String methodName, Class[] argTypes);

    protected final Method _proxyMethod$lookupMethod(Class interfce, Method [] methodMap, int index, String methodName, Class[] argTypes){
        // obtain method
        java.lang.reflect.Method method = methodMap[index];
        if(method == null){
            try{ // Lazily create the method.
                method = interfce.getMethod( methodName, argTypes );
                methodMap[index] = method;
            }catch(NoSuchMethodException nsme){ throw new RuntimeException("Method not found:  " +nsme.getMessage());}
        }
        return method;
    }

    protected final java.rmi.RemoteException _proxyError$(Throwable throwable){
        return new java.rmi.RemoteException("[OpenEJB]  Proxy Error: ",throwable );
    }

    protected final java.rmi.RemoteException _proxyError$(org.openejb.ApplicationException ae){
        return new java.rmi.RemoteException("[OpenEJB]  Proxy Error: The returned application exception is not defined in the throws clause.  ", ae.getRootCause());
    }

}
