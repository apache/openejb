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

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.Handle;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.InvocationResult;
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
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.EJBObjectProxy;
import org.openejb.corba.compiler.IiopOperation;
import org.openejb.corba.compiler.PortableStubCompiler;

/**
 * @version $Revision$ $Date$
 */
public class StandardServant extends Servant implements InvokeHandler {
    private static final Log log = LogFactory.getLog(StandardServant.class);
    private final EJBInterfaceType ejbInterfaceType;
    private final EJBContainer ejbContainer;
    private final Object primaryKey;
    private final String[] typeIds;
    private final Map operations;

    public StandardServant(EJBInterfaceType ejbInterfaceType, EJBContainer ejbContainer) {
        this(ejbInterfaceType, ejbContainer, null);
    }

    public StandardServant(EJBInterfaceType ejbInterfaceType, EJBContainer ejbContainer, Object primaryKey) {
        this.ejbInterfaceType = ejbInterfaceType;
        this.ejbContainer = ejbContainer;
        this.primaryKey = primaryKey;

        // get the interface class
        Class type;
        if (EJBInterfaceType.HOME == ejbInterfaceType) {
            type = ejbContainer.getProxyInfo().getHomeInterface();
        } else if (EJBInterfaceType.REMOTE == ejbInterfaceType) {
            type = ejbContainer.getProxyInfo().getRemoteInterface();
        } else {
            throw new IllegalArgumentException("Only home and remote interfaces are supported in this servant: " + ejbInterfaceType);
        }

        // build the operations index
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(type);
        Map operations = new HashMap(iiopOperations.length);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            operations.put(iiopOperation.getName(), iiopOperation.getMethod());
        }
        this.operations = Collections.unmodifiableMap(operations);

        // creat the corba ids array
        List ids = new LinkedList();
        for (Iterator iterator = PortableStubCompiler.getAllInterfaces(type).iterator(); iterator.hasNext();) {
            Class superInterface = (Class) iterator.next();
            if (Remote.class.isAssignableFrom(superInterface) && superInterface != Remote.class) {
                ids.add("RMI:" + superInterface.getName() + ":0000000000000000");
            }
        }
        typeIds = (String[]) ids.toArray(new String[ids.size()]);
    }

    public EJBInterfaceType getEjbInterfaceType() {
        return ejbInterfaceType;
    }

    public EJBContainer getEjbContainer() {
        return ejbContainer;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return typeIds;
    }

    public OutputStream _invoke(String operationName, InputStream _in, ResponseHandler reply) throws SystemException {
        try {
            // get the method object
            Method method = (Method) operations.get(operationName);
            int index = ejbContainer.getMethodIndex(method);
            if (index < 0) {
                throw new BAD_OPERATION(operationName);
            }

            org.omg.CORBA_2_3.portable.InputStream in = (org.omg.CORBA_2_3.portable.InputStream) _in;

            // read in all of the arguments
            Class[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                arguments[i] = org.openejb.corba.util.Util.readObject(parameterType, in);
            }

            // invoke the method
            Object result = null;
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ejbContainer.getClassLoader());

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                // extract the primary key from home ejb remove invocations
                Object primaryKey = this.primaryKey;
                if (ejbInterfaceType == EJBInterfaceType.HOME && method.getName().equals("remove")) {
                    primaryKey = arguments[0];
                    if (primaryKey instanceof Handle) {
                        Handle handle = (Handle) primaryKey;
                        EJBObjectProxy ejbObject = (EJBObjectProxy) handle.getEJBObject();
                        EJBObjectHandler handler = ejbObject.getEJBObjectHandler();
                        primaryKey = handler.getRegistryId();
                    }
                }

                // create the invocation object
                EJBInvocation invocation = new EJBInvocationImpl(ejbInterfaceType, primaryKey, index, arguments);

                // invoke the container
                InvocationResult invocationResult = ejbContainer.invoke(invocation);

                // process the result
                if (invocationResult.isException()) {
                    throw invocationResult.getException();
                }
                result = invocationResult.getResult();
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
                return org.openejb.corba.util.Util.writeException(method, reply, e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }

            // creat the output stream
            org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) reply.createReply();

            // write the output value
            org.openejb.corba.util.Util.writeObject(method.getReturnType(), result, out);

            return out;
        } catch (SystemException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new UnknownException(ex);
        }
    }
}
