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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import javax.ejb.EJBObject;
import javax.rmi.CORBA.Util;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.openejb.corba.ClientContext;
import org.openejb.corba.ClientContextManager;

/**
 * @version $Revision$ $Date$
 */
public class StubMethodInterceptor implements MethodInterceptor {
    private static final Method ISIDENTICAL;
    static {
        try {
            ISIDENTICAL = EJBObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Class type;
    private final Map operations;

    public StubMethodInterceptor(Class type) {
        this.type = type;
        this.operations = Collections.unmodifiableMap(org.openejb.corba.util.Util.mapMethodToOperation(type));
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        ClientContextHolderStub stub = (ClientContextHolderStub) proxy;

        // handle is identical in stub to avoid unnecessary round trip
        if (method.equals(ISIDENTICAL)) {
            org.omg.CORBA.Object otherObject = (org.omg.CORBA.Object)args[0];
            return new Boolean(stub._is_equivalent(otherObject));
        }

        // get the operation name object
        String operationName = (String) operations.get(method);
        if (operationName == null) {
            throw new IllegalStateException("Unknown method: " + method);
        }
        ClientContext oldContext = ClientContextManager.getClientContext();

        try {
            ClientContextManager.setClientContext(stub.getClientContext());

            while (true) {
                // if this is a stub to a remote object we invoke over the wire
                if (!Util.isLocal(stub)) {

                    InputStream in = null;
                    try {
                        // create the request output stream
                        OutputStream out = (OutputStream) stub._request(operationName, true);

                        // write the arguments
                        Class[] parameterTypes = method.getParameterTypes();
                        for (int i = 0; i < parameterTypes.length; i++) {
                            Class parameterType = parameterTypes[i];
                            org.openejb.corba.util.Util.writeObject(parameterType, args[i], out);
                        }

                        // send the invocation
                        in = (InputStream) stub._invoke(out);

                        // read the result
                        Object result = org.openejb.corba.util.Util.readObject(method.getReturnType(), in);
                        return result;
                    } catch (RemarshalException exception) {
                        continue;
                    } catch (ApplicationException exception) {
                        org.openejb.corba.util.Util.throwException(method, (InputStream) exception.getInputStream());
                    } catch (SystemException e) {
                        throw Util.mapSystemException(e);
                    } finally {
                        stub._releaseReply(in);
                    }
                } else {
                    // get the servant
                    ServantObject servantObject = stub._servant_preinvoke(operationName, type);
                    if (servantObject == null) {
                        continue;
                    }

                    try {
                        // copy the arguments
                        Object[] argsCopy = Util.copyObjects(args, stub._orb());

                        // invoke the servant
                        Object result = null;
                        try {
                            result = method.invoke(servantObject.servant, argsCopy);
                        } catch (InvocationTargetException e) {
                            if (e.getCause() != null) {
                                throw e.getCause();
                            }
                            throw e;
                        }

                        // copy the result
                        result = Util.copyObject(result, stub._orb());

                        return result;
                    } catch (Throwable throwable) {
                        // copy the exception
                        Throwable throwableCopy = (Throwable) Util.copyObject(throwable, stub._orb());

                        // if it is one of my exception rethrow it
                        Class[] exceptionTypes = method.getExceptionTypes();
                        for (int i = 0; i < exceptionTypes.length; i++) {
                            Class exceptionType = exceptionTypes[i];
                            if (exceptionType.isInstance(throwableCopy)) {
                                throw throwableCopy;
                            }
                        }

                        throw Util.wrapException(throwableCopy);
                    } finally {
                        stub._servant_postinvoke(servantObject);
                    }
                }
            }
        } finally {
            ClientContextManager.setClientContext(oldContext);
        }
    }
}
