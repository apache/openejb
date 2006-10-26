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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.security.auth.Subject;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.J2EEManagedObject;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.cache.SimpleInstanceCache;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.sfsb.BusinessMethod;
import org.apache.openejb.sfsb.CreateMethod;
import org.apache.openejb.sfsb.RemoveMethod;
import org.apache.openejb.sfsb.StatefulInstanceContextFactory;
import org.apache.openejb.sfsb.StatefulInstanceFactory;


/**
 * @version $Revision$ $Date$
 */
public class StatefulEjbDeployment extends AbstractRpcDeployment implements ExtendedEjbDeployment, J2EEManagedObject {
    private final StatefulInstanceFactory instanceFactory;
    private final InstanceCache instanceCache;
    private final MethodMap dispatchMethodMap;

    public StatefulEjbDeployment(String containerId,
                                 String ejbName,

                                 String homeInterfaceName,
                                 String remoteInterfaceName,
                                 String localHomeInterfaceName,
                                 String localInterfaceName,
                                 String beanClassName,
                                 ClassLoader classLoader,

                                 StatefulEjbContainer ejbContainer,

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
                                 Set applicationManagedSecurityResources) throws Exception {
        this(containerId,
                ejbName,


                loadClass(homeInterfaceName, classLoader, "home interface"),
                loadClass(remoteInterfaceName, classLoader, "remote interface"),
                loadClass(localHomeInterfaceName, classLoader, "local home interface"),
                loadClass(localInterfaceName, classLoader, "local interface"),
                loadClass(beanClassName, classLoader, "bean class"),
                classLoader,

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
    }

    public StatefulEjbDeployment(String containerId,
                                 String ejbName,

                                 Class homeInterface,
                                 Class remoteInterface,
                                 Class localHomeInterface,
                                 Class localInterface,
                                 Class beanClass,
                                 ClassLoader classLoader,

                                 StatefulEjbContainer ejbContainer,

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
                                 Set applicationManagedSecurityResources) throws Exception {

        super(containerId,
                ejbName,

                new ProxyInfo(EJBComponentType.STATEFUL,
                        containerId,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null,
                        null),
                beanClass,
                classLoader,

                new RpcSignatureIndexBuilder(beanClass,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null),

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

        // build the instance factory
        StatefulInstanceContextFactory contextFactory;
        contextFactory = new StatefulInstanceContextFactory(this,
                ejbContainer,
                proxyFactory
        );

        instanceFactory = new StatefulInstanceFactory(contextFactory);

        // build the cache
        this.instanceCache = new SimpleInstanceCache();
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchMethodMap.get(methodIndex);
        return vop;
    }

    private MethodMap buildDispatchMethodMap() throws Exception {
        Class beanClass = getBeanClass();

        MethodMap dispatchMethodMap = new MethodMap(signatures);

        MethodSignature removeSignature = new MethodSignature("ejbRemove");
        RemoveMethod removeVop = new RemoveMethod(beanClass, removeSignature);
        for (Iterator iterator = dispatchMethodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (methodSignature.isHomeMethod()) {
                if (methodName.startsWith("create")) {
                    String baseName = methodName.substring(6);
                    MethodSignature createSignature = new MethodSignature("ejbCreate" + baseName, methodSignature.getParameterTypes());
                    entry.setValue(new CreateMethod(beanClass, createSignature));
                } else if (methodName.startsWith("remove")) {
                    entry.setValue(removeVop);
                }
            } else {
                if (methodName.startsWith("remove") && methodSignature.getParameterTypes().length == 0) {
                    entry.setValue(removeVop);
                } else if (!methodName.startsWith("ejb") &&
                        !methodName.equals("setSessionContext") &&
                        !methodName.equals("afterBegin") &&
                        !methodName.equals("beforeCompletion") &&
                        !methodName.equals("afterCompletion")) {
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

    public StatefulInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public InstanceCache getInstanceCache() {
        return instanceCache;
    }

}
