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

package org.apache.openejb.assembler;

import java.util.HashMap;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;

/**
 * The Container manages one or more bean deployments at runtime. There are two
 * basic types of containers, the RPC container (org.apache.openejb.RpcContainer) and
 * the Java Message Service container (org.apache.openejb.JmsContainer), both of which
 * extend the base type org.apache.openejb.Container.
 * <p>
 * The Container interface provides methods for accessing the Container's id, its
 * ContainerManager, and the deployments managed by the container (represented by
 * org.apache.openejb.DeploymentInfo objects). In addition, the container defines the
 * getContainerType() method, which will return Container.ENTITY, Container.STATEFUL,
 * Container.STATELESS, or Container.MESSAGE_DRIVEN, depending on the bean type
 * managed by the container.
 *
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 */
public interface Container {

    final public static int STATELESS = 1;
    final public static int STATEFUL = 2;
    final public static int ENTITY = 3;
    final public static int MESSAGE_DRIVEN = 4;


    /**
    * This method is used to initalized a new container with its name, deployments and properties.
    * this method is invoked by the assembler and will throw an exception if invoked after 
    * the container is assembled.
    */
    public void init(Object containerId, HashMap deployments, Properties properties)
    throws OpenEJBException;


    /**
     * Gets the type of container (STATELESS, STATEFUL, ENTITY, or MESSAGE_DRIVEN
     *
     * @return id type bean container
     */
    public int getContainerType( );

    /**
     * Gets the id of this container.
     *
     * @return the id of this container.
     * @see DeploymentInfo#getContainerID() DeploymentInfo.getContainerID()
     */
    public Object getContainerID();


    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified deployment id.
     *
     * @param id the deployment id of the deployed bean.
     * @return the DeploymentInfo object associated with the bean.
     * @see DeploymentInfo
     * @see ContainerSystem#getDeploymentInfo(Object) ContainerSystem.getDeploymentInfo
     * @see DeploymentInfo#getDeploymentID()
     */
    public DeploymentInfo getDeploymentInfo(Object deploymentID);

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in this container.
     *
     * @return an array of DeploymentInfo objects
     * @see DeploymentInfo
     * @see ContainerSystem#deployments() ContainerSystem.deployments()
     */
    public DeploymentInfo [] deployments();

    /**
     * Adds a bean to this container.
     * @param deploymentId the deployment id of the bean to deploy.
     * @param info the DeploymentInfo object associated with the bean.
     * @throws org.apache.openejb.OpenEJBException
     *      Occurs when the container is not able to deploy the bean for some
     *      reason.
     */
    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException;
}
