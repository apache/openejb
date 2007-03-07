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
package org.apache.openejb.mdb;

import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.openejb.AbstractEjbDeployment;
import org.apache.openejb.MdbContainer;
import org.apache.openejb.MethodMap;
import org.apache.openejb.SignatureIndexBuilder;
import org.apache.openejb.cache.InstanceFactory;
import org.apache.openejb.cache.InstancePool;
import org.apache.openejb.dispatch.EJBTimeoutOperation;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.transaction.TransactionPolicyManager;
import org.apache.openejb.transaction.TransactionPolicyType;
import org.apache.openejb.util.SoftLimitedInstancePool;

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
                         DefaultPrincipal defaultPrincipal,
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
                defaultPrincipal,
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
                         DefaultPrincipal defaultPrincipal,
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
                defaultPrincipal,
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