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
package org.openejb.core;

import java.util.HashMap;

import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.ObjectReference;
import org.openejb.core.ivm.naming.Reference;

public class ContainerSystem implements org.openejb.spi.ContainerSystem{

    HashMap deployments = new HashMap();
    HashMap containers = new HashMap();
    IvmContext jndiRootContext = null;


    public ContainerSystem( ){
        // create OpenEJB JNDI Name Space
	try {
	    // Create the root context.
	    jndiRootContext = IvmContext.createRootContext();
	    // Create a subcontext to house the EJBs.
	    jndiRootContext.createSubcontext("java:openejb/ejb");
	}
	catch( javax.naming.NamingException exception) {
	    throw new RuntimeException();
	}
    }

    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified deployment id.
     *
     * @param id the deployment id of the deployed bean.
     * @return the DeploymentInfo object associated with the bean.
     * @see DeploymentInfo
     * @see org.openejb.Container#getDeploymentInfo(Object) Container.getDeploymentInfo
     * @see DeploymentInfo#getDeploymentID()
     */
    public DeploymentInfo getDeploymentInfo(Object id){
        return (DeploymentInfo)deployments.get(id);
    }

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in all the containers in this container system.
     *
     * @return an array of DeploymentInfo objects
     * @see DeploymentInfo
     * @see Container#deployments() Container.deployments()
     */
    public DeploymentInfo [] deployments( ){
        return (DeploymentInfo [])deployments.values().toArray(new DeploymentInfo [deployments.size()]);
    }

    /**
     * Returns the <code>Container</code> in this container system with the specified id.
     *
     * @param id the id of the Container
     * @return the Container associated with the id
     * @see Container
     * @see ContainerManager#getContainer(Object) ContainerManager.getContainer
     * @see Container#getContainerID() Container.getContainerID()
     * @see DeploymentInfo#getContainerID() DeploymentInfo.getContainerID()
     */
    public Container getContainer(Object id){
        return (Container)containers.get(id);
    }

    /**
     * Gets all the <code>Container</code>s in this container system.
     *
     * @return an array of all the Containers
     * @see Container
     * @see ContainerManager#containers() ContainerManager.containers()
     */
    public Container [] containers( ){
        return (Container [])containers.values().toArray(new Container [containers.size()]);
    }

    /**
     * Adds a Container to the list of those that are managed by this container system.
     * If a Container previously existed with the same id it will be replaced.
     * @param id the id of the Container
     * @param the Container to manage
     * @see org.openejb.Container
     */
    public void addContainer(Object id, Container c){
        containers.put(id,c);
    }


    /**
     * Adds a DeploymentInfo object to the list of those that are registered 
     * by this container System.
     * 
     * If a DeploymentInfo object previously existed with the same id it will 
     * be replaced.
     * 
     * Also adds deployment to OpenEJB's global JNDI Name Space under the context
     *   java:openejb/ejb/<i>deployment-id</i>
     * 
     * The global JNDI name space contains bindings for all enterprise bean
     * EJBHome object deployed in the entire container system.  EJBHome objects
     * are bound using their deployment-id under the java:openejb/ejb/ namespace.
     * For example, an enterprise bean with the deployment id = 55555 would be
     * have its EJBHome bound to the name "java:openejb/ejb/55555"
     * 
     * @param deployment
     * @see org.openejb.DeploymentInfo
     */
    public void addDeployment(org.openejb.core.DeploymentInfo deployment){

        // add deployment to registry
        this.deployments.put(deployment.getDeploymentID(),deployment);

        // add deployment to OpenEJB JNDI Name Space
        javax.ejb.EJBHome ejbHome = deployment.getEJBHome();
        Reference ref = new ObjectReference( ejbHome );
        if(deployment.getComponentType()== DeploymentInfo.STATEFUL)
            ref = new org.openejb.core.stateful.EncReference( ref );
        else if(deployment.getComponentType()== DeploymentInfo.STATELESS)
            ref = new org.openejb.core.stateless.EncReference( ref );
        else
            ref = new org.openejb.core.entity.EncReference( ref );
        try{

        String bindName = deployment.getDeploymentID().toString();
        if(bindName.charAt(0)== '/')
            bindName = bindName.substring(1);
        bindName = "openejb/ejb/"+bindName;
        jndiRootContext.bind(bindName, ref);
        }catch(Exception e){ e.printStackTrace();throw new RuntimeException();}

    }

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
    public javax.naming.Context getJNDIContext(){
        return jndiRootContext;
    }
}
