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

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.TransactionManager;

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.proxy.CglibEJBProxyFactory;
import org.apache.openejb.proxy.EJBProxyHelper;

/**
 * @version $Revision$ $Date$
 */
public class EndpointFactory {
    private final MdbDeployment mdbDeploymentContext;
    private final CglibEJBProxyFactory endpointFactory;
    private final int[] operationMap;
    private final TransactionManager transactionManager;

    public EndpointFactory(MdbDeployment mdbDeploymentContext, Class mdbInterface, ClassLoader classLoader, TransactionManager transactionManager) {
        this.mdbDeploymentContext = mdbDeploymentContext;
        InterfaceMethodSignature[] signatures = mdbDeploymentContext.getSignatures();
        endpointFactory = new CglibEJBProxyFactory(EndpointProxy.class, new Class[]{mdbInterface, MessageEndpoint.class}, classLoader);
        operationMap = EJBProxyHelper.getOperationMap(endpointFactory.getType(), signatures, true);
        this.transactionManager = transactionManager;
    }

    public MessageEndpoint getMessageEndpoint(NamedXAResource xaResource) {
        EndpointHandler handler = new EndpointHandler(mdbDeploymentContext, xaResource, operationMap, transactionManager);
        return (MessageEndpoint) endpointFactory.create(handler,
                new Class[]{EndpointHandler.class},
                new Object[]{handler});
    }
}
