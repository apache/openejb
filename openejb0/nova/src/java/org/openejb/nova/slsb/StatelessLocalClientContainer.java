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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
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
import org.openejb.nova.method.EJBCallbackFilter;
import org.openejb.nova.method.EJBInterfaceMethods;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessLocalClientContainer implements EJBLocalClientContainer {
    private final Class localHome;
    private final EJBLocalHome localHomeProxy;
    private final EJBLocalObject localProxy;
    private Interceptor firstInterceptor;
    private final Map localMap;
    private final int[] indexMap;
    private final EJBLocalObject fastLocalProxy;

    public StatelessLocalClientContainer(Class localHome, Map objectMap, Class local) {
        this.localHome = localHome;
        localMap = objectMap;
        this.localHomeProxy = (EJBLocalHome) Proxy.newProxyInstance(localHome.getClassLoader(), new Class[]{localHome}, new StatelessLocalHomeProxy());
        this.localProxy = (EJBLocalObject) Proxy.newProxyInstance(local.getClassLoader(), new Class[]{local}, new StatelessLocalObjectCallback());

        FastClass fastClass = FastClass.create(local);
        indexMap = new int[fastClass.getMaxIndex() + 1];
        for (Iterator iterator = objectMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method m = (Method) entry.getKey();
            int vopIndex = ((Integer) entry.getValue()).intValue();
            int index = fastClass.getIndex(m.getName(), m.getParameterTypes());
            indexMap[index] = vopIndex;
        }

        SimpleCallbacks callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatelessLocalObjectCallback());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StatelessLocalObjectImpl.class);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(StatelessLocalObjectImpl.class));
        enhancer.setCallbacks(callbacks);
        Factory factory = enhancer.create(new Class[]{EJBLocalHome.class}, new Object[]{localHomeProxy});
        this.fastLocalProxy = (EJBLocalObject) factory.newInstance(new Class[]{EJBLocalHome.class}, new Object[]{localHomeProxy}, callbacks);
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

    public EJBLocalHome getEJBLocalHome() {
        return localHomeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return fastLocalProxy;
    }

    private class StatelessLocalHomeProxy implements InvocationHandler {
        private final Method ejbCreate;

        private StatelessLocalHomeProxy() {
            try {
                ejbCreate = localHome.getMethod("create", null);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("localhome class " + localHome.getName() + " does not have a create() method");
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ejbCreate.equals(method)) {
                return fastLocalProxy;
            } else if (EJBInterfaceMethods.LOCALHOME_REMOVE_OBJECT.equals(method)) {
                throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
            } else {
                throw new IllegalStateException("Cannot use home method on a Stateless SessionBean");
            }
        }
    }

    public static abstract class StatelessLocalObjectImpl implements EJBLocalObject {
        private final EJBLocalHome localHomeProxy;

        public StatelessLocalObjectImpl(EJBLocalHome localHomeProxy) {
            this.localHomeProxy = localHomeProxy;
        }

        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return localHomeProxy;
        }

        public boolean isIdentical(EJBLocalObject obj) throws EJBException {
            return obj == this;
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateless SessionBean");
        }

        public void remove() throws RemoveException, EJBException {
        }
    }

    private class StatelessLocalObjectCallback implements InvocationHandler, MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            InvocationResult result;
            try {
                int vopIndex = indexMap[methodProxy.getIndex()];
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.LOCAL, vopIndex, args);
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

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.LOCALOBJECT_GET_LOCALHOME.equals(method)) {
                return localHomeProxy;
            } else if (EJBInterfaceMethods.LOCALOBJECT_REMOVE.equals(method)) {
                return null;
            } else if (EJBInterfaceMethods.LOCALOBJECT_ISIDENTICAL.equals(method)) {
                EJBLocalObject other = (EJBLocalObject) args[0];
                return Boolean.valueOf(other == proxy); //@todo this relies on this Proxy being a Singleton
            } else if (EJBInterfaceMethods.LOCALOBJECT_GET_PRIMARYKEY.equals(method)) {
                throw new EJBException("Cannot use getPrimaryKey() on a Stateless SessionBean");
            } else {
                InvocationResult result;
                try {
                    Integer index = (Integer) localMap.get(method);
                    EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.LOCAL, index.intValue(), args);
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
    }

}
