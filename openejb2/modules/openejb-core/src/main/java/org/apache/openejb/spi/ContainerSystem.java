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

package org.apache.openejb.spi;

import org.apache.openejb.assembler.Container;
import org.apache.openejb.assembler.DeploymentInfo;

/**
 * The ContainerSystem interface represents a complete OpenEJB container system including
 * ContainerManagers, Containers, deployed enterprise beans and the primary services
 * (transaction, security, and persistence).
 * <p>
 * The ContainerSystem serves as the root in the container system hierarchy. The
 * ContainerSystem contains one or more ContainerManagers. ContainerManagers contain
 * one or more Containers. Containers contain one or more deployed beans of particular kind
 * (stateless, stateful, or entity).
 * <p>
 * The access to other parts of the container system hierarchy is not ridged, the
 * ContainerSystem interface provides methods for accessing its ContainerManagers by
 * ID or as a collection. The interface also provides methods for obtaining references to
 * specific Containers and DeploymentInfo objects by ID.  IDs for all elements of the
 * container system are unique across the container system.
 * <p>
 * The default implementation of this interface is provided by the
 * org.apache.openejb.core.ContainerSystem class.
 * <p>
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 */
public interface ContainerSystem {



    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified deployment id.
     *
     * @param id the deployment id of the deployed bean.
     * @return the DeploymentInfo object associated with the bean.
     * @see DeploymentInfo
     * @see Container#getDeploymentInfo(Object) Container.getDeploymentInfo
     * @see DeploymentInfo#getDeploymentID()
     */
    public DeploymentInfo getDeploymentInfo(Object id);

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in all the containers in this container system.
     *
     * @return an array of DeploymentInfo objects
     * @see DeploymentInfo
     * @see Container#deployments() Container.deployments()
     */
    public DeploymentInfo [] deployments( );

    /**
     * Returns the <code>Container</code> in this container system with the specified id.
     *
     * @param id the id of the Container
     * @return the Container associated with the id
     * @see Container
     * @see Container#getContainerID() Container.getContainerID()
     */
    public Container getContainer(Object id);

    /**
     * Gets all the <code>Container</code>s in this container system.
     *
     * @return an array of all the Containers
     * @see Container
     */
    public Container [] containers( );


    /**
    * Returns the global JNDI name space for the OpenEJB container system.
    * The global JNDI name space contains bindings for all enterprise bean
    * EJBHome object deployed in the entire container system.  EJBHome objects
    * are bound using their deployment-id under the java:openejb/ejb/ namespace.
    * For example, an enterprise bean with the deployment id = 55555 would be
    * have its EJBHome bound to the name "java:openejb/ejb/55555"
    *
    * @return the global JNDI context
    */
    public javax.naming.Context getJNDIContext();
}