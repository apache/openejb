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

/**
 * Container for the local interface of a Stateless SessionBean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class StatelessLocalClientContainer implements EJBLocalClientContainer {
    private Interceptor firstInterceptor; // @todo make this final
    private final int createIndex;
    private final EJBLocalHome localHomeProxy;
    private final int[] indexMap;
    private final EJBLocalObject fastLocalProxy;

    /**
     * Constructor used to initialize the ClientContainer.
     * @param localHome the class of the EJB's LocalHome interface
     * @param objectMap the mapping from methods on the local interface to the EJB's VirtualOperations
     * @param local the class of the EJB's LocalObject interface
     */
    public StatelessLocalClientContainer(Class localHome, Map objectMap, Class local) {
        FastClass fastClass = FastClass.create(localHome);
        createIndex = fastClass.getIndex("create", new Class[0]);

        SimpleCallbacks callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatelessLocalHomeCallback());
        Enhancer enhancer = getEnhancer(localHome, StatelessLocalHomeImpl.class, callbacks);
        Factory factory = enhancer.create();
        this.localHomeProxy = (EJBLocalHome) factory.newInstance(callbacks);


        fastClass = FastClass.create(local);
        indexMap = new int[fastClass.getMaxIndex() + 1];
        for (Iterator iterator = objectMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method m = (Method) entry.getKey();
            int vopIndex = ((Integer) entry.getValue()).intValue();
            int index = fastClass.getIndex(m.getName(), m.getParameterTypes());
            indexMap[index] = vopIndex;
        }

        callbacks = new SimpleCallbacks();
        callbacks.setCallback(Callbacks.INTERCEPT, new StatelessLocalObjectCallback());

        enhancer = getEnhancer(local, StatelessLocalObjectImpl.class, callbacks);
        factory = enhancer.create(new Class[]{EJBLocalHome.class}, new Object[]{localHomeProxy});
        this.fastLocalProxy = (EJBLocalObject) factory.newInstance(new Class[]{EJBLocalHome.class}, new Object[]{localHomeProxy}, callbacks);
    }

    private static Enhancer getEnhancer(Class local, Class baseClass, SimpleCallbacks callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(baseClass);
        enhancer.setInterfaces(new Class[]{local});
        enhancer.setCallbackFilter(new EJBCallbackFilter(baseClass));
        enhancer.setCallbacks(callbacks);
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

    public EJBLocalHome getEJBLocalHome() {
        return localHomeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return fastLocalProxy;
    }

    /**
     * Base class for EJBLocalHome invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessLocalHomeImpl implements EJBLocalHome {
        public void remove(Object primaryKey) throws RemoveException, EJBException {
            throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
        }
    }

    /**
     * Callback handler for EJBLocalHome invocations that cannot be handled
     * directly by the proxy.
     */
    private class StatelessLocalHomeCallback implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (methodProxy.getIndex() == createIndex) {
                return fastLocalProxy;
            }
            throw new IllegalStateException("Cannot use home method on a Stateless SessionBean");
        }
    }

    /**
     * Base class for EJBLocal invocations. Handles operations which can
     * be performed directly by the proxy.
     */
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

    /**
     * Callback handler for EJBLocal invocations that cannot be handled
     * directly by the proxy.
     */
    private class StatelessLocalObjectCallback implements MethodInterceptor {
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
    }
}
