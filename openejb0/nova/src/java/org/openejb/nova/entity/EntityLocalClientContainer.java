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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.method.EJBInterfaceMethods;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class EntityLocalClientContainer implements EJBLocalClientContainer {
    private final Constructor remoteFactory;
    private final EJBLocalHome homeProxy;
    private Interceptor firstInterceptor;
    private final Map localMap;
    private final Map localHomeMap;

    public EntityLocalClientContainer(Map localHomeMap, Class localHome, Map objectMap, Class local) {
        this.homeProxy = (EJBLocalHome) Proxy.newProxyInstance(localHome.getClassLoader(), new Class[]{localHome}, new EntityHomeProxy());
        try {
            this.remoteFactory = Proxy.getProxyClass(local.getClassLoader(), new Class[]{local}).getConstructor(new Class[]{InvocationHandler.class});
        } catch (Exception e) {
            throw new AssertionError("Unable to locate constructor for Proxy");
        }

        this.localHomeMap = localHomeMap;
        this.localMap = objectMap;
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
        return homeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        try {
            return (EJBLocalObject) remoteFactory.newInstance(new Object[]{new EntityObjectProxy(primaryKey)});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate proxy implementation");
        }
    }

    private class EntityHomeProxy implements InvocationHandler {
        private EntityHomeProxy() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.LOCALHOME_REMOVE_OBJECT.equals(method)) {
                EJBLocalObject object = getEJBLocalObject(args[0]);
                object.remove();
                return null;
            } else {
                Integer index = (Integer) localHomeMap.get(method);
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.LOCALHOME, index.intValue(), args);
                return EntityLocalClientContainer.this.invoke(invocation);
            }
        }
    }

    private class EntityObjectProxy implements InvocationHandler {
        private final Object id;

        public EntityObjectProxy(Object id) {
            this.id = id;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EJBInterfaceMethods.LOCALOBJECT_GET_PRIMARYKEY.equals(method)) {
                return id;
            } else if (EJBInterfaceMethods.LOCALOBJECT_GET_LOCALHOME.equals(method)) {
                return homeProxy;
            } else if (EJBInterfaceMethods.LOCALOBJECT_ISIDENTICAL.equals(method)) {
                return isIdentical((EJBLocalObject) args[0]);
            } else {
                Integer index = (Integer) localMap.get(method);
                EJBInvocation invocation = new EJBInvocationImpl(EJBInvocationType.LOCAL, id, index.intValue(), args);
                return EntityLocalClientContainer.this.invoke(invocation);
            }
        }

        private Boolean isIdentical(EJBLocalObject other) {
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
            return Boolean.valueOf(EntityLocalClientContainer.this == otherProxy.getContainer() && id.equals(otherProxy.id));
        }

        private EntityLocalClientContainer getContainer() {
            return EntityLocalClientContainer.this;
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
}
