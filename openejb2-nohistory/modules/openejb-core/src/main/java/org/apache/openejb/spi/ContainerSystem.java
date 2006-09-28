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
 * $Id: ContainerSystem.java 444993 2004-10-25 09:55:08Z dblevins $
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
 * org.openejb.core.ContainerSystem class.
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