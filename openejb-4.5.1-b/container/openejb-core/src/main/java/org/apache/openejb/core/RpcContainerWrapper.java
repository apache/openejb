/*
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
package org.apache.openejb.core;

import org.apache.openejb.BeanContext;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;

import java.lang.reflect.Method;

public class RpcContainerWrapper implements RpcContainer {

    private final RpcContainer container;

    public RpcContainerWrapper(RpcContainer container) {
        this.container = container;
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return container.invoke(deployID, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object [] args, Object primKey) throws OpenEJBException {
        return container.invoke(deployID, callInterface, callMethod, args, primKey);
    }

    public Object invoke(Object deployID, InterfaceType callType, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        return container.invoke(deployID, callType, callInterface, callMethod, args, primKey);
    }

    public ContainerType getContainerType() {
        return container.getContainerType();
    }

    public Object getContainerID() {
        return container.getContainerID();
    }

    public BeanContext getBeanContext(Object deploymentID) {
        return container.getBeanContext(deploymentID);
    }

    public BeanContext[] getBeanContexts() {
        return container.getBeanContexts();
    }

    public void deploy(BeanContext info) throws OpenEJBException {
        container.deploy(info);
    }

    public void start(BeanContext info) throws OpenEJBException {
        container.start(info);
    }
    
    public void stop(BeanContext info) throws OpenEJBException {
        container.stop(info);
        info.stop();
    }
    
    public void undeploy(BeanContext info) throws OpenEJBException {
        container.undeploy(info);
    }

    public RpcContainer getContainer() {
        return container;
    }
}
