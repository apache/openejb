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

import java.util.Set;

import javax.security.auth.Subject;
import javax.management.ObjectName;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.cache.InstanceFactory;
import org.openejb.cache.InstancePool;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.proxy.ProxyInfo;
import org.openejb.security.PermissionManager;
import org.openejb.security.SecurityConfiguration;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.transaction.TransactionPolicy;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.util.SoftLimitedInstancePool;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractContainerBuilder implements ContainerBuilder {
    private ClassLoader classLoader;
    private String containerId;
    private String ejbName;
    private String beanClassName;
    private String homeInterfaceName;
    private String remoteInterfaceName;
    private String localHomeInterfaceName;
    private String localInterfaceName;
    private String serviceEndpointName;
    private String primaryKeyClassName;
    private Subject defaultSubject;
    private Subject runAs;
    private boolean doAsCurrentCaller = false;
    private boolean securityEnabled = false;
    private boolean useContextHandler = false;
    private SecurityConfiguration securityConfiguration;
    private ReadOnlyContext componentContext;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private UserTransactionImpl userTransaction;
    private TransactionPolicySource transactionPolicySource;
    private String[] jndiNames;
    private String[] localJndiNames;
    //todo use object names here for build configuration rather than in ModuleBuilder.
    private TransactionContextManager transactionContextManager;
    private TrackedConnectionAssociator trackedConnectionAssociator;

    private ObjectName transactedTimerName;
    private ObjectName nonTransactedTimerName;

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
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

    public String getServiceEndpointName() {
        return serviceEndpointName;
    }

    public void setServiceEndpointName(String serviceEndpointName) {
        this.serviceEndpointName = serviceEndpointName;
    }

    public String getPrimaryKeyClassName() {
        return primaryKeyClassName;
    }

    public void setPrimaryKeyClassName(String primaryKeyClassName) {
        this.primaryKeyClassName = primaryKeyClassName;
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public void setDefaultSubject(Subject defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public boolean isDoAsCurrentCaller() {
        return doAsCurrentCaller;
    }

    public void setDoAsCurrentCaller(boolean doAsCurrentCaller) {
        this.doAsCurrentCaller = doAsCurrentCaller;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public boolean isUseContextHandler() {
        return useContextHandler;
    }

    public void setUseContextHandler(boolean useContextHandler) {
        this.useContextHandler = useContextHandler;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
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

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public UserTransactionImpl getUserTransaction() {
        return userTransaction;
    }

    public void setUserTransaction(UserTransactionImpl userTransaction) {
        this.userTransaction = userTransaction;
    }

    public TransactionPolicySource getTransactionPolicySource() {
        return transactionPolicySource;
    }

    public void setTransactionPolicySource(TransactionPolicySource transactionPolicySource) {
        this.transactionPolicySource = transactionPolicySource;
    }

    public String[] getJndiNames() {
        return jndiNames;
    }

    public void setJndiNames(String[] jndiNames) {
        this.jndiNames = jndiNames;
    }

    public String[] getLocalJndiNames() {
        return localJndiNames;
    }

    public void setLocalJndiNames(String[] localJndiNames) {
        this.localJndiNames = localJndiNames;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        this.transactionContextManager = transactionContextManager;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public ObjectName getTransactedTimerName() {
        return transactedTimerName;
    }

    public void setTransactedTimerName(ObjectName transactedTimerName) {
        this.transactedTimerName = transactedTimerName;
    }

    public ObjectName getNonTransactedTimerName() {
        return nonTransactedTimerName;
    }

    public void setNonTransactedTimerName(ObjectName nonTransactedTimerName) {
        this.nonTransactedTimerName = nonTransactedTimerName;
    }

    protected abstract int getEJBComponentType();

    public EJBContainer createContainer() throws Exception {
        return (EJBContainer) buildIt(true);
    }

    public GBeanMBean createConfiguration() throws Exception {
        return (GBeanMBean) buildIt(false);
    }

    protected abstract Object buildIt(boolean buildContainer) throws Exception;

    protected InterceptorBuilder initializeInterceptorBuilder(InterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        interceptorBuilder.setContainerId(containerId);
        interceptorBuilder.setEJBName(ejbName);
        interceptorBuilder.setVtable(vtable);
        interceptorBuilder.setRunAs(runAs);
        interceptorBuilder.setDoAsCurrentCaller(doAsCurrentCaller);
        interceptorBuilder.setSecurityEnabled(securityEnabled);
        interceptorBuilder.setUseContextHandler(useContextHandler);
        interceptorBuilder.setComponentContext(componentContext);
        interceptorBuilder.setTransactionPolicyManager(new TransactionPolicyManager(transactionPolicySource, signatures));
        interceptorBuilder.setPermissionManager(new PermissionManager(ejbName, signatures));
        return interceptorBuilder;
    }

    protected ProxyInfo createProxyInfo() throws ClassNotFoundException {
        ClassLoader classLoader = getClassLoader();
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
        return proxyInfo;
    }

    protected SoftLimitedInstancePool createInstancePool(InstanceFactory instanceFactory) {
        return new SoftLimitedInstancePool(instanceFactory, 1);
    }

    private static Class loadOptionalClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }
        return ClassLoading.loadClass(className, classLoader);
    }

    protected EJBContainer createContainer(
            InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool) throws Exception {

        return new GenericEJBContainer(
                getContainerId(),
                getEJBName(),
                createProxyInfo(),
                signatures,
                contextFactory,
                interceptorBuilder,
                pool,
                getUserTransaction(),
                getJndiNames(),
                getLocalJndiNames(),
                getTransactionContextManager(),
                getTrackedConnectionAssociator(),
                null, //timer
                null, //objectname
                null, //kernel
                getSecurityConfiguration(),
                getDefaultSubject());
    }

    protected GBeanMBean createConfiguration(
            ClassLoader cl, InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool,
            ObjectName timerName) throws Exception {

        GBeanMBean gbean = new GBeanMBean(GenericEJBContainer.GBEAN_INFO, cl);
        gbean.setAttribute("ContainerID", getContainerId());
        gbean.setAttribute("EJBName", getEJBName());
        gbean.setAttribute("ProxyInfo", createProxyInfo());
        gbean.setAttribute("Signatures", signatures);
        gbean.setAttribute("ContextFactory", contextFactory);
        gbean.setAttribute("InterceptorBuilder", interceptorBuilder);
        gbean.setAttribute("Pool", pool);
        gbean.setAttribute("UserTransaction", getUserTransaction());
        gbean.setAttribute("JndiNames", getJndiNames());
        gbean.setAttribute("LocalJndiNames", getLocalJndiNames());
        gbean.setReferencePattern("Timer", timerName);
        gbean.setAttribute("SecurityConfiguration", getSecurityConfiguration());
        gbean.setAttribute("DefaultSubject", getDefaultSubject());

        return gbean;
    }

    protected ObjectName getTimerName(Class beanClass) {
        ObjectName timerName = null;
        if (TimedObject.class.isAssignableFrom(beanClass)) {
            InterfaceMethodSignature signature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
            TransactionPolicy transactionPolicy = getTransactionPolicySource().getTransactionPolicy("timeout", signature);
            boolean isTransacted = transactionPolicy == ContainerPolicy.Required || transactionPolicy == ContainerPolicy.RequiresNew;
            if (isTransacted) {
                timerName = getTransactedTimerName();
            } else {
                timerName = getNonTransactedTimerName();
            }
        }
        return timerName;
    }
}
