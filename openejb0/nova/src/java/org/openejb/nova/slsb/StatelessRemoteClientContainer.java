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
package org.openejb.nova.slsb;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

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
public class StatelessRemoteClientContainer implements EJBRemoteClientContainer {
    private final Class home;
    private final Class remote;
    private final EJBHome homeProxy;
    private final EJBObject remoteProxy;
    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;
    private final Handle handle;
    private Interceptor firstInterceptor;
    private final Map remoteMap;

    public StatelessRemoteClientContainer(Class home, Map objectMap, Class remote) {
        this.home = home;
        this.remote = remote;
        this.homeProxy = (EJBHome) Proxy.newProxyInstance(home.getClassLoader(), new Class[]{home}, new StatelessHomeProxy());
        this.remoteProxy = (EJBObject) Proxy.newProxyInstance(remote.getClassLoader(), new Class[]{remote}, new StatelessObjectProxy());

        this.ejbMetadata = new StatelessMetaData();
        this.homeHandle = null;
        this.handle = null;

        remoteMap = objectMap;
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
        return remoteProxy;
    }

    private class StatelessHomeProxy implements InvocationHandler {
        private final Method ejbCreate;

        private StatelessHomeProxy() {
            try {
                ejbCreate = home.getMethod("create", null);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("home class " + home.getName() + " does not have a create() method");
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ejbCreate.equals(method)) {
                return remoteProxy;
            } else if (EJBInterfaceMethods.HOME_GET_EJBMETADATA.equals(method)) {
                return ejbMetadata;
            } else if (EJBInterfaceMethods.HOME_GET_HOMEHANDLE.equals(method)) {
                return homeHandle;
            } else if (EJBInterfaceMethods.HOME_REMOVE_OBJECT.equals(method)) {
                throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
            } else if (EJBInterfaceMethods.HOME_REMOVE_HANDLE.equals(method)) {
                throw new RemoteException("Cannot use remove(Handle) on a Stateless SessionBean");
            } else {
                throw new IllegalStateException("Cannot use home method on a Stateless SessionBean");
            }
        }
    }

    private class StatelessObjectProxy implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.OBJECT_GET_HOME.equals(method)) {
                return homeProxy;
            } else if (EJBInterfaceMethods.OBJECT_GET_HANDLE.equals(method)) {
                return handle;
            } else if (EJBInterfaceMethods.OBJECT_REMOVE.equals(method)) {
                return null;
            } else if (EJBInterfaceMethods.OBJECT_ISIDENTICAL.equals(method)) {
                EJBObject other = (EJBObject) args[0];
                return Boolean.valueOf(other == proxy); //@todo this relies on this Proxy being a Singleton
            } else if (EJBInterfaceMethods.OBJECT_GET_PRIMARYKEY.equals(method)) {
                throw new RemoteException("Cannot use getPrimaryKey() on a Stateless SessionBean");
            } else {
                InvocationResult result;
                try {
                    Integer index = (Integer) remoteMap.get(method);
                    EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInvocationType.REMOTE, index.intValue(), args);
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
        }
    }

    private class StatelessMetaData implements EJBMetaData, Serializable {
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
            throw new IllegalStateException("getPrimaryKeyClass is not supported for SessionBean");
        }

        public boolean isSession() {
            return true;
        }

        public boolean isStatelessSession() {
            return true;
        }
    }
}
