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
package org.openejb.deployment;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.GenericEJBContainer;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstanceFactory;
import org.openejb.cache.InstancePool;
import org.openejb.deployment.corba.TransactionImportPolicyBuilder;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.proxy.ProxyInfo;
import org.openejb.security.PermissionManager;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicies;
import org.openejb.transaction.TransactionPolicy;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.util.SoftLimitedInstancePool;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
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
    private DefaultPrincipal defaultPrincipal;
    protected Subject runAs;
    private boolean doAsCurrentCaller = false;
    private boolean securityEnabled = false;
    private boolean useContextHandler = false;
    private String policycontextId;
    private Map componentContext;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private UserTransactionImpl userTransaction;
    private TransactionPolicySource transactionPolicySource;
    private TransactionImportPolicyBuilder transactionImportPolicyBuilder;
    private String[] jndiNames;
    private String[] localJndiNames;
    //todo use object names here for build configuration rather than in ModuleBuilder.
    private TransactionContextManager transactionContextManager;
    private TrackedConnectionAssociator trackedConnectionAssociator;

    private AbstractNameQuery transactedTimerName;
    private AbstractNameQuery nonTransactedTimerName;

    //corba tx import


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

    public DefaultPrincipal getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        this.defaultPrincipal = defaultPrincipal;
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

    public void setPolicyContextID(String policyContextID) {
        this.policycontextId = policyContextID;
    }

    public String getPolicycontextId() {
        return policycontextId;
    }

    public Map getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Map componentContext) {
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


    public TransactionImportPolicyBuilder getTransactionImportPolicyBuilder() {
        return transactionImportPolicyBuilder;
    }

    public void setTransactionImportPolicyBuilder(TransactionImportPolicyBuilder transactionImportPolicyBuilder) {
        this.transactionImportPolicyBuilder = transactionImportPolicyBuilder;
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

    public AbstractNameQuery getTransactedTimerName() {
        return transactedTimerName;
    }

    public void setTransactedTimerName(AbstractNameQuery transactedTimerName) {
        this.transactedTimerName = transactedTimerName;
    }

    public AbstractNameQuery getNonTransactedTimerName() {
        return nonTransactedTimerName;
    }

    public void setNonTransactedTimerName(AbstractNameQuery nonTransactedTimerName) {
        this.nonTransactedTimerName = nonTransactedTimerName;
    }

    protected abstract int getEJBComponentType();

    public EJBContainer createContainer() throws Exception {
        return (EJBContainer) buildIt(null);
    }

    public GBeanData createConfiguration(AbstractNameQuery transactionContextManagerObjectName, AbstractNameQuery connectionTrackerObjectName, AbstractNameQuery tssBeanObjectName, GBeanData gbean) throws Exception {
        buildIt(gbean);
//        gbean.setAbstractName(containerObjectName);
        gbean.setReferencePattern("TransactionContextManager", transactionContextManagerObjectName);
        gbean.setReferencePattern("TrackedConnectionAssociator", connectionTrackerObjectName);
        if (tssBeanObjectName != null) {
            gbean.setReferencePattern("TSSBean", tssBeanObjectName);
        }
        return gbean;
    }

    protected abstract Object buildIt(GBeanData gbeanData) throws Exception;

    protected InterceptorBuilder initializeInterceptorBuilder(InterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        interceptorBuilder.setContainerId(containerId);
        interceptorBuilder.setEJBName(ejbName);
        interceptorBuilder.setVtable(vtable);
        interceptorBuilder.setRunAs(runAs);
        interceptorBuilder.setDoAsCurrentCaller(doAsCurrentCaller);
        interceptorBuilder.setSecurityEnabled(securityEnabled);
        interceptorBuilder.setUseContextHandler(useContextHandler);
        interceptorBuilder.setPolicyContextId(policycontextId);
        interceptorBuilder.setTransactionPolicyManager(new TransactionPolicyManager(buildTransactionPolicies(transactionPolicySource, signatures)));
        interceptorBuilder.setPermissionManager(new PermissionManager(ejbName, signatures));
        return interceptorBuilder;
    }

    private TransactionPolicy[][] buildTransactionPolicies(TransactionPolicySource transactionPolicySource, InterfaceMethodSignature[] signatures) {
        TransactionPolicy[][] transactionPolicy = new TransactionPolicy[EJBInterfaceType.MAX_ORDINAL][];
        transactionPolicy[EJBInterfaceType.HOME.getOrdinal()] = mapPolicies("Home", signatures, transactionPolicySource);
        transactionPolicy[EJBInterfaceType.REMOTE.getOrdinal()] = mapPolicies("Remote", signatures, transactionPolicySource);
        transactionPolicy[EJBInterfaceType.LOCALHOME.getOrdinal()] = mapPolicies("LocalHome", signatures, transactionPolicySource);
        transactionPolicy[EJBInterfaceType.LOCAL.getOrdinal()] = mapPolicies("Local", signatures, transactionPolicySource);
        transactionPolicy[EJBInterfaceType.WEB_SERVICE.getOrdinal()] = mapPolicies("ServiceEndpoint", signatures, transactionPolicySource);
        transactionPolicy[EJBInterfaceType.TIMEOUT.getOrdinal()] = new TransactionPolicy[signatures.length];
        Arrays.fill(transactionPolicy[EJBInterfaceType.TIMEOUT.getOrdinal()], ContainerPolicy.Supports); //we control the transaction from the top of the stack.

        return transactionPolicy;
    }

    private static TransactionPolicy[] mapPolicies(String intfName, InterfaceMethodSignature[] signatures, TransactionPolicySource transactionPolicySource) {
        TransactionPolicy[] policies = new TransactionPolicy[signatures.length];
        for (int index = 0; index < signatures.length; index++) {
            InterfaceMethodSignature signature = signatures[index];
            policies[index] = TransactionPolicies.getTransactionPolicy(transactionPolicySource.getTransactionPolicy(intfName, signature));
        }
        return policies;
    }

    protected Serializable getHomeTxPolicyConfig() throws ClassNotFoundException {
        if (transactionImportPolicyBuilder == null) {
            return null;
        }
        ClassLoader classLoader = getClassLoader();
        Class homeInterface = loadOptionalClass(homeInterfaceName, classLoader);
        if (homeInterface == null) {
            return null;
        } else {
            return transactionImportPolicyBuilder.buildTransactionImportPolicy("Home", homeInterface, true, transactionPolicySource, classLoader);
        }
    }

    protected Serializable getRemoteTxPolicyConfig() throws ClassNotFoundException {
        if (transactionImportPolicyBuilder == null) {
            return null;
        }
        ClassLoader classLoader = getClassLoader();
        Class remoteInterface = loadOptionalClass(remoteInterfaceName, classLoader);
        if (remoteInterface == null) {
            return null;
        } else {
            return transactionImportPolicyBuilder.buildTransactionImportPolicy("Remote", remoteInterface, false, transactionPolicySource, classLoader);
        }
    }

    protected ProxyInfo createProxyInfo() throws ClassNotFoundException {
        ClassLoader classLoader = getClassLoader();
        Class homeInterface = loadOptionalClass(homeInterfaceName, classLoader);
        Class remoteInterface = loadOptionalClass(remoteInterfaceName, classLoader);
        Class localHomeInterface = loadOptionalClass(localHomeInterfaceName, classLoader);
        Class localInterface = loadOptionalClass(localInterfaceName, classLoader);
        Class serviceInterface = loadOptionalClass(serviceEndpointName, classLoader);
        Class primaryKeyClass = loadOptionalClass(primaryKeyClassName, classLoader);
        return new ProxyInfo(getEJBComponentType(),
                containerId,
                homeInterface,
                remoteInterface,
                localHomeInterface,
                localInterface,
                serviceInterface,
                primaryKeyClass);
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

    protected EJBContainer createContainer(InterfaceMethodSignature[] signatures,
                                           InstanceContextFactory contextFactory,
                                           InterceptorBuilder interceptorBuilder,
                                           InstancePool pool) throws Exception {

        return new GenericEJBContainer(getContainerId(),
                getEJBName(),
                createProxyInfo(),
                signatures,
                contextFactory,
                interceptorBuilder,
                pool,
                getComponentContext(),
                getUserTransaction(),
                getJndiNames(),
                getLocalJndiNames(),
                getTransactionContextManager(),
                getTrackedConnectionAssociator(),
                null, //timer
                null, //objectname
                null, //kernel
                getDefaultPrincipal(),
                runAs,
                null,
                getHomeTxPolicyConfig(),
                getRemoteTxPolicyConfig(),
                Thread.currentThread().getContextClassLoader());
    }

//    protected GBeanData buildGBeanData() {
//        return new GBeanData(GenericEJBContainer.GBEAN_INFO);
//    }
    
    protected GBeanData createConfiguration(GBeanData gbean, ClassLoader cl, InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool,
            AbstractNameQuery timerName) throws Exception {

//        buildGBeanData();
        gbean.setAttribute("containerID", getContainerId());
        gbean.setAttribute("ejbName", getEJBName());
        gbean.setAttribute("proxyInfo", createProxyInfo());
        gbean.setAttribute("signatures", signatures);
        gbean.setAttribute("contextFactory", contextFactory);
        gbean.setAttribute("interceptorBuilder", interceptorBuilder);
        gbean.setAttribute("pool", pool);
        gbean.setAttribute("componentContext", getComponentContext());
        gbean.setAttribute("userTransaction", getUserTransaction());
        gbean.setAttribute("jndiNames", getJndiNames());
        gbean.setAttribute("localJndiNames", getLocalJndiNames());
        if (timerName != null) {
            gbean.setReferencePattern("Timer", timerName);
        }
        gbean.setAttribute("defaultPrincipal", getDefaultPrincipal());
        gbean.setAttribute("runAsSubject", getRunAs());
        gbean.setAttribute("homeTxPolicyConfig", getHomeTxPolicyConfig());
        gbean.setAttribute("remoteTxPolicyConfig", getRemoteTxPolicyConfig());

        return gbean;
    }

    protected AbstractNameQuery getTimerName(Class beanClass) {
        // use reflection to determine if class implements TimedObject
        // todo remove the reflection code when we adjust the class loaders so deployment sees the same classes as the deployers
        Class timedObjectClass = null;
        try {
            timedObjectClass = beanClass.getClassLoader().loadClass("javax.ejb.TimedObject");
        } catch (ClassNotFoundException e) {
            return null;
        }
        if (!timedObjectClass.isAssignableFrom(beanClass)) {
            return null;
        }

        InterfaceMethodSignature signature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
        TransactionPolicy transactionPolicy = TransactionPolicies.getTransactionPolicy(getTransactionPolicySource().getTransactionPolicy("timeout", signature));
        boolean isTransacted = transactionPolicy == ContainerPolicy.Required || transactionPolicy == ContainerPolicy.RequiresNew;
        if (isTransacted) {
            return getTransactedTimerName();
        } else {
            return getNonTransactedTimerName();
        }
    }

}
