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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBRemoteClientContainer;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.method.EJBCallbackFilter;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessRemoteClientContainer implements EJBRemoteClientContainer {
    private Interceptor firstInterceptor;
    private final int createIndex;
    private final int[] objectMap;
    private final Class home;
    private final Class remote;
    private final EJBHome homeProxy;
    private final EJBObject objectProxy;
    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;
    private final Handle handle;

    public StatelessRemoteClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class home, Class remote) {
        this.firstInterceptor = firstInterceptor;
        SimpleCallbacks callbacks;
        Enhancer enhancer;
        Factory factory;

        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatelessHomeCallback());
        enhancer = getEnhancer(home, StatelessHomeImpl.class, callbacks);
        factory = enhancer.create(new Class[]{StatelessRemoteClientContainer.class}, new Object[]{null});
        this.homeProxy = (EJBHome) factory.newInstance(new Class[]{StatelessRemoteClientContainer.class}, new Object[]{this}, callbacks);
        createIndex = MethodHelper.getSuperIndex(FastClass.create(homeProxy.getClass()), "create", new Class[0]);
        assert (createIndex != -1) : "No create method defined";

        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatelessObjectCallback());
        enhancer = getEnhancer(remote, StatelessObjectImpl.class, callbacks);
        factory = enhancer.create(new Class[]{StatelessRemoteClientContainer.class}, new Object[]{null});
        this.objectProxy = (EJBObject) factory.newInstance(new Class[]{StatelessRemoteClientContainer.class}, new Object[]{this}, callbacks);
        objectMap = MethodHelper.getObjectMap(signatures, FastClass.create(objectProxy.getClass()));

        this.home = home;
        this.remote = remote;

        this.ejbMetadata = new StatelessMetaData();
        this.homeHandle = null;
        this.handle = null;
    }

    private static Enhancer getEnhancer(Class local, Class baseClass, SimpleCallbacks callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(baseClass);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(baseClass));
        enhancer.setCallbacks(callbacks);
        return enhancer;
    }

    public EJBHome getEJBHome() {
        return homeProxy;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return objectProxy;
    }

    /**
     * Base class for EJBLocalHome invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessHomeImpl implements EJBHome {
        private final StatelessRemoteClientContainer container;

        public StatelessHomeImpl(StatelessRemoteClientContainer container) {
            this.container = container;
        }

        public EJBMetaData getEJBMetaData() throws RemoteException {
            return container.ejbMetadata;
        }

        public HomeHandle getHomeHandle() throws RemoteException {
            return container.homeHandle;
        }

        public void remove(Handle handle) throws RemoteException, RemoveException {
            throw new RemoteException("Cannot use remove(Handle) on a Stateless SessionBean");
        }

        public void remove(Object primaryKey) throws RemoveException {
            throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
        }
    }

    /**
     * Callback handler for EJBLocalHome invocations that cannot be handled
     * directly by the proxy.
     */
    private class StatelessHomeCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (methodProxy.getSuperIndex() == createIndex) {
                return objectProxy;
            }
            throw new IllegalStateException("Cannot use home method on a Stateless SessionBean");
        }
    }

    /**
     * Base class for EJBLocal invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessObjectImpl implements EJBObject {
        private StatelessRemoteClientContainer container;

        public StatelessObjectImpl(StatelessRemoteClientContainer container) {
            this.container = container;
        }

        public EJBHome getEJBHome() {
            return container.homeProxy;
        }

        public boolean isIdentical(EJBObject obj) {
            return obj == this;
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateless SessionBean");
        }

        public Handle getHandle() throws RemoteException {
            return container.handle;
        }

        public void remove() throws RemoveException {
        }
    }

    /**
     * Callback handler for EJBLocal invocations that cannot be handled
     * directly by the proxy.
     */
    private class StatelessObjectCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            InvocationResult result;
            try {
                int vopIndex = objectMap[methodProxy.getSuperIndex()];
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.REMOTE, vopIndex, args);
                result = firstInterceptor.invoke(invocation);
            } catch (Throwable t) {
                // System Exception from interceptor chain
                // Wrap checked Exceptions in an EJBException, otherwise just throw
                if (t instanceof Exception && t instanceof RuntimeException == false) {
                    t = new EJBException((Exception) t);
                }
                throw t;
            }
            if (result.isNormal()) {
                return result.getResult();
            } else {
                throw result.getException();
            }
        }
    }

    private class StatelessMetaData implements EJBMetaData {
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
