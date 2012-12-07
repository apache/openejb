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
package org.apache.openejb.core.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.io.Serializable;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public class EntityEjbObjectHandler extends EjbObjectProxyHandler {

    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEjbObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEjbObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the BaseEjbProxyHandler.invalidateAllHandlers is invoked. The EntityEjbObjectHandler uses a 
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    private Object registryId;

    public EntityEjbObjectHandler(BeanContext beanContext, Object pk, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        super(beanContext, pk, interfaceType, interfaces, mainInterface);
    }

    /*
    * This method generates a logically unique entity bean identifier from the primary key,
    * deployment id, and container id. This registry key is then used as an index for the associated
    * entity bean in the BaseEjbProxyHandler.liveHandleRegistry. The liveHandleRegistry tracks 
    * handler for the same bean identity so that they can removed together when one of the remove() operations
    * is called.
    */
    public static Object getRegistryId(Container container, Object deploymentId, Object primaryKey) {
        return new RegistryId(container, deploymentId, primaryKey);
    }

    public Object getRegistryId() {
        if (registryId == null) {
            registryId = getRegistryId(container, deploymentID, primaryKey);
        }
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        return primaryKey;
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);

        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument to isIdentical, but received " + args.length);
        }

        Object that = args[0];
        Object invocationHandler = ProxyManager.getInvocationHandler(that);

        if (invocationHandler instanceof EntityEjbObjectHandler) {
            EntityEjbObjectHandler handler = (EntityEjbObjectHandler) invocationHandler;

            /*
            * The registry id is a compound key composed of the bean's primary key, deployment id, and
            * container id.  It uniquely identifies the entity bean that is proxied by the EntityEjbObjectHandler
            * within the IntraVM.
            */
            return this.getRegistryId().equals(handler.getRegistryId());
        }
        return false;
    }

    protected Object remove(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, interfaceType, interfce, method, args, primaryKey);
        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(getRegistryId());
        return value;
    }

    public void invalidateReference() {
        // entity bean object references should not be invalidated since they
        // will automatically hook up to a new instance of the bean using the
        // primary key (we will load a new instance from the db)
    }

    private static class RegistryId implements Serializable {
        private static final long serialVersionUID = -6009230402616418827L;

        private final Object containerId;
        private final Object deploymentId;
        private final Object primaryKey;

        public RegistryId(Container container, Object deploymentId, Object primaryKey) {
            if (container == null) throw new NullPointerException("container is null");
            if (deploymentId == null) throw new NullPointerException("deploymentId is null");

            this.containerId = container.getContainerID();
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegistryId that = (RegistryId) o;

            return containerId.equals(that.containerId) &&
                    deploymentId.equals(that.deploymentId) &&
                    !(primaryKey != null ? !primaryKey.equals(that.primaryKey) : that.primaryKey != null);
        }

        public int hashCode() {
            int result;
            result = containerId.hashCode();
            result = 31 * result + deploymentId.hashCode();
            result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0);
            return result;
        }


        public String toString() {
            return "[" + containerId + ", " + deploymentId + ", " + primaryKey + "]";
        }
    }
}
