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
package org.openejb.corba;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.rmi.CORBA.Util;
import javax.rmi.PortableRemoteObject;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;
import org.openejb.EJBContainer;
import org.openejb.corba.compiler.IiopOperation;
import org.openejb.corba.compiler.PortableStubCompiler;

/**
 * @version $Revision$ $Date$
 */
public class StandardServant extends Servant implements org.omg.CORBA.portable.InvokeHandler {
    private static final Log log = LogFactory.getLog(StandardServant.class);
    private final EJBContainer ejbContainer;
    private final Object primaryKey;
    private final String[] typeIds;
    private final Map operations;

    public StandardServant(Class type, EJBContainer ejbContainer) {
        this(type, ejbContainer, null);
    }

    public StandardServant(Class type, EJBContainer ejbContainer, Object primaryKey) {
        this.ejbContainer = ejbContainer;
        this.primaryKey = primaryKey;

        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(type);
        Map operations = new HashMap(iiopOperations.length);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            operations.put(iiopOperation.getName(), iiopOperation.getMethod());
        }
        this.operations = Collections.unmodifiableMap(operations);

        List ids = new LinkedList();
        for (Iterator iterator = PortableStubCompiler.getAllInterfaces(type).iterator(); iterator.hasNext();) {
            Class superInterface = (Class) iterator.next();
            if (Remote.class.isAssignableFrom(superInterface) && superInterface != Remote.class) {
                ids.add("RMI:" + superInterface.getName() + ":0000000000000000");
            }
        }
        typeIds = (String[]) ids.toArray(new String[ids.size()]);
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return typeIds;
    }

    public OutputStream _invoke(String operationName, InputStream _in, ResponseHandler reply) throws SystemException {
        try {
            // get the method object
            Method method = (Method) operations.get(operationName);
            if (method == null) {
                throw new BAD_OPERATION();
            }

            org.omg.CORBA_2_3.portable.InputStream in = (org.omg.CORBA_2_3.portable.InputStream) _in;

            // read in all of the arguments
            Object[] args = readArguments(method, in);

            // invoke the method
            Object result = null;
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ejbContainer.getClassLoader());

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                result = ejbContainer.invoke(method, args, primaryKey);
            } catch (TransactionRolledbackException e) {
                log.debug("TransactionRolledbackException", e);
                throw new TRANSACTION_ROLLEDBACK(e.toString());
            } catch (TransactionRequiredException e) {
                log.debug("TransactionRequiredException", e);
                throw new TRANSACTION_REQUIRED(e.toString());
            } catch (InvalidTransactionException e) {
                log.debug("InvalidTransactionException", e);
                throw new INVALID_TRANSACTION(e.toString());
            } catch (NoSuchObjectException e) {
                log.debug("NoSuchObjectException", e);
                throw new OBJECT_NOT_EXIST(e.toString());
            } catch (AccessException e) {
                log.debug("AccessException", e);
                throw new NO_PERMISSION(e.toString());
            } catch (MarshalException e) {
                log.debug("MarshalException", e);
                throw new MARSHAL(e.toString());
            } catch (RemoteException e) {
                log.debug("RemoteException", e);
                throw new UNKNOWN(e.toString());
            } catch (Exception e) {
                // all other exceptions are written to stream
                // if this is an unknown exception type it will
                // be thrown out of writeException
                return writeException(method, reply, e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }


            // creat the output stream
            org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) reply.createReply();

            // write the output value
            writeResult(method.getReturnType(), result, out);

            return out;
        } catch (SystemException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new UnknownException(ex);
        }
    }

    private Object[] readArguments(Method method, org.omg.CORBA_2_3.portable.InputStream in) {
        Class[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            arguments[i] = readArgument(parameterType,  in);
        }
        return arguments;
    }

    private Object readArgument(Class type, org.omg.CORBA_2_3.portable.InputStream in) {
        if (type == boolean.class) {
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
        } else if (type == Object.class) {
            return Util.readAny(in);
        } else if (Remote.class.isAssignableFrom(type)) {
            return PortableRemoteObject.narrow(in.read_Object(), type);
        } else {
            return in.read_value(type);
        }
    }

    private void writeResult(Class type, Object result, org.omg.CORBA_2_3.portable.OutputStream out) {
        if (type == void.class) {
            // do nothing for a void
        } else if (type == boolean.class) {
            out.write_boolean(((Boolean)result).booleanValue());
        } else if (type == byte.class) {
            out.write_octet(((Byte)result).byteValue());
        } else if (type == char.class) {
            out.write_wchar(((Character)result).charValue());
        } else if (type == double.class) {
            out.write_double(((Double)result).doubleValue());
        } else if (type == float.class) {
            out.write_float(((Float)result).floatValue());
        } else if (type == int.class) {
            out.write_long(((Integer)result).intValue());
        } else if (type == long.class) {
            out.write_longlong(((Long)result).longValue());
        } else if (type == short.class) {
            out.write_short(((Short)result).shortValue());
        } else if (type == Object.class) {
            Util.writeAny(out, result);
        } else if (Remote.class.isAssignableFrom(type)) {
            Util.writeRemoteObject(out, result);
        } else {
            out.write_value((Serializable)result, type);
        }
    }

    private OutputStream writeException(Method method, ResponseHandler reply, Exception exception) throws Throwable {
        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];
            if (RemoteException.class.isAssignableFrom(exceptionType) ||
                    RuntimeException.class.isAssignableFrom(exceptionType) ) {
                continue;
            }

            // Determine the exception id
            String exceptionName = exceptionType.getName().replace('.', '/');
            if (exceptionName.endsWith("Exception")) {
                exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
            }
            exceptionName += "Ex";
            String id = "IDL:" + exceptionName + ":1.0";

            org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) reply.createExceptionReply();
            out.write_string(id);
            out.write_value(exception, exceptionType);
            return out;
        }
        throw exception;
    }
}
