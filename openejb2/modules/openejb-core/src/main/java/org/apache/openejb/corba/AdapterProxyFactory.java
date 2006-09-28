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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.corba;

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;


/**
 * @version $Revision$ $Date$
 */
public class AdapterProxyFactory {

    private final static Log log = LogFactory.getLog(AdapterProxyFactory.class);
    private final static AdapterMethodInterceptor interceptor = new AdapterMethodInterceptor();
    private final Enhancer enhancer;

    public AdapterProxyFactory(Class clientInterface) {
        this(clientInterface, clientInterface.getClassLoader());
    }

    public AdapterProxyFactory(Class clientInterface, ClassLoader classLoader) {
        this(new Class[]{clientInterface}, classLoader);
    }


    public AdapterProxyFactory(Class[] clientInterfaces, ClassLoader classLoader) {
        assert clientInterfaces != null;
        assert areAllInterfaces(clientInterfaces);

        enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(AdapterDelegate.class);
        enhancer.setInterfaces(clientInterfaces);
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setCallbackFilter(FILTER);
        enhancer.setUseFactory(false);
    }

    private static boolean areAllInterfaces(Class[] clientInterfaces) {
        for (int i = 0; i < clientInterfaces.length; i++) {
            if (clientInterfaces[i] == null || !clientInterfaces[i].isInterface()) {
                return false;
            }
        }
        return true;
    }

    public Object create(Remote delegate, ClassLoader classLoader) {
        return create(new Class[]{Remote.class, ClassLoader.class}, new Object[]{delegate, classLoader});
    }

    public synchronized Object create(Class[] types, Object[] arguments) {
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, interceptor});
        return enhancer.create(types, arguments);
    }

    private final static class AdapterMethodInterceptor implements MethodInterceptor {

        public final Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
            try {
                AdapterDelegate delegate = (AdapterDelegate) o;
                Thread.currentThread().setContextClassLoader(delegate.getClassLoader());

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                return methodProxy.invoke(delegate.getDelegate(), args);
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
            } finally {
                Thread.currentThread().setContextClassLoader(savedCL);
            }
        }
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            if (method.getName().equals("finalize") &&
                method.getParameterTypes().length == 0 &&
                method.getReturnType() == Void.TYPE) {
                return 0;
            }
            return 1;
        }
    };
}

