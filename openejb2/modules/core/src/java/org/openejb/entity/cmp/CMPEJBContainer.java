/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.entity.cmp;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.J2EEManagedObject;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.EJBContainer;
import org.openejb.GenericEJBContainer;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.corba.TSSBean;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.proxy.ProxyInfo;
import org.tranql.intertxcache.Cache;
import org.tranql.intertxcache.CacheFactory;
import org.tranql.intertxcache.FrontEndCache;
import org.tranql.intertxcache.FrontEndCacheDelegate;
import org.tranql.intertxcache.FrontEndToCacheAdaptor;



/**
 *
 * @version $Revision$ $Date$
 */
public class CMPEJBContainer extends GenericEJBContainer {
    private final TransactionContextManager transactionContextManager;
    private final FrontEndCacheDelegate delegate;
    private final CacheFactory factory;
    
    public CMPEJBContainer(Object containerId, String ejbName, ProxyInfo proxyInfo, InterfaceMethodSignature[] signatures, InstanceContextFactory contextFactory, InterceptorBuilder interceptorBuilder, InstancePool pool, Map componentContext, UserTransactionImpl userTransaction, String[] jndiNames, String[] localJndiNames, TransactionContextManager transactionContextManager, TrackedConnectionAssociator trackedConnectionAssociator, ThreadPooledTimer timer, String objectName, Kernel kernel, DefaultPrincipal defaultPrincipal, Subject runAsSubject, TSSBean tssBean, Serializable homeTxPolicyConfig, Serializable remoteTxPolicyConfig, ClassLoader classLoader, FrontEndCacheDelegate delegate, CacheFactory factory) throws Exception {
        super(containerId, ejbName, proxyInfo, signatures, contextFactory,
                interceptorBuilder, pool, componentContext, userTransaction, jndiNames,
                localJndiNames, transactionContextManager, trackedConnectionAssociator,
                timer, objectName, kernel, defaultPrincipal, runAsSubject, tssBean,
                homeTxPolicyConfig, remoteTxPolicyConfig, classLoader);
        this.transactionContextManager = transactionContextManager;
        this.delegate = delegate;
        this.factory = factory;
    }
    
    public void doStart() throws Exception {
        super.doStart();
        
        TransactionManager tm = transactionContextManager.getTransactionManager();
        
        Cache cache = factory.factory();
        FrontEndCache frontEndCache = new FrontEndToCacheAdaptor(tm, cache);
        delegate.addFrontEndCache(getEjbName(), frontEndCache);
    }
    
    public void doStop() throws Exception {
        super.doStop();
        
        delegate.removeFrontEndCache(getEjbName());
    }
    
    public void doFail() {
        super.doFail();
        
        delegate.removeFrontEndCache(getEjbName());
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(CMPEJBContainer.class);

        infoFactory.addAttribute("containerID", Object.class, true);
        infoFactory.addAttribute("ejbName", String.class, true);
        infoFactory.addAttribute("proxyInfo", ProxyInfo.class, true);
        infoFactory.addAttribute("signatures", InterfaceMethodSignature[].class, true);
        infoFactory.addAttribute("contextFactory", InstanceContextFactory.class, true);
        infoFactory.addAttribute("interceptorBuilder", InterceptorBuilder.class, true);
        infoFactory.addAttribute("pool", InstancePool.class, true);
        infoFactory.addAttribute("componentContext", Map.class, true);
        infoFactory.addAttribute("userTransaction", UserTransactionImpl.class, true);
        infoFactory.addAttribute("jndiNames", String[].class, true);
        infoFactory.addAttribute("localJndiNames", String[].class, true);
        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);
        infoFactory.addAttribute("runAsSubject", Subject.class, true);

        infoFactory.addAttribute("homeTxPolicyConfig", Serializable.class, true);
        infoFactory.addAttribute("remoteTxPolicyConfig", Serializable.class, true);

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoFactory.addReference("Timer", ThreadPooledTimer.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addReference("TSSBean", TSSBean.class);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addInterface(J2EEManagedObject.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);


        infoFactory.addAttribute("ejbHome", EJBHome.class, false);
        infoFactory.addAttribute("ejbLocalHome", EJBLocalHome.class, false);
        infoFactory.addAttribute("unmanagedReference", EJBContainer.class, false);


        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("frontEndCacheDelegate", FrontEndCacheDelegate.class, true);
        infoFactory.addAttribute("cacheFactory", CacheFactory.class, true);
        
        infoFactory.addOperation("getMethodIndex", new Class[]{Method.class});
        infoFactory.addOperation("getEjbObject", new Class[]{Object.class});
        infoFactory.addOperation("getEjbLocalObject", new Class[]{Object.class});

        infoFactory.addOperation("invoke", new Class[]{Invocation.class});
        infoFactory.addOperation("invoke", new Class[]{Method.class, Object[].class, Object.class});

        infoFactory.addOperation("getTimerById", new Class[]{Long.class});

        infoFactory.setConstructor(new String[]{
            "containerID",
            "ejbName",
            "proxyInfo",
            "signatures",
            "contextFactory",
            "interceptorBuilder",
            "pool",
            "componentContext",
            "userTransaction",
            "jndiNames",
            "localJndiNames",
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "Timer",
            "objectName",
            "kernel",
            "defaultPrincipal",
            "runAsSubject",
            "TSSBean",
            "homeTxPolicyConfig",
            "remoteTxPolicyConfig",
            "classLoader",
            "frontEndCacheDelegate",
            "cacheFactory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}