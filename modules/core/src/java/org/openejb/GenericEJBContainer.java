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
import java.security.Permissions;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.Timer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.jacc.RoleMappingConfigurationFactory;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.cache.InstancePool;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.EJBObjectProxy;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.openejb.security.SecurityConfiguration;
import org.openejb.timer.BasicTimerServiceImpl;


/**
 * @version $Revision$ $Date$
 */
public class GenericEJBContainer implements EJBContainer, GBeanLifecycle {

    private static Log log = LogFactory.getLog(GenericEJBContainer.class);

    private final ClassLoader classLoader;
    private final Object containerId;
    private final String ejbName;

    private final Interceptor interceptor;
    private final ProxyInfo proxyInfo;
    private final EJBProxyFactory proxyFactory;
    private final InterfaceMethodSignature[] signatures;

    private final String[] jndiNames;
    private final String[] localJndiNames;

    private final SecurityConfiguration securityConfiguration;
    private transient PolicyConfiguration policyConfiguration;
    private transient Subject defaultSubject;
    private final BasicTimerServiceImpl timerService;


    public GenericEJBContainer(Object containerId,
                               String ejbName,
                               ProxyInfo proxyInfo,
                               InterfaceMethodSignature[] signatures,
                               InstanceContextFactory contextFactory,
                               InterceptorBuilder interceptorBuilder,
                               InstancePool pool,
                               Map componentContext,
                               UserTransactionImpl userTransaction,
                               String[] jndiNames,
                               String[] localJndiNames,
                               TransactionContextManager transactionContextManager,
                               TrackedConnectionAssociator trackedConnectionAssociator,
                               ThreadPooledTimer timer,
                               String objectName,
                               Kernel kernel,
                               SecurityConfiguration securityConfiguration,
                               Subject defaultSubject,
                               ClassLoader classLoader) throws Exception {

        assert (containerId != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (componentContext != null);
        assert (signatures != null);
        assert (interceptorBuilder != null);
        assert (jndiNames != null);
        assert (localJndiNames != null);
        assert (pool != null);
        assert (transactionContextManager != null);

        this.classLoader = classLoader;
        assert (classLoader != null);
        this.containerId = containerId;
        this.ejbName = ejbName;
        this.jndiNames = copyNames(jndiNames);
        this.localJndiNames = copyNames(localJndiNames);
        this.signatures = signatures;

        // initialize the proxy factory
        this.proxyInfo = proxyInfo;
        this.proxyFactory = new EJBProxyFactory(this);

        // create ReadOnlyContext
        Context enc = null;
        if (componentContext != null) {
            for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
                Object value = iterator.next();
                if (value instanceof KernelAwareReference) {
                    ((KernelAwareReference) value).setKernel(kernel);
                }
                if (value instanceof ClassLoaderAwareReference) {
                    ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
                }
            }
            enc = new SimpleReadOnlyContext(componentContext);
        }
        interceptorBuilder.setComponentContext(enc);

        // give the contextFactory a reference to the proxyFactory
        // after this there is no reason to hold on to a reference to the contextFactory
        contextFactory.setProxyFactory(proxyFactory);
        SystemMethodIndices systemMethodIndices = contextFactory.setSignatures(getSignatures());

        // build the interceptor chain
        interceptorBuilder.setTransactionContextManager(transactionContextManager);
        interceptorBuilder.setTrackedConnectionAssociator(trackedConnectionAssociator);
        interceptorBuilder.setInstancePool(pool);
        TwoChains chains = interceptorBuilder.buildInterceptorChains();
        if (defaultSubject != null) {
            interceptor = new DefaultSubjectInterceptor(chains.getUserChain());
        } else {
            interceptor = chains.getUserChain();
        }

        contextFactory.setSystemChain(chains.getSystemChain());
        contextFactory.setTransactionContextManager(transactionContextManager);
        if (timer != null) {
            timerService = new BasicTimerServiceImpl(systemMethodIndices, interceptor, timer, objectName, kernel.getKernelName(), ObjectName.getInstance(objectName), transactionContextManager, classLoader);
            contextFactory.setTimerService(timerService);
        } else {
            timerService = null;
        }

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionContextManager, trackedConnectionAssociator);
        }

        this.securityConfiguration = securityConfiguration;
        this.defaultSubject = defaultSubject;

        // TODO maybe there is a more suitable place to do this.  Maybe not.

        setupJndi();
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return interceptor.invoke(invocation);
    }

    public Object invoke(Method method, Object[] args, Object primKey) throws Throwable {
        EJBInterfaceType invocationType = null;
        int index = proxyFactory.getMethodIndex(method);

        Class serviceEndpointInterface = this.getProxyInfo().getServiceEndpointInterface();

        Class clazz = method.getDeclaringClass();
        if (EJBHome.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.HOME;
        } else if (EJBObject.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.REMOTE;
        } else if (serviceEndpointInterface != null && serviceEndpointInterface.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.WEB_SERVICE;
        } else {
            throw new IllegalArgumentException("Legacy invoke interface only supports remote and service-endpoint interfaces: " + clazz);
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

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public int getMethodIndex(Method method) {
        return proxyFactory.getMethodIndex(method);
    }

    public InterfaceMethodSignature[] getSignatures() {
        // return a copy just to be safe... this method should not be called often
        InterfaceMethodSignature[] copy = new InterfaceMethodSignature[signatures.length];
        System.arraycopy(signatures, 0, copy, 0, signatures.length);
        return copy;
    }

    public EJBContainer getUnmanagedReference() {
        return this;
    }

    private static String[] copyNames(String[] names) {
        if (names == null) {
            return null;
        }
        int length = names.length;
        String[] copy = new String[length];
        System.arraycopy(names, 0, copy, 0, length);
        return copy;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public Timer getTimerById(Long id) {
        assert timerService != null;
        return timerService.getTimerById(id);
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            if (str.indexOf(":org.apache.geronimo.naming") < 0) {
                str = str + ":org.apache.geronimo.naming";
            }
        }
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }

    public void doStart() throws Exception {

        if (timerService != null) {
            timerService.doStart();
        }

        if (defaultSubject != null) ContextManager.registerSubject(defaultSubject);

        if (securityConfiguration != null) {
            /**
             * Get the JACC policy configuration that's associated with this
             * EJB container and configure it with the geronimo security
             * configuration.  The work for this is done by the class
             * JettyXMLConfiguration.
             */
            try {
                PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

                policyConfiguration = factory.getPolicyConfiguration(securityConfiguration.getPolicyContextId(), true);

                policyConfiguration.addToExcludedPolicy(securityConfiguration.getExcludedPolicy());
                policyConfiguration.addToUncheckedPolicy(securityConfiguration.getUncheckedPolicy());
                Iterator roles = securityConfiguration.getRolePolicies().keySet().iterator();
                while (roles.hasNext()) {
                    String role = (String) roles.next();

                    policyConfiguration.addToRole(role, (Permissions) securityConfiguration.getRolePolicies().get(role));
                }

                RoleMappingConfiguration roleMapper = RoleMappingConfigurationFactory.getRoleMappingFactory().getRoleMappingConfiguration(securityConfiguration.getPolicyContextId(), true);
                if (roleMapper != null) {
                    Iterator iter = securityConfiguration.getRoleMapping().keySet().iterator();
                    while (iter.hasNext()) {
                        String roleName = (String) iter.next();
                        Set principalSet = (Set) securityConfiguration.getRoleMapping().get(roleName);
                        roleMapper.addRoleMapping(roleName, principalSet);
                    }
                }


                policyConfiguration.commit();
            } catch (ClassNotFoundException e) {
                // do nothing
            } catch (PolicyContextException e) {
                // do nothing
            } catch (GeronimoSecurityException e) {
                // do nothing
            }
            log.debug("Using JACC policy '" + securityConfiguration.getPolicyContextId() + "'");
        }
        log.info("GenericEJBContainer '" + containerId + "'started");
    }

    public void doStop() throws Exception {
        if (timerService != null) {
            timerService.doStop();
        }

        if (defaultSubject != null) ContextManager.unregisterSubject(defaultSubject);

        if (this.securityConfiguration != null) {
            /**
             * Delete the policy configuration for this web application
             */
            if (policyConfiguration != null) policyConfiguration.delete();

        }
        log.info("GenericEJBContainer '" + containerId + "' stopped");
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //todo fix this
            throw new RuntimeException(e);
        }

        try {
            if (policyConfiguration != null) policyConfiguration.delete();
        } catch (PolicyContextException e) {
            // do nothing
        }

        log.info("GenericEJBContainer '" + containerId + "'failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GenericEJBContainer.class); //we don't know the j2eeType

        infoFactory.addAttribute("ContainerID", Object.class, true);
        infoFactory.addAttribute("EJBName", String.class, true);
        infoFactory.addAttribute("ProxyInfo", ProxyInfo.class, true);
        infoFactory.addAttribute("Signatures", InterfaceMethodSignature[].class, true);
        infoFactory.addAttribute("ContextFactory", InstanceContextFactory.class, true);
        infoFactory.addAttribute("InterceptorBuilder", InterceptorBuilder.class, true);
        infoFactory.addAttribute("Pool", InstancePool.class, true);
        infoFactory.addAttribute("componentContext", Map.class, true);
        infoFactory.addAttribute("UserTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("JndiNames", String[].class, true);
        infoFactory.addAttribute("LocalJndiNames", String[].class, true);

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);
        infoFactory.addReference("Timer", ThreadPooledTimer.class);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);


        infoFactory.addAttribute("ejbHome", EJBHome.class, false);
        infoFactory.addAttribute("ejbLocalHome", EJBLocalHome.class, false);
        infoFactory.addAttribute("unmanagedReference", EJBContainer.class, false);

        infoFactory.addAttribute("SecurityConfiguration", SecurityConfiguration.class, true);
        infoFactory.addAttribute("DefaultSubject", Subject.class, true);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addOperation("getMethodIndex", new Class[] {Method.class});
        infoFactory.addOperation("getEJBObject", new Class[] {Object.class});
        infoFactory.addOperation("getEJBLocalObject", new Class[] {Object.class});

        infoFactory.addOperation("invoke", new Class[]{Invocation.class});
        infoFactory.addOperation("invoke", new Class[]{Method.class, Object[].class, Object.class});

        infoFactory.addOperation("getTimerById", new Class[]{Long.class});

        infoFactory.setConstructor(new String[]{
            "ContainerID",
            "EJBName",
            "ProxyInfo",
            "Signatures",
            "ContextFactory",
            "InterceptorBuilder",
            "Pool",
            "componentContext",
            "UserTransaction",
            "JndiNames",
            "LocalJndiNames",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "Timer",
            "objectName",
            "kernel",
            "SecurityConfiguration",
            "DefaultSubject",
            "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private final class DefaultSubjectInterceptor implements Interceptor {

        private final Interceptor interceptor;

        public DefaultSubjectInterceptor(Interceptor interceptor) {
            this.interceptor = interceptor;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            boolean clearCurrentCaller = false;

            if (ContextManager.getCurrentCaller() == null) {
                ContextManager.setCurrentCaller(defaultSubject);
                ContextManager.setNextCaller(defaultSubject);
                clearCurrentCaller = true;
            }
            try {
                return interceptor.invoke(invocation);
            } finally {
                if (clearCurrentCaller) {
                    ContextManager.setCurrentCaller(null);
                    ContextManager.setNextCaller(null);
                }
            }
        }
    }
}
