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

import java.util.LinkedHashMap;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.openejb.cache.InstanceFactory;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.openejb.security.PermissionManager;
import org.openejb.transaction.EJBUserTransaction;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.util.SoftLimitedInstancePool;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
public abstract class AbstractContainerBuilder {
    private Object containerId;
    private String ejbName;
    private String beanClassName;
    private String homeInterfaceName;
    private String remoteInterfaceName;
    private String localHomeInterfaceName;
    private String localInterfaceName;
    private String primaryKeyClassName;
    private Subject runAs;
    private ReadOnlyContext componentContext;
    private Set unshareableResources;
    private EJBUserTransaction userTransaction;
    private TransactionPolicySource transactionPolicySource;
    private TransactionManager transactionManager;
    private TrackedConnectionAssociator trackedConnectionAssociator;

    public Object getContainerId() {
        return containerId;
    }

    public void setContainerId(Object containerId) {
        this.containerId = containerId;
    }

    public String getEJBName() {
        return ejbName;
    }

    public void setEJBName(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public void setHomeInterfaceName(String homeInterfaceName) {
        this.homeInterfaceName = homeInterfaceName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String remoteInterfaceName) {
        this.remoteInterfaceName = remoteInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public void setLocalHomeInterfaceName(String localHomeInterfaceName) {
        this.localHomeInterfaceName = localHomeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public void setLocalInterfaceName(String localInterfaceName) {
        this.localInterfaceName = localInterfaceName;
    }

    public String getPrimaryKeyClassName() {
        return primaryKeyClassName;
    }

    public void setPrimaryKeyClassName(String primaryKeyClassName) {
        this.primaryKeyClassName = primaryKeyClassName;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(ReadOnlyContext componentContext) {
        this.componentContext = componentContext;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public void setUnshareableResources(Set unshareableResources) {
        this.unshareableResources = unshareableResources;
    }

    public EJBUserTransaction getUserTransaction() {
        return userTransaction;
    }

    public void setUserTransaction(EJBUserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    public TransactionPolicySource getTransactionPolicySource() {
        return transactionPolicySource;
    }

    public void setTransactionPolicySource(TransactionPolicySource transactionPolicySource) {
        this.transactionPolicySource = transactionPolicySource;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    protected abstract int getEJBComponentType();

    public abstract EJBContainer createContainer() throws Exception;

    protected InterceptorBuilder initializeInterceptorBuilder(InterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        interceptorBuilder.setContainerId(containerId);
        interceptorBuilder.setEJBName(ejbName);
        interceptorBuilder.setVtable(vtable);
        interceptorBuilder.setRunAs(runAs);
        interceptorBuilder.setComponentContext(componentContext);
        interceptorBuilder.setTransactionPolicyManager(new TransactionPolicyManager(transactionPolicySource, signatures));
        interceptorBuilder.setPermissionManager(new PermissionManager(ejbName, signatures));
        interceptorBuilder.setUnshareableResources(unshareableResources);
        return interceptorBuilder;
    }

    protected EJBProxyFactory createProxyFactory(InterfaceMethodSignature[] signatures) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class homeInterface = loadOptionalClass(homeInterfaceName, classLoader);
        Class remoteInterface = loadOptionalClass(remoteInterfaceName, classLoader);
        Class localHomeInterface = loadOptionalClass(localHomeInterfaceName, classLoader);
        Class localInterface = loadOptionalClass(localInterfaceName, classLoader);
        Class primaryKeyClass = loadOptionalClass(primaryKeyClassName, classLoader);
        ProxyInfo proxyInfo = new ProxyInfo(
                getEJBComponentType(),
                containerId,
                homeInterface,
                remoteInterface,
                localHomeInterface,
                localInterface,
                primaryKeyClass);
        return new EJBProxyFactory(proxyInfo, signatures);
    }

    protected SoftLimitedInstancePool createInstancePool(InstanceFactory instanceFactory) {
        return new SoftLimitedInstancePool(instanceFactory, 1);
    }

    protected abstract LinkedHashMap buildVopMap(Class beanClass);

    private static Class loadOptionalClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if(className == null) {
            return null;
        }
        return ClassLoading.loadClass(className, classLoader);
    }
}
