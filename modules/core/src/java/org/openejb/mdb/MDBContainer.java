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
import java.util.Set;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.openejb.ConnectionTrackingInterceptor;
import org.openejb.EJBContainerConfiguration;
import org.openejb.SystemExceptionInterceptor;
import org.openejb.TransactionDemarcation;
import org.openejb.cache.InstancePool;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;
import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.DispatchInterceptor;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.dispatch.MethodSignature;
import org.openejb.security.EJBIdentityInterceptor;
import org.openejb.security.EJBRunAsInterceptor;
import org.openejb.security.EJBSecurityInterceptor;
import org.openejb.security.PermissionManager;
import org.openejb.security.PolicyContextHandlerEJBInterceptor;
import org.openejb.transaction.EJBUserTransaction;
import org.openejb.transaction.TransactionContextInterceptor;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.util.SoftLimitedInstancePool;

/**
 * @version $Revision$ $Date$
 */
public class MDBContainer implements MessageEndpointFactory, GBean {
    private final String ejbName;
    private final TransactionDemarcation transactionDemarcation;
    private final ReadOnlyContext componentContext;
    private final EJBUserTransaction userTransaction;
    private final Set unshareableResources;
    private final TransactionPolicySource transactionPolicySource;
    private final String contextId;
    private final Subject runAs;
    private final boolean setSecurityInterceptor;
    private final boolean setPolicyContextHandlerDataEJB;
    private final boolean setIdentity;

    private final TransactionManager transactionManager;
    private final ActivationSpec activationSpec;

    private final ClassLoader classLoader;
    private final Class beanClass;

    private final VirtualOperation[] vtable;
    private final MethodSignature[] signatures;
    private final Class messageEndpointInterface;
    private final MessageEndpointInterceptor messageClientContainer;
    private final InstancePool pool;

    private final Interceptor interceptor;

    public MDBContainer(EJBContainerConfiguration config, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator, ActivationSpec activationSpec) throws Exception {
        ejbName = config.ejbName;
        transactionDemarcation = config.txnDemarcation;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
        unshareableResources = config.unshareableResources;
        transactionPolicySource = config.transactionPolicySource;
        contextId = config.contextId;
        runAs = config.runAs;
        setSecurityInterceptor = config.setSecurityInterceptor;
        setPolicyContextHandlerDataEJB = config.setPolicyContextHandlerDataEJB;
        setIdentity = config.setIdentity;

        this.transactionManager = transactionManager;
        this.activationSpec = activationSpec;

        classLoader = Thread.currentThread().getContextClassLoader();
        beanClass = classLoader.loadClass(config.beanClassName);
        messageEndpointInterface = classLoader.loadClass(config.messageEndpointInterfaceName);

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
        }

        MDBOperationFactory vopFactory = MDBOperationFactory.newInstance(beanClass);
        vtable = vopFactory.getVTable();
        signatures = vopFactory.getSignatures();

        pool = new SoftLimitedInstancePool(new MDBInstanceFactory(this), 1);

        // set up server side interceptors
        Interceptor firstInterceptor;
        firstInterceptor = new DispatchInterceptor(vtable);
        if (trackedConnectionAssociator != null) {
            firstInterceptor = new ConnectionTrackingInterceptor(firstInterceptor, trackedConnectionAssociator, unshareableResources);
        }
        firstInterceptor = new TransactionContextInterceptor(firstInterceptor, transactionManager, new TransactionPolicyManager(transactionPolicySource, signatures));
        if (setIdentity) {
            firstInterceptor = new EJBIdentityInterceptor(firstInterceptor);
        }
        if (setSecurityInterceptor) {
            // todo check if we need to do security checks on MDBs
            firstInterceptor = new EJBSecurityInterceptor(firstInterceptor, contextId, new PermissionManager(ejbName, signatures));
        }
        if (runAs != null) {
            firstInterceptor = new EJBRunAsInterceptor(firstInterceptor, runAs);
        }
        if (setPolicyContextHandlerDataEJB) {
            firstInterceptor = new PolicyContextHandlerEJBInterceptor(firstInterceptor);
        }
        firstInterceptor = new MDBInstanceInterceptor(firstInterceptor, pool);
        firstInterceptor = new ComponentContextInterceptor(firstInterceptor, componentContext);
        firstInterceptor = new SystemExceptionInterceptor(firstInterceptor, getEJBName());
        interceptor = firstInterceptor;

        // set up client containers
        messageClientContainer = new MessageEndpointInterceptor(this, vopFactory.getSignatures(), messageEndpointInterface);
    }

    public Class getMessageEndpointInterface() {
        return messageEndpointInterface;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(true);
        }
        getAdapter().endpointActivation(this, activationSpec);
    }

    public void doStop() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
        getAdapter().endpointDeactivation(this, activationSpec);
    }

    public void doFail() {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
        getAdapter().endpointDeactivation(this, activationSpec);
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return interceptor.invoke(invocation);
    }

    public String getEJBName() {
        return ejbName;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public MessageEndpoint createEndpoint(XAResource adapterXAResource) throws UnavailableException {
        return messageClientContainer.getMessageEndpoint(adapterXAResource);
    }

    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        // TODO: need to see if the method is Supports or Required.
        return MDBContainer.this.transactionDemarcation == TransactionDemarcation.CONTAINER;
    }

    private ResourceAdapter getAdapter() {
        if (activationSpec.getResourceAdapter() == null) {
            throw new IllegalStateException("Attempting to use activation spec when it is not activated");
        }
        return activationSpec.getResourceAdapter();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(MDBContainer.class.getName());

        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"EJBContainerConfiguration", "TransactionManager", "TrackedConnectionAssociator", "ActivationSpec"},
                new Class[]{EJBContainerConfiguration.class, TransactionManager.class, TrackedConnectionAssociator.class, ActivationSpec.class}));

        infoFactory.addAttribute(new GAttributeInfo("ActivationSpec", true));
        infoFactory.addAttribute(new GAttributeInfo("EJBContainerConfiguration", true));

        infoFactory.addReference(new GReferenceInfo("TransactionManager", TransactionManager.class.getName()));
        infoFactory.addReference(new GReferenceInfo("TrackedConnectionAssociator", TrackedConnectionAssociator.class.getName()));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
