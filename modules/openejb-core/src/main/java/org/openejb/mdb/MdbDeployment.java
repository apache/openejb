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

import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.openejb.AbstractEjbDeployment;
import org.openejb.MdbContainer;
import org.openejb.MethodMap;
import org.openejb.SignatureIndexBuilder;
import org.openejb.cache.InstanceFactory;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.EJBTimeoutOperation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.transaction.TransactionPolicyType;
import org.openejb.util.SoftLimitedInstancePool;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

/**
 * @version $Revision$ $Date$
 */
public class MdbDeployment extends AbstractEjbDeployment implements MessageEndpointFactory, GBeanLifecycle {
    private static Boolean[] isTransactedMap = new Boolean[TransactionPolicyType.size()];

    static {
        isTransactedMap[TransactionPolicyType.Mandatory.getIndex()] = Boolean.TRUE;//this won't work, of course
        isTransactedMap[TransactionPolicyType.Never.getIndex()] = Boolean.FALSE;
        isTransactedMap[TransactionPolicyType.NotSupported.getIndex()] = Boolean.FALSE;
        isTransactedMap[TransactionPolicyType.Required.getIndex()] = Boolean.TRUE;
        isTransactedMap[TransactionPolicyType.RequiresNew.getIndex()] = Boolean.TRUE;
        isTransactedMap[TransactionPolicyType.Supports.getIndex()] = Boolean.FALSE;
    }


    private final ActivationSpecWrapper activationSpecWrapper;
    private final EndpointFactory endpointFactory;
    private final boolean[] deliveryTransacted;
    private final Map methodIndexMap;
    private final MethodMap dispatchMethodMap;
    private final InstancePool instancePool;

    public MdbDeployment(String containerId,
                         String ejbName,

                         String endpointInterfaceName,
                         String beanClassName,
                         ClassLoader classLoader,

                         ActivationSpecWrapper activationSpecWrapper,

                         MdbContainer ejbContainer,

                         String policyContextId,
                         Subject runAs,

                         boolean beanManagedTransactions,
                         SortedMap transactionPolicies,

                         Set unshareableResources,
                         Set applicationManagedSecurityResources,

                         Map componentContext,

                         Kernel kernel) throws Exception {
        this(containerId,
                ejbName,
                loadClass(beanClassName, classLoader, "bean class"),
                loadClass(endpointInterfaceName, classLoader, "endpoint interface"),
                classLoader,
                activationSpecWrapper,
                ejbContainer,
                policyContextId,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                unshareableResources,
                applicationManagedSecurityResources,
                componentContext,
                kernel);
    }

    public MdbDeployment(String containerId,
                         String ejbName,

                         Class beanClass,
                         Class endpointInterface,
                         ClassLoader classLoader,

                         ActivationSpecWrapper activationSpecWrapper,

                         MdbContainer ejbContainer,

                         String policyContextId,
                         Subject runAs,

                         boolean beanManagedTransactions,
                         SortedMap transactionPolicies,

                         Set unshareableResources,
                         Set applicationManagedSecurityResources,

                         Map componentContext,

                         Kernel kernel) throws Exception {

        super(containerId,
                ejbName,
                beanClass,
                classLoader,
                new MdbSignatureIndexBuilder(endpointInterface, beanClass),
                ejbContainer,
                false,
                policyContextId,
                null,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                kernel, unshareableResources, applicationManagedSecurityResources);

        this.activationSpecWrapper = activationSpecWrapper;

        InterfaceMethodSignature[] signatures = getSignatures();
        dispatchMethodMap = buildDispatchMethodMap();

        // build the instance factory
        MdbInstanceContextFactory contextFactory = new MdbInstanceContextFactory(this, ejbContainer);
        InstanceFactory instanceFactory = new MdbInstanceFactory(contextFactory);

        // build the pool
        instancePool = new SoftLimitedInstancePool(instanceFactory, 1);

        deliveryTransacted = new boolean[signatures.length];
        if (!beanManagedTransactions) {
            for (int i = 0; i < signatures.length; i++) {
                InterfaceMethodSignature signature = signatures[i];
                TransactionPolicyType transactionPolicyType = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "local", signature);
                deliveryTransacted[i] = transactionPolicyType == TransactionPolicyType.Required;
            }
        } else {
            for (int i = 0; i < signatures.length; i++) {
                deliveryTransacted[i] = false;
            }
        }

        // create the endpoint factory
        endpointFactory = new EndpointFactory(this, endpointInterface, classLoader, ejbContainer.getTransactionManager());

        // Method index map which is used to check if a method transacted and for legacy invocation
        Map map = new HashMap();
        for (int i = 0; i < this.signatures.length; i++) {
            InterfaceMethodSignature signature = this.signatures[i];
            Method method = signature.getMethod(endpointInterface);
            if (method != null) {
                map.put(method, new Integer(i));
            }
        }
        methodIndexMap = Collections.unmodifiableMap(map);
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchMethodMap.get(methodIndex);
        return vop;
    }

    public int getMethodIndex(Method method) {
        Integer methodIndex = (Integer) methodIndexMap.get(method);
        if (methodIndex == null) {
            return -1;
        }
        return methodIndex.intValue();
    }

    private static class MdbSignatureIndexBuilder implements SignatureIndexBuilder {
        private final Class endpointInterface;
        private final Class beanClass;

        public MdbSignatureIndexBuilder(Class endpointInterface, Class beanClass) {
            this.endpointInterface = endpointInterface;
            this.beanClass = beanClass;
        }

        public InterfaceMethodSignature[] createSignatureIndex() {
            TreeSet signatures = new TreeSet();

            if (TimedObject.class.isAssignableFrom(beanClass)) {
                signatures.add(new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false));
            }

            Method[] endpointMethods = endpointInterface.getMethods();
            for (int i = 0; i < endpointMethods.length; i++) {
                Method endpointMethod = endpointMethods[i];
                signatures.add(new InterfaceMethodSignature(endpointMethod, false));
            }
            return (InterfaceMethodSignature[]) signatures.toArray(new InterfaceMethodSignature[signatures.size()]);
        }
    }

    private MethodMap buildDispatchMethodMap() throws Exception {
        MethodMap dispatchMethodMap = new MethodMap(signatures);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            InterfaceMethodSignature ejbTimeoutSignature = new InterfaceMethodSignature("ejbTimeout", new String[]{Timer.class.getName()}, false);
            dispatchMethodMap.put(ejbTimeoutSignature, EJBTimeoutOperation.INSTANCE);
        }

        for (Iterator iterator = dispatchMethodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (!methodSignature.isHomeMethod()) {
                if (!methodName.startsWith("ejb") && !methodName.equals("setMessageDrivenContext")) {
                    MethodSignature signature = new MethodSignature(methodName, methodSignature.getParameterTypes());
                    entry.setValue(new BusinessMethod(beanClass, signature));
                }
            }
        }
        return dispatchMethodMap;
    }

    public MessageEndpoint createEndpoint(XAResource adapterXAResource) throws UnavailableException {
        NamedXAResource wrapper = adapterXAResource == null ? null : new WrapperNamedXAResource(adapterXAResource, containerId);
        return endpointFactory.getMessageEndpoint(wrapper);
    }

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        Integer methodIndex = (Integer) methodIndexMap.get(method);
        if (methodIndex == null) {
            throw new NoSuchMethodError("Unknown method: " + method);
        }

        return isDeliveryTransacted(methodIndex.intValue());
    }

    public boolean isDeliveryTransacted(int methodIndex) throws NoSuchMethodException {
        return deliveryTransacted[methodIndex];
    }

    public InstancePool getInstancePool() {
        return instancePool;
    }

    public void doStart() throws Exception {
        super.doStart();
        activationSpecWrapper.activate(this);
    }

    protected void destroy() throws PersistenceException {
        activationSpecWrapper.deactivate(this);
        super.destroy();
    }

    public String getEJBName() {
        return ejbName;
    }

    public EndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    public Map getMethodIndexMap() {
        return methodIndexMap;
    }


}