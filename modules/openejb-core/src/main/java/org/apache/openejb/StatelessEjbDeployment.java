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
package org.apache.openejb;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.J2EEManagedObject;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.openejb.cache.InstanceFactory;
import org.apache.openejb.cache.InstancePool;
import org.apache.openejb.dispatch.EJBTimeoutOperation;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.slsb.BusinessMethod;
import org.apache.openejb.slsb.CreateMethod;
import org.apache.openejb.slsb.StatelessInstanceContextFactory;
import org.apache.openejb.slsb.StatelessInstanceFactory;
import org.apache.openejb.util.SoftLimitedInstancePool;

import javax.ejb.Handle;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;


/**
 * @version $Revision$ $Date$
 */
public class StatelessEjbDeployment extends AbstractRpcDeployment implements ExtendedEjbDeployment, J2EEManagedObject {
    private final InstancePool instancePool;
    private final List handlerInfos;
    private final MethodMap dispatchMethodMap;

    public StatelessEjbDeployment(String containerId,
                                  String ejbName,

                                  String homeInterfaceName,
                                  String remoteInterfaceName,
                                  String localHomeInterfaceName,
                                  String localInterfaceName,
                                  String serviceEndpointInterfaceName,
                                  String beanClassName,
                                  ClassLoader classLoader,
                                  int cacheSize,

                                  StatelessEjbContainer ejbContainer,

                                  String[] jndiNames,
                                  String[] localJndiNames,

                                  boolean securityEnabled,
                                  String policyContextId,
                                  DefaultPrincipal defaultPrincipal,
                                  Subject runAs,

                                  boolean beanManagedTransactions,
                                  SortedMap transactionPolicies,

                                  Map componentContext,

                                  Kernel kernel,

                                  // connector stuff
                                  Set unshareableResources,
                                  Set applicationManagedSecurityResources,

                                  // web services
                                  List handlerInfos) throws Exception {

        this(containerId,
                ejbName,
                loadClass(homeInterfaceName, classLoader, "home interface"),
                loadClass(remoteInterfaceName, classLoader, "remote interface"),
                loadClass(localHomeInterfaceName, classLoader, "local home interface"),
                loadClass(localInterfaceName, classLoader, "local interface"),
                loadClass(serviceEndpointInterfaceName, classLoader, "service endpoint interface"),
                loadClass(beanClassName, classLoader, "bean class"),
                classLoader,
                cacheSize,
                ejbContainer,
                jndiNames,
                localJndiNames,
                securityEnabled,
                policyContextId,
                defaultPrincipal,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                kernel,
                unshareableResources,
                applicationManagedSecurityResources,
                handlerInfos);
    }

    public StatelessEjbDeployment(String containerId,
                                  String ejbName,

                                  Class homeInterface,
                                  Class remoteInterface,
                                  Class localHomeInterface,
                                  Class localInterface,
                                  Class serviceEndpointInterface,
                                  Class beanClass,
                                  ClassLoader classLoader,
                                  int cacheSize,

                                  StatelessEjbContainer ejbContainer,

                                  String[] jndiNames,
                                  String[] localJndiNames,

                                  boolean securityEnabled,
                                  String policyContextId,
                                  DefaultPrincipal defaultPrincipal,
                                  Subject runAs,

                                  boolean beanManagedTransactions,
                                  SortedMap transactionPolicies,

                                  Map componentContext,

                                  Kernel kernel,

                                  // connector stuff
                                  Set unshareableResources,
                                  Set applicationManagedSecurityResources,

                                  // web services
                                  List handlerInfos) throws Exception {

        super(containerId,
                ejbName,
                new ProxyInfo(EJBComponentType.STATELESS,
                        containerId,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        serviceEndpointInterface,
                        null),
                beanClass,
                classLoader,
                new StatelessSignatureIndexBuilder(beanClass, homeInterface, remoteInterface, localHomeInterface, localInterface, serviceEndpointInterface),
                ejbContainer,
                jndiNames,
                localJndiNames,
                securityEnabled,
                policyContextId,
                defaultPrincipal,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                kernel,
                unshareableResources,
                applicationManagedSecurityResources);

        dispatchMethodMap = buildDispatchMethodMap();

        InstanceContextFactory contextFactory = new StatelessInstanceContextFactory(this, ejbContainer, proxyFactory);

        InstanceFactory instanceFactory = new StatelessInstanceFactory(contextFactory);

        instancePool = new SoftLimitedInstancePool(instanceFactory, cacheSize);

        this.handlerInfos = handlerInfos;
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchMethodMap.get(methodIndex);
        return vop;
    }

    private static class StatelessSignatureIndexBuilder extends RpcSignatureIndexBuilder {
        public StatelessSignatureIndexBuilder(Class beanClass, Class homeInterface, Class remoteInterface, Class localHomeInterface, Class localInterface, Class serviceEndpointInterface) {
            super(beanClass, homeInterface, remoteInterface, localHomeInterface, localInterface, serviceEndpointInterface);
        }

        public TreeSet createSignatureSet() {
            TreeSet signatures = super.createSignatureSet();
            signatures.remove(new InterfaceMethodSignature("ejbActivate", false));
            signatures.remove(new InterfaceMethodSignature("ejbPassivate", false));
            signatures.remove(new InterfaceMethodSignature("remove", false));
            signatures.remove(new InterfaceMethodSignature("remove", new Class[]{Object.class}, true));
            signatures.remove(new InterfaceMethodSignature("remove", new Class[]{Handle.class}, true));
            return signatures;
        }
    }


    private MethodMap buildDispatchMethodMap() throws Exception {
        Class beanClass = getBeanClass();

        MethodMap dispatchMethodMap = new MethodMap(signatures);

        // create... this is the method that is called by the user
        InterfaceMethodSignature createSignature = new InterfaceMethodSignature("create", true);
        dispatchMethodMap.put(createSignature, CreateMethod.INSTANCE);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            InterfaceMethodSignature timeoutSignature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
            dispatchMethodMap.put(timeoutSignature, EJBTimeoutOperation.INSTANCE);
        }

        // add the business methods
        for (Iterator iterator = dispatchMethodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (!methodSignature.isHomeMethod()) {
                if (!methodName.startsWith("ejb") && !methodName.equals("setSessionContext")) {
                    MethodSignature signature = new MethodSignature(methodName, methodSignature.getParameterTypes());
                    entry.setValue(new BusinessMethod(beanClass, signature));
                }
            }
        }

        return dispatchMethodMap;
    }

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public InstancePool getInstancePool() {
        return instancePool;
    }

    public List getHandlerInfos() {
        return handlerInfos;
    }

}