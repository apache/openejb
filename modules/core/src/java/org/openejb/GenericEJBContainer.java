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
package org.openejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.transaction.TransactionManager;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;

import org.openejb.cache.InstancePool;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.EJBObjectProxy;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;

/**
 * @version $Revision$ $Date$
 */
public class GenericEJBContainer implements EJBContainer {
    private final ClassLoader classLoader;
    private final Object containerId;
    private final String ejbName;

    private final Interceptor interceptor;
    private final EJBProxyFactory proxyFactory;

    private final String[] jndiNames;
    private final String[] localJndiNames;

    public GenericEJBContainer(
            Object containerId,
            String ejbName,
            EJBProxyFactory proxyFactory,
            InterfaceMethodSignature[] signatures,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool,
            UserTransactionImpl userTransaction,
            String[] jndiNames,
            String[] localJndiNames,
            TransactionManager transactionManager,
            TrackedConnectionAssociator trackedConnectionAssociator) throws Exception {

        assert (containerId != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (signatures != null);
        assert (interceptorBuilder != null);
        assert (jndiNames != null);
        assert (localJndiNames != null);
        assert (pool != null);
        assert (transactionManager != null);

        this.classLoader = Thread.currentThread().getContextClassLoader();
        assert (classLoader != null);
        this.containerId = containerId;
        this.ejbName = ejbName;
        this.jndiNames = copyNames(jndiNames);
        this.localJndiNames = copyNames(localJndiNames);

        // initialize the proxy factory
        proxyFactory.setContainer(this);
        this.proxyFactory = proxyFactory;

        // build the interceptor chain
        interceptorBuilder.setTransactionManager(transactionManager);
        interceptorBuilder.setTrackedConnectionAssociator(trackedConnectionAssociator);
        interceptorBuilder.setInstancePool(pool);
        interceptor = interceptorBuilder.buildInterceptorChain();

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
        }
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return interceptor.invoke(invocation);
    }

    public Object invoke(Method method, Object[] args, Object primKey) throws Throwable {
        EJBInterfaceType invocationType = null;
        int index = proxyFactory.getMethodIndex(method);

        Class clazz = method.getDeclaringClass();
        if (EJBHome.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.HOME;
        } else if (EJBObject.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.REMOTE;
        } else {
            throw new IllegalArgumentException("Legacy invoke interface only supports remote interfaces: " + clazz);
        }

        // extract the primary key from home ejb remove invocations
        if (invocationType == EJBInterfaceType.HOME && method.getName().equals("remove")) {
            primKey = args[0];
            if (primKey instanceof Handle) {
                Handle handle = (Handle) primKey;
                EJBObjectProxy ejbObject = (EJBObjectProxy) handle.getEJBObject();
                EJBObjectHandler handler = ejbObject.getEJBObjectHandler();
                primKey = handler.getRegistryId();
            }
        }

        EJBInvocationImpl invocation = new EJBInvocationImpl(invocationType, primKey, index, args);

        InvocationResult result = null;
        try {
            result = invoke(invocation);
        } catch (Throwable t) {
            RemoteException re;
            if (t instanceof RemoteException) {
                re = (RemoteException) t;
            } else {
                re = new RemoteException("The bean encountered a non-application exception. method", t);
            }
            throw new InvalidateReferenceException(re);
        }

        if (result.isException()) {
            throw new org.openejb.ApplicationException(result.getException());
        } else {
            return result.getResult();
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Object getContainerID() {
        return containerId;
    }

    public String getEJBName() {
        return ejbName;
    }

    public String[] getJndiNames() {
        return copyNames(jndiNames);
    }

    public String[] getLocalJndiNames() {
        return copyNames(localJndiNames);
    }

    public EJBHome getEJBHome() {
        return proxyFactory.getEJBHome();
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return proxyFactory.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEJBLocalHome() {
        return proxyFactory.getEJBLocalHome();
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return proxyFactory.getEJBLocalObject(primaryKey);
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public EJBContainer getUnmanagedReference(){
        return this;
    }
    
    private static String[] copyNames(String[] names) {
        if(names == null) {
            return null;
        }
        int length = names.length;
        String[] copy = new String[length];
        System.arraycopy(names, 0, copy, 0, length);
        return copy;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GenericEJBContainer.class);

        infoFactory.setConstructor(
                new String[]{"ContainerID", "EJBName", "ProxyFactory", "Signatures", "InterceptorBuilder", "Pool", "UserTransaction", "JndiNames", "LocalJndiNames", "TransactionManager", "TrackedConnectionAssociator"},
                new Class[]{Object.class, String.class, EJBProxyFactory.class, InterfaceMethodSignature[].class, InterceptorBuilder.class, InstancePool.class, UserTransactionImpl.class, String[].class, String[].class, TransactionManager.class, TrackedConnectionAssociator.class});

        infoFactory.addAttribute("ContainerID", true);
        infoFactory.addAttribute("EJBName", true);
        infoFactory.addAttribute("ProxyFactory", true);
        infoFactory.addAttribute("Signatures", true);
        infoFactory.addAttribute("InterceptorBuilder", true);
        infoFactory.addAttribute("Pool", true);
        infoFactory.addAttribute("UserTransaction", true);
        infoFactory.addAttribute("JndiNames", true);
        infoFactory.addAttribute("LocalJndiNames", true);
        infoFactory.addReference("TransactionManager", TransactionManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);

        infoFactory.addAttribute("EJBHome", false);
        infoFactory.addAttribute("EJBLocalHome", false);
        infoFactory.addAttribute("UnmanagedReference", false);
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
