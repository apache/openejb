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

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
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
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.method.EJBCallbackFilter;

/**
 * Container for the local interface to an EntityBean.
 * This container owns implementation of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class EntityLocalClientContainer implements EJBLocalClientContainer {
    private static final Class[] CONSTRUCTOR = new Class[]{EntityLocalClientContainer.class, Object.class};
    private static final SimpleCallbacks PROXY_CALLBACK;
    static {
        PROXY_CALLBACK = new SimpleCallbacks();
        PROXY_CALLBACK.setCallback(Callbacks.INTERCEPT, new EntityLocalObjectCallback());
    }

    private final Interceptor firstInterceptor;

    private final int[] homeMap;
    private final EJBLocalHome homeProxy;

    private final int removeIndex;
    private final int[] objectMap;
    private final Factory proxyFactory;

    public EntityLocalClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class localHome, Class local) {
        this.firstInterceptor = firstInterceptor;
        SimpleCallbacks callbacks;
        Enhancer enhancer;
        Factory factory;

        // Create LocalHome proxy
        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new EntityLocalHomeCallback());
        enhancer = getEnhancer(localHome, EntityLocalHomeImpl.class, callbacks);
        factory = enhancer.create(new Class[]{EntityLocalClientContainer.class}, new Object[]{this});
        this.homeProxy = (EJBLocalHome) factory.newInstance(new Class[]{EntityLocalClientContainer.class}, new Object[]{this}, callbacks);
        homeMap = MethodHelper.getHomeMap(signatures, FastClass.create(homeProxy.getClass()));

        // Create LocalObject Proxy
        enhancer = getEnhancer(local, EntityLocalObjectImpl.class, PROXY_CALLBACK);
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
    }

    private static Enhancer getEnhancer(Class local, Class baseClass, SimpleCallbacks callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(baseClass);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(baseClass));
        enhancer.setCallbacks(callbacks);
        return enhancer;
    }

    public EJBLocalHome getEJBLocalHome() {
        return homeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return (EJBLocalObject) proxyFactory.newInstance(CONSTRUCTOR, new Object[] { this, primaryKey }, PROXY_CALLBACK);
    }

    private void remove(Object id) throws RemoveException {
        InvocationResult result;
        try {
            EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.LOCAL, id, removeIndex, null);
            result = firstInterceptor.invoke(invocation);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new EJBException(e);
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
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
     * Base class for EJBLocalHome proxies.
     * Owns a reference to the container.
     */
    private abstract static class EntityLocalHomeImpl implements EJBLocalHome {
        private final EntityLocalClientContainer container;

        public EntityLocalHomeImpl(EntityLocalClientContainer container) {
            this.container = container;
        }

        public void remove(Object primaryKey) throws RemoveException, EJBException {
            container.remove(primaryKey);
        }
    }

    /**
     * Callback handler for EJBLocalHome that handles methods not directly
     * implemented by the base class.
     */
    private static class EntityLocalHomeCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            EntityLocalClientContainer container = ((EntityLocalHomeImpl) o).container;
            int vopIndex = container.homeMap[methodProxy.getSuperIndex()];
            return container.invoke(new EJBInvocationImpl(EJBInvocationType.LOCALHOME, vopIndex, objects));
        }
    }

    /**
     * Base class for EJBLocalObject proxies.
     * Owns a reference to the container and the id of the Entity.
     * Implements EJBLocalObject methods, such as getPrimaryKey(), that do
     * not require a trip to the server.
     */
    private abstract static class EntityLocalObjectImpl implements EJBLocalObject {
        private final EntityLocalClientContainer container;
        private final Object id;

        public EntityLocalObjectImpl(EntityLocalClientContainer container, Object id) {
            this.container = container;
            this.id = id;
        }

        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return container.getEJBLocalHome();
        }

        public Object getPrimaryKey() throws EJBException {
            return id;
        }

        public boolean isIdentical(EJBLocalObject obj) throws EJBException {
            if (obj instanceof EntityLocalObjectImpl) {
                EntityLocalObjectImpl other = (EntityLocalObjectImpl) obj;
                return other.container == container && other.id.equals(id);
            }
            return false;
        }

        public void remove() throws RemoveException, EJBException {
            container.remove(id);
        }
    }

    /**
     * Callback handler for EJBLocalObject that handles methods not directly
     * implemented by the base class.
     */
    private static class EntityLocalObjectCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            EntityLocalObjectImpl entityLocalObject = ((EntityLocalObjectImpl)o);
            EntityLocalClientContainer container = entityLocalObject.container;
            int vopIndex = container.objectMap[methodProxy.getSuperIndex()];
            return container.invoke(new EJBInvocationImpl(EJBInvocationType.LOCAL, entityLocalObject.id, vopIndex, args));
        }
    }
}
