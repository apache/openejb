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
package org.openejb.mdb;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.openejb.ResourceEnvironmentBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.slsb.CreateMethod;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;
import org.openejb.util.SoftLimitedInstancePool;

/**
 * @version $Revision$ $Date$
 */
public class MDBContainerBuilder implements ResourceEnvironmentBuilder {
    private String containerId;
    private String ejbName;
    private ObjectName activationSpecName;
    private String beanClassName;
    private String endpointInterfaceName;
    private Subject runAs;
    private ReadOnlyContext componentContext;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private UserTransactionImpl userTransaction;
    private TransactionPolicySource transactionPolicySource;
    private ClassLoader classLoader;

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

    public ObjectName getActivationSpecName() {
        return activationSpecName;
    }

    public void setActivationSpecName(ObjectName activationSpecName) {
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

    public GBeanMBean createConfiguration() throws Exception {
        // get the bean class
        Class beanClass = classLoader.loadClass(beanClassName);

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // build the instance factory
        MDBInstanceContextFactory contextFactory = new MDBInstanceContextFactory(containerId, beanClass, userTransaction, unshareableResources, applicationManagedSecurityResources);
        MDBInstanceFactory instanceFactory = new MDBInstanceFactory(componentContext, contextFactory, beanClass);

        // build the pool
        InstancePool pool = new SoftLimitedInstancePool(instanceFactory, 1);

        // create and intitalize the interceptor builder
        MDBInterceptorBuilder interceptorBuilder = new MDBInterceptorBuilder();
        interceptorBuilder.setEJBName(ejbName);
        interceptorBuilder.setVtable(vtable);
        interceptorBuilder.setRunAs(runAs);
        interceptorBuilder.setComponentContext(componentContext);
        interceptorBuilder.setInstancePool(pool);

        boolean[] deliveryTransacted = new boolean[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            TransactionPolicy transactionPolicy = transactionPolicySource.getTransactionPolicy("local", signature);
            deliveryTransacted[i] = transactionPolicy == ContainerPolicy.Required;
        }

        // create and initialize the GBean
        GBeanMBean gbean = new GBeanMBean(MDBContainer.GBEAN_INFO, classLoader);
        gbean.setAttribute("containerId", containerId);
        gbean.setAttribute("ejbName", ejbName);
        gbean.setReferencePattern("activationSpecWrapper", activationSpecName);
        gbean.setAttribute("endpointInterfaceName", endpointInterfaceName);
        gbean.setAttribute("signatures", signatures);
        gbean.setAttribute("deliveryTransacted", deliveryTransacted);
        gbean.setAttribute("interceptorBuilder", interceptorBuilder);
        gbean.setAttribute("instancePool", pool);
        gbean.setAttribute("userTransaction", userTransaction);
        return gbean;
    }

    private LinkedHashMap buildVopMap(Class beanClass) throws Exception {
        LinkedHashMap vopMap = new LinkedHashMap();

        Method setMessageDrivenContext = null;
        try {
            Class messageDrivenContextClass = getClassLoader().loadClass("javax.ejb.MessageDrivenContext");
            setMessageDrivenContext = beanClass.getMethod("setMessageDrivenContext", new Class[]{messageDrivenContextClass});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement setMessageDrivenContext(javax.ejb.MessageDrivenContext)");
        }

        // add the create method
        vopMap.put(new InterfaceMethodSignature("create", true), new CreateMethod());

        // add the business methods
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }
            if (setMessageDrivenContext.equals(beanMethod)) {
                continue;
            }
            String name = beanMethod.getName();
            if (name.startsWith("ejb")) {
                continue;
            }
            MethodSignature signature = new MethodSignature(beanMethod);
            vopMap.put(
                    new InterfaceMethodSignature(signature, false),
                    new BusinessMethod(beanClass, signature));
        }

        return vopMap;
    }
}
