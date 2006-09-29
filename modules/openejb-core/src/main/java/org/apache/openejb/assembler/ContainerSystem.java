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

import javax.naming.NamingException;

import org.apache.openejb.naming.IvmContext;

public class ContainerSystem implements org.apache.openejb.spi.ContainerSystem{

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
     * @see Container#getContainerID() Container.getContainerID()
     */
    public Container getContainer(Object id){
        return (Container)containers.get(id);
    }

    /**
     * Gets all the <code>Container</code>s in this container system.
     *
     * @return an array of all the Containers
     * @see Container
     */
    public Container [] containers( ){
        return (Container [])containers.values().toArray(new Container [containers.size()]);
    }

    /**
     * Adds a Container to the list of those that are managed by this container system.
     * If a Container previously existed with the same id it will be replaced.
     * @param id the id of the Container
     * @param c the Container to manage
     * @see org.apache.openejb.assembler.Container
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
     * @see org.apache.openejb.assembler.DeploymentInfo
     */
    public void addDeployment(org.apache.openejb.assembler.CoreDeploymentInfo deployment){

        // add deployment to registry
        this.deployments.put(deployment.getDeploymentID(),deployment);

        // add deployment to OpenEJB JNDI Name Space
        javax.ejb.EJBHome ejbHome = deployment.getEJBHome();

        String bindName = deployment.getDeploymentID().toString();
        if(bindName.charAt(0)== '/')
            bindName = bindName.substring(1);
        bindName = "openejb/ejb/"+bindName;
        try {
            jndiRootContext.bind(bindName, ejbHome);
        } catch (NamingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

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
