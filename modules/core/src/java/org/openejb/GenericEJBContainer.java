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
    private final ProxyInfo proxyInfo;
    private final EJBProxyFactory proxyFactory;
    private final InterfaceMethodSignature[] signatures;

    private final String[] jndiNames;
    private final String[] localJndiNames;

    public GenericEJBContainer(
            Object containerId,
            String ejbName,
            ProxyInfo proxyInfo,
            InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
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
        this.signatures = signatures;

        // initialize the proxy factory
        this.proxyInfo = proxyInfo;
        this.proxyFactory = new EJBProxyFactory(this);

        // give the contextFactory a reference to the proxyFactory
        // after this there is no reason to hold on to a reference to the contextFactory
        contextFactory.setProxyFactory(proxyFactory);
        contextFactory.setSignatures(getSignatures());

        // build the interceptor chain
        interceptorBuilder.setTransactionManager(transactionManager);
        interceptorBuilder.setTrackedConnectionAssociator(trackedConnectionAssociator);
        interceptorBuilder.setInstancePool(pool);
        TwoChains chains = interceptorBuilder.buildInterceptorChains();
        interceptor = chains.getUserChain();

        contextFactory.setSystemChain(chains.getSystemChain());

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
        }

        // TODO maybe there is a more suitable place to do this.  Maybe not.

        setupJndi();
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

        EJBInvocation invocation = new EJBInvocationImpl(invocationType, primKey, index, args);

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

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public InterfaceMethodSignature[] getSignatures() {
        // return a copy just to be safe... this method should not be called often
        InterfaceMethodSignature[] copy = new InterfaceMethodSignature[signatures.length];
        System.arraycopy(signatures, 0, copy, 0, signatures.length);
        return copy;
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

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null)
            str = ":org.apache.geronimo.naming";
        else
            str = str + ":org.apache.geronimo.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GenericEJBContainer.class);

        infoFactory.addAttribute("ContainerID", Object.class, true);
        infoFactory.addAttribute("EJBName", String.class, true);
        infoFactory.addAttribute("ProxyInfo", ProxyInfo.class, true);
        infoFactory.addAttribute("Signatures", InterfaceMethodSignature[].class, true);
        infoFactory.addAttribute("ContextFactory", InstanceContextFactory.class, true);
        infoFactory.addAttribute("InterceptorBuilder", InterceptorBuilder.class, true);
        infoFactory.addAttribute("Pool", InstancePool.class, true);
        infoFactory.addAttribute("UserTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("JndiNames", String[].class, true);
        infoFactory.addAttribute("LocalJndiNames", String[].class, true);

        infoFactory.addReference("TransactionManager", TransactionManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);

        infoFactory.addAttribute("ProxyFactory", EJBProxyFactory.class, false);
        infoFactory.addAttribute("EJBHome", EJBHome.class, false);
        infoFactory.addAttribute("EJBLocalHome", EJBLocalHome.class, false);
        infoFactory.addAttribute("UnmanagedReference", EJBContainer.class, false);

        infoFactory.setConstructor(new String[]{
            "ContainerID",
            "EJBName",
            "ProxyInfo",
            "Signatures",
            "ContextFactory",
            "InterceptorBuilder",
            "Pool",
            "UserTransaction",
            "JndiNames",
            "LocalJndiNames",
            "TransactionManager",
            "TrackedConnectionAssociator"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
