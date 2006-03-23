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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.EJBTimeoutOperation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.mdb.BusinessMethod;
import org.openejb.mdb.MDBContainer;
import org.openejb.mdb.MDBInstanceContextFactory;
import org.openejb.mdb.MDBInstanceFactory;
import org.openejb.mdb.MDBInterceptorBuilder;
import org.openejb.mdb.dispatch.SetMessageDrivenContextOperation;
import org.openejb.slsb.EJBCreateMethod;
import org.openejb.slsb.RemoveMethod;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.openejb.util.SoftLimitedInstancePool;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * @version $Revision$ $Date$
 */
public class MDBContainerBuilder implements ResourceEnvironmentBuilder, SecureBuilder {
    private static final MethodSignature SET_MESSAGE_DRIVEN_CONTEXT = new MethodSignature("setMessageDrivenContext", new String[]{"javax.ejb.MessageDrivenContext"});

    private String containerId;
    private String ejbName;
    private AbstractName activationSpecName;
    private String beanClassName;
    private String endpointInterfaceName;
    private Subject runAs;
    private boolean doAsCurrentCaller = false;
    private boolean securityEnabled = false;
    private boolean useContextHandler = false;
    private String policyContextID;
    private Map componentContext;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private UserTransactionImpl userTransaction;
    private TransactionPolicySource transactionPolicySource;
    private ClassLoader classLoader;
    private AbstractNameQuery transactedTimerName;
    private AbstractNameQuery nonTransactedTimerName;


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

    public AbstractName getActivationSpecName() {
        return activationSpecName;
    }

    public void setActivationSpecName(AbstractName activationSpecName) {
        this.activationSpecName = activationSpecName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getEndpointInterfaceName() {
        return endpointInterfaceName;
    }

    public void setEndpointInterfaceName(String endpointInterfaceName) {
        this.endpointInterfaceName = endpointInterfaceName;
    }

    public DefaultPrincipal getDefaultPrincipal() {
        return null;  // RETURN NOTHING
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        // DO NOTHING
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
        this.policyContextID = policyContextID;
    }

    public String getPolicyContextID() {
        return policyContextID;
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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

    // todo this created GBeanDatas without and object name, which is a bad idea
    public GBeanData createConfiguration() throws Exception {
        // get the bean class
        Class beanClass = classLoader.loadClass(beanClassName);

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // build the instance factory
        MDBInstanceContextFactory contextFactory = new MDBInstanceContextFactory(containerId, beanClass, userTransaction, unshareableResources, applicationManagedSecurityResources);
        MDBInstanceFactory instanceFactory = new MDBInstanceFactory(contextFactory);

        // build the pool
        InstancePool pool = new SoftLimitedInstancePool(instanceFactory, 1);

        // create and intitalize the interceptor moduleBuilder
        MDBInterceptorBuilder interceptorBuilder = new MDBInterceptorBuilder();
        interceptorBuilder.setEJBName(ejbName);
        interceptorBuilder.setVtable(vtable);
        interceptorBuilder.setRunAs(runAs);
        interceptorBuilder.setInstancePool(pool);

        boolean[] deliveryTransacted = new boolean[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            TransactionPolicyType transactionPolicyType = transactionPolicySource.getTransactionPolicy("local", signature);
            deliveryTransacted[i] = transactionPolicyType == TransactionPolicyType.Required;
        }

        AbstractNameQuery timerName = null;
        if (TimedObject.class.isAssignableFrom(beanClass)) {
            InterfaceMethodSignature signature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
            TransactionPolicyType transactionPolicy = transactionPolicySource.getTransactionPolicy("timeout", signature);
            Boolean isTransacted = (Boolean) isTransactedMap[transactionPolicy.getIndex()];
            if (isTransacted != null) {
                if (isTransacted.booleanValue()) {
                    timerName = transactedTimerName;
                } else {
                    timerName = nonTransactedTimerName;
                }
            } else {
                //bean managed tx , so it could do stuff in a tx.
                timerName = transactedTimerName;
            }
        }

        // create and initialize the GBean
        GBeanData gbean = new GBeanData(MDBContainer.GBEAN_INFO);
        gbean.setAttribute("containerId", containerId);
        gbean.setAttribute("ejbName", ejbName);
//        gbean.setReferencePattern("ActivationSpecWrapper", activationSpecName);
        gbean.setAttribute("endpointInterfaceName", endpointInterfaceName);
        gbean.setAttribute("signatures", signatures);
        gbean.setAttribute("deliveryTransacted", deliveryTransacted);
        gbean.setAttribute("contextFactory", contextFactory);
        gbean.setAttribute("interceptorBuilder", interceptorBuilder);
        gbean.setAttribute("componentContext", getComponentContext());
        gbean.setAttribute("instancePool", pool);
        gbean.setAttribute("userTransaction", userTransaction);
        if (timerName != null) {
            //todo configid really not required?
            gbean.setReferencePattern("Timer", timerName);
        }
        return gbean;
    }

    private static Boolean[] isTransactedMap = new Boolean[TransactionPolicyType.size()];

    static {
        isTransactedMap[TransactionPolicyType.Mandatory.getIndex()] = Boolean.TRUE;//this won't work, of course
        isTransactedMap[TransactionPolicyType.Never.getIndex()] = Boolean.FALSE;
        isTransactedMap[TransactionPolicyType.NotSupported.getIndex()] = Boolean.FALSE;
        isTransactedMap[TransactionPolicyType.Required.getIndex()] = Boolean.TRUE;
        isTransactedMap[TransactionPolicyType.RequiresNew.getIndex()] = Boolean.TRUE;
        isTransactedMap[TransactionPolicyType.Supports.getIndex()] = Boolean.FALSE;
    }

    protected LinkedHashMap buildVopMap(Class beanClass) throws Exception {
        LinkedHashMap vopMap = new LinkedHashMap();

        // ejbCreate... this is the method called by the pool to create a new instance
        vopMap.put(new InterfaceMethodSignature("ejbCreate", false), new EJBCreateMethod(beanClass));
        // ejbRemove... this is the method called by the pool to destroy an instance
        vopMap.put(new InterfaceMethodSignature("ejbRemove", false), new RemoveMethod(beanClass));
        // ejbTimeout
        if (TimedObject.class.isAssignableFrom(beanClass)) {
            vopMap.put(new InterfaceMethodSignature("ejbTimeout", new String[]{Timer.class.getName()}, false),
                EJBTimeoutOperation.INSTANCE);
        }

        // add the business methods
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }

            if (beanMethod.getName().startsWith("ejb")) {
                continue;
            }

            // match set message driven context down here since it can not be easily ignored like ejb* methods
            MethodSignature signature = new MethodSignature(beanMethod);
            if (SET_MESSAGE_DRIVEN_CONTEXT.equals(signature)) {
                vopMap.put(new InterfaceMethodSignature("setMessageDrivenContext", new String[]{"javax.ejb.MessageDrivenContext"}, false),
                        SetMessageDrivenContextOperation.INSTANCE);
            } else {
                vopMap.put(new InterfaceMethodSignature(signature, false),
                        new BusinessMethod(beanClass, signature));
            }
        }

        return vopMap;
    }

}
