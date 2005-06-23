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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.Stub;
import javax.rmi.PortableRemoteObject;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.openejb.corba.compiler.IiopOperation;
import org.openejb.corba.compiler.PortableStubCompiler;
import org.openejb.corba.ClientContext;
import org.openejb.corba.ClientContextManager;
import org.openejb.corba.ClientContextHolder;

/**
 * @version $Revision$ $Date$
 */
public class StubMethodInterceptor implements MethodInterceptor {
    private static final Log log = LogFactory.getLog(StubMethodInterceptor.class);
    private final Class type;
    private final Map operations;

    public StubMethodInterceptor(Class type) {
        this.type = type;
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(type);
        Map operations = new HashMap(iiopOperations.length);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            operations.put(iiopOperation.getMethod(), iiopOperation.getName());
        }
        this.operations = Collections.unmodifiableMap(operations);
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Stub stub = ((Stub) proxy);

        // get the operation name object
        String operationName = (String) operations.get(method);
        if (operationName == null) {
            throw new IllegalStateException("Unknown method: " + method);
        }
        ClientContext oldContext = ClientContextManager.getClientContext();

        //first try the stub
        ClientContextHolder holder = (ClientContextHolder) proxy;
        ClientContext context = holder.getClientContext();

        //if stub got deserialized rather than looked up via CSSBean, it might not have the context
        if (context == null) {
            StubDelegateImpl delegate = StubDelegateImpl.getDelegateForStub(stub);
            if (delegate == null) {
                throw new IllegalStateException("No StubDelegateImpl for stub");
            }
            context = delegate.getClientContext();
            //might as well set it for next time
            holder.setClientContext(context);
        }
        try {
            ClientContextManager.setClientContext(context);

            while (true) {
                // if this is a stub to a remote object we invoke over the wire
                if (!Util.isLocal(stub)) {

                    InputStream in = null;
                    try {
                        // create the request output stream
                        OutputStream out = (OutputStream) stub._request(operationName, true);

                        // write the arguments
                        writeArguments(method, args, out);

                        // send the invocation
                        in = (InputStream) stub._invoke(out);

                        // read the result
                        Object result = readResult(method.getReturnType(), in, context);
                        return result;
                    } catch (RemarshalException exception) {
                        continue;
                    } catch (ApplicationException exception) {
                        readException(method, (InputStream) exception.getInputStream());
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

    private static void writeArguments(Method method, Object[] args, OutputStream out) {
        Class[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            writeArgument(parameterType, args[i], out);
        }
    }

    private static void writeArgument(Class type, Object result, OutputStream out) {
        if (type == void.class) {
            // do nothing for a void
        } else if (type == boolean.class) {
            out.write_boolean(((Boolean) result).booleanValue());
        } else if (type == byte.class) {
            out.write_octet(((Byte) result).byteValue());
        } else if (type == char.class) {
            out.write_wchar(((Character) result).charValue());
        } else if (type == double.class) {
            out.write_double(((Double) result).doubleValue());
        } else if (type == float.class) {
            out.write_float(((Float) result).floatValue());
        } else if (type == int.class) {
            out.write_long(((Integer) result).intValue());
        } else if (type == long.class) {
            out.write_longlong(((Long) result).longValue());
        } else if (type == short.class) {
            out.write_short(((Short) result).shortValue());
        } else if (type == Object.class || type == Serializable.class) {
            Util.writeAny(out, result);
        } else if (Remote.class.isAssignableFrom(type)) {
            Util.writeRemoteObject(out, result);
        } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
            out.write_Object((org.omg.CORBA.Object) result);
        } else {
            out.write_value((Serializable) result, type);
        }
    }

    private static Object readResult(Class type, InputStream in, ClientContext context) {
        if (type == void.class) {
            return null;
        } else if (type == boolean.class) {
            return new Boolean(in.read_boolean());
        } else if (type == byte.class) {
            return new Byte(in.read_octet());
        } else if (type == char.class) {
            return new Character(in.read_wchar());
        } else if (type == double.class) {
            return new Double(in.read_double());
        } else if (type == float.class) {
            return new Float(in.read_float());
        } else if (type == int.class) {
            return new Integer(in.read_long());
        } else if (type == long.class) {
            return new Long(in.read_longlong());
        } else if (type == short.class) {
            return new Short(in.read_short());
        } else if (type == Object.class || type == Serializable.class) {
            return Util.readAny(in);
        } else if (Remote.class.isAssignableFrom(type)) {
            Object o = PortableRemoteObject.narrow(in.read_Object(), type);
            if (o instanceof ClientContextHolder) {
                ((ClientContextHolder)o).setClientContext(context);
            }
            return o;
        } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
            return in.read_Object();
        } else {
            return in.read_value(type);
        }
    }

    private void readException(Method method, InputStream in) throws Throwable {
        // read the exception id
        final String id = in.read_string();

        // get the class name from the id
        if (!id.startsWith("IDL:")) {
            log.warn("Malformed exception id: " + id);
            return;
        }

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];
//            if (RemoteException.class.isAssignableFrom(exceptionType) ||
//                    RuntimeException.class.isAssignableFrom(exceptionType) ) {
//                continue;
//            }

            // Determine the exception id
            String exceptionName = exceptionType.getName().replace('.', '/');
            if (exceptionName.endsWith("Exception")) {
                exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
            }
            exceptionName += "Ex";
            String exceptionId = "IDL:" + exceptionName + ":1.0";
            if (id.equals(exceptionId)) {
                throw (Throwable) in.read_value(exceptionType);
            }
        }
        throw new UnexpectedException(id);
    }

//    private static Object copyResult(Class type, Object result, Stub stub) throws RemoteException {
//        if (type == boolean.class) {
//            return result;
//        } else if (type == byte.class) {
//            return result;
//        } else if (type == char.class) {
//            return result;
//        } else if (type == double.class) {
//            return result;
//        } else if (type == float.class) {
//            return result;
//        } else if (type == int.class) {
//            return result;
//        } else if (type == long.class) {
//            return result;
//        } else if (type == short.class) {
//            return result;
//        } else if (Remote.class.isAssignableFrom(type)) {
//            return PortableRemoteObject.narrow(Util.copyObject(result, stub._orb()), type);
//        } else {
//            return Util.copyObject(result, stub._orb());
//        }
//    }
}
