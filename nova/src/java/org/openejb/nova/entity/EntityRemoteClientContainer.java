/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.entity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBRemoteClientContainer;
import org.openejb.nova.method.EJBInterfaceMethods;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class EntityRemoteClientContainer implements EJBRemoteClientContainer {
    private final Class pkClass;
    private final Class home;
    private final Class remote;
    private final Constructor remoteFactory;
    private final EJBHome homeProxy;
    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;
    private Interceptor firstInterceptor;
    private final Map localMap;
    private final Map localHomeMap;

    public EntityRemoteClientContainer(Class pkClass, Map homeMap, Class home, Map objectMap, Class remote) {
        this.pkClass = pkClass;
        this.home = home;
        this.remote = remote;
        this.homeProxy = (EJBHome) Proxy.newProxyInstance(home.getClassLoader(), new Class[]{home}, new EntityHomeProxy());
        try {
            this.remoteFactory = Proxy.getProxyClass(remote.getClassLoader(), new Class[]{remote}).getConstructor(new Class[]{InvocationHandler.class});
        } catch (Exception e) {
            throw new AssertionError("Unable to locate constructor for Proxy");
        }
        this.ejbMetadata = new EntityMetaData();
        this.homeHandle = null;

        localHomeMap = homeMap;
        localMap = objectMap;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            return;
        }
        Interceptor parent = firstInterceptor;
        while (parent.getNext() != null) {
            parent = parent.getNext();
        }
        parent.setNext(interceptor);
    }

    public EJBHome getEJBHome() {
        return homeProxy;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        try {
            return (EJBObject) remoteFactory.newInstance(new Object[]{new EntityObjectProxy(primaryKey)});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate proxy implementation");
        }
    }

    private class EntityHomeProxy implements InvocationHandler {
        private EntityHomeProxy() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.HOME_GET_EJBMETADATA.equals(method)) {
                return ejbMetadata;
            } else if (EJBInterfaceMethods.HOME_GET_HOMEHANDLE.equals(method)) {
                return homeHandle;
            } else if (EJBInterfaceMethods.HOME_REMOVE_OBJECT.equals(method)) {
                EJBObject object = getEJBObject(args[0]);
                object.remove();
                return null;
            } else if (EJBInterfaceMethods.HOME_REMOVE_HANDLE.equals(method)) {
                throw new UnsupportedOperationException();
            } else {
                Integer index = (Integer) localHomeMap.get(method);
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.HOME, index.intValue(), args);
                return EntityRemoteClientContainer.this.invoke(invocation);
            }
        }
    }

    private class EntityObjectProxy implements InvocationHandler, Serializable {
        private final Object id;

        public EntityObjectProxy(Object id) {
            this.id = id;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.OBJECT_GET_PRIMARYKEY.equals(method)) {
                return id;
            } else if (EJBInterfaceMethods.OBJECT_GET_HOME.equals(method)) {
                return homeProxy;
            } else if (EJBInterfaceMethods.OBJECT_GET_HANDLE.equals(method)) {
                throw new UnsupportedOperationException();
            } else if (EJBInterfaceMethods.OBJECT_ISIDENTICAL.equals(method)) {
                return isIdentical((EJBObject) args[0]);
            } else {
                Integer index = (Integer) localMap.get(method);
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.REMOTE, id, index.intValue(), args);
                return EntityRemoteClientContainer.this.invoke(invocation);
            }
        }

        private Boolean isIdentical(EJBObject other) {
            // get the InvocationHandler backing the Proxy
            InvocationHandler otherHandler;
            try {
                otherHandler = Proxy.getInvocationHandler(other);
            } catch (IllegalArgumentException e) {
                // other object was not a Proxy
                return Boolean.FALSE;
            }

            if (otherHandler instanceof EntityObjectProxy == false) {
                // other Proxy is not an Entity Proxy
                return Boolean.FALSE;
            }
            EntityObjectProxy otherProxy = (EntityObjectProxy) otherHandler;
            return Boolean.valueOf(EntityRemoteClientContainer.this == otherProxy.getContainer() && id.equals(otherProxy.id));
        }

        private EntityRemoteClientContainer getContainer() {
            return EntityRemoteClientContainer.this;
        }
    }

    private Object invoke(EJBInvocation invocation) throws Exception {
        InvocationResult result;
        try {
            result = firstInterceptor.invoke(invocation);
        } catch (RemoteException e) {
            // pass RemoteException's through
            throw e;
        } catch (Throwable t) {
            // System exception from interceptor chain - throw as a RemoteException
            throw new RemoteException(t.getMessage(), t);
        }
        if (result.isNormal()) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }

    private class EntityMetaData implements EJBMetaData, Serializable {
        public EJBHome getEJBHome() {
            return homeProxy;
        }

        public Class getHomeInterfaceClass() {
            return home;
        }

        public Class getRemoteInterfaceClass() {
            return remote;
        }

        public Class getPrimaryKeyClass() {
            return pkClass;
        }

        public boolean isSession() {
            return false;
        }

        public boolean isStatelessSession() {
            return false;
        }
    }
}
