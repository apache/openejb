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
package org.openejb.nova.sfsb;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

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
public class StatefulRemoteClientContainer implements EJBRemoteClientContainer {
    private static final Class[] CONSTRUCTOR = new Class[]{StatefulRemoteClientContainer.class, Object.class};
    private static final SimpleCallbacks PROXY_CALLBACK;

    static {
        PROXY_CALLBACK = new SimpleCallbacks();
        PROXY_CALLBACK.setCallback(Callbacks.INTERCEPT, new StatefulObjectCallback());
    }

    private Interceptor firstInterceptor;

    private final Class home;
    private final Class remote;

    private final Factory proxyFactory;

    private final int[] homeMap;
    private final EJBHome homeProxy;

    private final int removeIndex;
    private final int[] objectMap;

    private final EJBMetaData ejbMetadata;

    public StatefulRemoteClientContainer(MethodSignature[] signatures, Class home, Class remote) {
        this.home = home;
        this.remote = remote;

        SimpleCallbacks callbacks;
        Enhancer enhancer;
        Factory factory;

        // Create LocalHome proxy
        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatefulHomeCallback());
        enhancer = getEnhancer(home, StatefulHomeImpl.class, callbacks);
        factory = enhancer.create(new Class[]{StatefulRemoteClientContainer.class}, new Object[]{this});
        homeProxy = (EJBHome) factory.newInstance(new Class[]{StatefulRemoteClientContainer.class}, new Object[]{this}, callbacks);
        homeMap = MethodHelper.getHomeMap(signatures, FastClass.create(homeProxy.getClass()));

        // Create LocalObject Proxy
        enhancer = getEnhancer(remote, StatefulObjectImpl.class, PROXY_CALLBACK);
        proxyFactory = enhancer.create(CONSTRUCTOR, new Object[]{this, null});
        objectMap = MethodHelper.getObjectMap(signatures, FastClass.create(proxyFactory.getClass()));

        // Get VOP index for ejbRemove method
        int index = -1;
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            if ("ejbRemove".equals(signature.getMethodName())) {
                index = i;
                break;
            }
        }
        assert (index != -1) : "No ejbRemove VOP defined";
        removeIndex = index;

        ejbMetadata = new StatefulMetaData();
    }

    private static Enhancer getEnhancer(Class local, Class baseClass, SimpleCallbacks callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(baseClass);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(baseClass));
        enhancer.setCallbacks(callbacks);
        enhancer.setClassLoader(local.getClassLoader());
        return enhancer;
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
        return (EJBObject) proxyFactory.newInstance(CONSTRUCTOR, new Object[]{this, primaryKey}, PROXY_CALLBACK);
    }

    private void remove(Object id) throws RemoveException, RemoteException {
        InvocationResult result;
        try {
            result = firstInterceptor.invoke(new EJBInvocationImpl(EJBInvocationType.REMOTE, id, removeIndex, null));
        } catch (RemoteException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemoteException(t.getMessage(), t);
        }
        if (result.isException()) {
            throw (RemoveException) result.getException();
        }
    }

    private Object invoke(EJBInvocation invocation) throws Throwable {
        InvocationResult result;
        try {
            result = firstInterceptor.invoke(invocation);
        } catch (Throwable t) {
            // System exception from interceptor chain - throw as is or wrapped in an EJBException
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

    /**
     * Base class for EJBHome proxies.
     * Owns a reference to the container.
     */
    private abstract static class StatefulHomeImpl implements EJBHome {
        private final StatefulRemoteClientContainer container;

        public StatefulHomeImpl(StatefulRemoteClientContainer container) {
            this.container = container;
        }

        public EJBMetaData getEJBMetaData() throws RemoteException {
            return container.ejbMetadata;
        }

        public HomeHandle getHomeHandle() throws RemoteException {
            // @todo We need to support this...
            throw new UnsupportedOperationException();
        }

        public void remove(Handle handle) throws RemoteException, RemoveException {
            // @todo We need to support this...
            throw new UnsupportedOperationException();
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateful SessionBean");
        }
    }

    /**
     * Callback handler for EJBHome that handles methods not directly
     * implemented by the base class.
     */
    private static class StatefulHomeCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            StatefulRemoteClientContainer container = ((StatefulHomeImpl) o).container;
            int vopIndex = container.homeMap[methodProxy.getSuperIndex()];
            return container.invoke(new EJBInvocationImpl(EJBInvocationType.HOME, vopIndex, objects));
        }
    }

    /**
     * Base class for EJBLocalObject proxies.
     * Owns a reference to the container and the id of the Stateful.
     * Implements EJBLocalObject methods, such as getPrimaryKey(), that do
     * not require a trip to the server.
     */
    private abstract static class StatefulObjectImpl implements EJBObject {
        private final StatefulRemoteClientContainer container;
        private final Object id;

        public StatefulObjectImpl(StatefulRemoteClientContainer container, Object id) {
            this.container = container;
            this.id = id;
        }

        public EJBHome getEJBHome() throws EJBException {
            return container.getEJBHome();
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateful SessionBean");
        }

        public boolean isIdentical(EJBObject obj) throws EJBException {
            if (obj instanceof StatefulObjectImpl) {
                StatefulObjectImpl other = (StatefulObjectImpl) obj;
                return other.container == container && other.id.equals(id);
            }
            return false;
        }

        public Handle getHandle() throws RemoteException {
            throw new UnsupportedOperationException();
        }

        public void remove() throws RemoveException, RemoteException {
            container.remove(id);
        }
    }

    /**
     * Callback handler for EJBLocalObject that handles methods not directly
     * implemented by the base class.
     */
    private static class StatefulObjectCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            StatefulObjectImpl statefulObject = ((StatefulObjectImpl) o);
            StatefulRemoteClientContainer container = statefulObject.container;
            int vopIndex = container.objectMap[methodProxy.getSuperIndex()];
            return container.invoke(new EJBInvocationImpl(EJBInvocationType.REMOTE, statefulObject.id, vopIndex, args));
        }
    }

    private class StatefulMetaData implements EJBMetaData, Serializable {
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
            throw new EJBException("Cannot use getPrimaryKey() on a Stateful SessionBean");
        }

        public boolean isSession() {
            return true;
        }

        public boolean isStatelessSession() {
            return false;
        }
    }
}
