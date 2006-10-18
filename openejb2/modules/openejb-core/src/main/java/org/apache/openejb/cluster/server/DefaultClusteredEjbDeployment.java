/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cluster.server;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import javax.naming.Context;

import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.timer.BasicTimerServiceImpl;
import org.apache.openejb.transaction.TransactionPolicyManager;
import org.apache.openejb.security.PermissionManager;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
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

    //TODO need to review if the following methods should be here
    public Subject getRunAsSubject() {
        return container.getRunAsSubject();
    }

    public Context getComponentContext() {
        return container.getComponentContext();
    }

    public void logSystemException(Throwable t) {
        container.logSystemException(t);
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        return container.getVirtualOperation(methodIndex);
    }

    public boolean isSecurityEnabled() {
        return container.isSecurityEnabled();
    }

    public String getPolicyContextId() {
        return container.getPolicyContextId();
    }

    public PermissionManager getPermissionManager() {
        return container.getPermissionManager();
    }

    public TransactionPolicyManager getTransactionPolicyManager() {
        return container.getTransactionPolicyManager();
    }

    public Class getBeanClass() {
        return container.getBeanClass();
    }

    public Timer getTimerById(Long id) {
        return container.getTimerById(id);
    }

    public BasicTimerServiceImpl getTimerService() {
        return container.getTimerService();
    }

    public Set getUnshareableResources() {
        return container.getUnshareableResources();
    }

    public Set getApplicationManagedSecurityResources() {
        return container.getApplicationManagedSecurityResources();
    }
}