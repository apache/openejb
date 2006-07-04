/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.assembler;

import java.util.HashMap;
import java.util.Properties;

import org.openejb.OpenEJBException;
import org.openejb.assembler.*;

/**
 * The Container manages one or more bean deployments at runtime. There are two
 * basic types of containers, the RPC container (org.openejb.RpcContainer) and
 * the Java Message Service container (org.openejb.JmsContainer), both of which
 * extend the base type org.openejb.Container.
 * <p>
 * The Container interface provides methods for accessing the Container's id, its
 * ContainerManager, and the deployments managed by the container (represented by
 * org.openejb.DeploymentInfo objects). In addition, the container defines the
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
     * @throws org.openejb.OpenEJBException
     *      Occurs when the container is not able to deploy the bean for some
     *      reason.
     */
    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException;
}
