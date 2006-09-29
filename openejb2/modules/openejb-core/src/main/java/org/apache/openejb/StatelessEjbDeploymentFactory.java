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

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class StatelessEjbDeploymentFactory extends RpcEjbDeploymentFactory {
    protected boolean beanManagedTransactions;
    private String serviceEndpointInterfaceName;
    private List handlerInfos;

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public void setBeanManagedTransactions(boolean beanManagedTransactions) {
        this.beanManagedTransactions = beanManagedTransactions;
    }

    public String getServiceEndpointInterfaceName() {
        return serviceEndpointInterfaceName;
    }

    public void setServiceEndpointInterfaceName(String serviceEndpointInterfaceName) {
        this.serviceEndpointInterfaceName = serviceEndpointInterfaceName;
    }

    public List getHandlerInfos() {
        return handlerInfos;
    }

    public void setHandlerInfos(List handlerInfos) {
        this.handlerInfos = handlerInfos;
    }

    public Object create() throws Exception {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }
        }
        Class beanClass = loadClass(beanClassName, "bean class");
        Class homeInterface = loadClass(homeInterfaceName, "home interface");
        Class remoteInterface = loadClass(remoteInterfaceName, "remote interface");
        Class localHomeInterface = loadClass(localHomeInterfaceName, "local home interface");
        Class localInterface = loadClass(localInterfaceName, "local interface");
        Class serviceEndpointInterface = loadClass(serviceEndpointInterfaceName, "service endpoint interface");

        return new StatelessEjbDeployment(containerId,
                ejbName,

                homeInterface,
                remoteInterface,
                localHomeInterface,
                localInterface,
                serviceEndpointInterface,
                beanClass,

                classLoader,
                (StatelessEjbContainer) ejbContainer,
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
                tssBean,
                unshareableResources,
                applicationManagedSecurityResources,
                handlerInfos);
    }
}
