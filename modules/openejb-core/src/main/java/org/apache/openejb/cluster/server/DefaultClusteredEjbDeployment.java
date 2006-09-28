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
package org.apache.openejb.cluster.server;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.security.auth.Subject;

import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.proxy.ProxyInfo;


/**
 * 
 * @version $Revision$ $Date$
 */
public class DefaultClusteredEjbDeployment implements ClusteredEjbDeployment {
    private final RpcEjbDeployment container;
    private final ClusteredInstanceCache cache;
    private final ClusteredInstanceContextFactory factory;

    public DefaultClusteredEjbDeployment(RpcEjbDeployment container, ClusteredInstanceCache cache, ClusteredInstanceContextFactory factory) {
        this.container = container;
        this.cache = cache;
        this.factory = factory;
    }

    public ClassLoader getClassLoader() {
        return container.getClassLoader();
    }

    public String getContainerId() {
        return container.getContainerId();
    }

    public Subject getDefaultSubject() {
        return container.getDefaultSubject();
    }

    public EJBHome getEjbHome() {
        return container.getEjbHome();
    }

    public EJBLocalHome getEjbLocalHome() {
        return container.getEjbLocalHome();
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return container.getEjbLocalObject(primaryKey);
    }

    public String getEjbName() {
        return container.getEjbName();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return container.getEjbObject(primaryKey);
    }

    public Serializable getHomeTxPolicyConfig() {
        return container.getHomeTxPolicyConfig();
    }

    public String[] getJndiNames() {
        return container.getJndiNames();
    }

    public String[] getLocalJndiNames() {
        return container.getLocalJndiNames();
    }

    public int getMethodIndex(Method method) {
        return container.getMethodIndex(method);
    }

    public ProxyInfo getProxyInfo() {
        return container.getProxyInfo();
    }

    public Serializable getRemoteTxPolicyConfig() {
        return container.getRemoteTxPolicyConfig();
    }

    public InterfaceMethodSignature[] getSignatures() {
        return container.getSignatures();
    }

    public EjbDeployment getUnmanagedReference() {
        return container.getUnmanagedReference();
    }

    public InvocationResult invoke(Invocation arg0) throws Throwable {
        return container.invoke(arg0);
    }

    public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
        return container.invoke(callMethod, args, primKey);
    }

    public ClusteredInstanceCache getInstanceCache() {
        return cache;
    }

    public ClusteredInstanceContextFactory getInstanceContextFactory() {
        return factory;
    }
}