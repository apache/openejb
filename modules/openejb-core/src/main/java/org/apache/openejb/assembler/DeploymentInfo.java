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


import java.lang.reflect.Method;



/**
 * The DeploymentInfo object represents all the data needed by other parts of 
 * the container system for a bean that is deployed in the container system.  
 * Bean's that are deployed in the container system are refered to as bean 
 * deployments or deployments.  Each bean deployment will have its own DeploymentInfo 
 * object to represent much of the information that was provided to the container by 
 * the bean's EJB XML deployment descriptor or by the Deployer at the time of deployment.
 */
public interface DeploymentInfo {
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of Never.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.6 Never</i> of the EJB 1.1 specification"
     */
    final public static byte TX_NEVER = (byte)0;
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of NotSupported.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.1 NotSupported</i> of the EJB 1.1 specification"
     */
    final public static byte TX_NOT_SUPPORTED = (byte)1;
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of Supports.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.3 Supports</i> of the EJB 1.1 specification"
     */
    final public static byte TX_SUPPORTS = (byte)2;
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of Manditory.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.5 Manditory</i> of the EJB 1.1 specification"
     */
    final public static byte TX_MANDITORY = (byte)3;
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of Required.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.2 Required</i> of the EJB 1.1 specification"
     */
    final public static byte TX_REQUIRED = (byte)4;
    
    /** 
     * The constant that will be returned from the <code>getTransactionAttribute</code>
     * method if the bean's method has the transaction attribute of RequiresNew.
     * @see #getTransactionAttribute(Method)
     * @see "See section <i>11.6.2.4 RequiresNew</i> of the EJB 1.1 specification"
     */
    final public static byte TX_REQUIRES_NEW = (byte)5;
    
    
    final public static String AC_CREATE_EJBHOME = "create.ejbhome";

    /**
     * Gets the type of this bean component.
     * Will return a <code>STATEFUL</code>, <code>STATELESS</code>, <code>BMP_ENTITY</code> or <code>CMP_ENTITY</code>.
     *
     * @return Returns <code>STATEFUL</code>, <code>STATELESS</code>, <code>BMP_ENTITY</code> or <code>CMP_ENTITY</code>.
     */
    public int getComponentType( );
    
    
    /**
     * Gets the transaction attribute that must be applied to this method when executing.
     *
     * The type can be anyone of <code>TX_NEVER</code>, <code>TX_NOT_SUPPORTED</code>, <code>TX_SUPPORTS</code>, <code>TX_MANDITORY</code>, <code>TX_REQUIRED</code>, <code>TX_REQUIRES_NEW</code>, 
     *
     * @param method the bean's method for which transaction attribute information is needed
     * @return the transaction constant that states the method's transaction attribute
     * @see #TX_NEVER    
     * @see #TX_NOT_SUPPORTED    
     * @see #TX_SUPPORTS    
     * @see #TX_MANDITORY    
     * @see #TX_REQUIRED    
     * @see #TX_REQUIRES_NEW    
     */
    public byte getTransactionAttribute(Method method);


    /**
     * Gets the id of the container this deployed bean is in.
     *
     * @return the id of the deployment's container.
     * @see Container#getContainerID() Container.getContainerID()
     */
    public Container getContainer( );
    

    /**
     * Gets the id of this bean deployment.
     *
     * @return the id of of this bean deployment
     */
    public String getDeploymentID( );

    /**
     * Returns true if this bean deployment has chosen  bean-managed transaction demarcation.
     * Returns false if the continer will be managing the bean's transactions.
     *
     * @return Returns true if this bean deployment is managing its own transactions.
     */
    public boolean isBeanManagedTransaction();
    
    /**
     * Gets the home interface for the bean deployment. 
     *
     * Used primarily by Servers integrating OpenEJB into their platform.  Aids in implementing
     * the bean's home interface.
     *
     * @return a Class object of the bean's home interface
     * @see javax.ejb.EJBHome
     */
    public Class getHomeInterface( );
    
    /**
     * Gets the remote interface for the bean deployment. 
     *
     * Used primarily by Servers integrating OpenEJB into their platform.  Aids in implementing
     * the bean's remote interface.
     *
     * @return a Class object of the bean's remote interface
     * @see javax.ejb.EJBObject
     */
    public Class getRemoteInterface( );

    /**
     * Gets the bean's class definition.
     *
     * Used primarily by containers to instantiate new instances of a bean.
     * 
     * @return a Class object of the bean's class definition
     * @see javax.ejb.EnterpriseBean
     */
    public Class getBeanClass( );

    /**
     * Gets the Class type of the primary key for this bean deployment.
     * Returns null if the bean is a type that does not need a primary key.
     * 
     * @return the Class type of the bean's primary key or null if the bean doesn't need a primary key
     */
    public Class getPrimaryKeyClass( );
    
    /**
    * Useful for Container-Managed Persistence (CMP) Entity beans with Simple Primary Keys.
    * Gets the Field of the CMP entity bean class which corresponds to the simple 
    * primary key.  Entity beans that have complex primary keys (keys with several fields)
    * will not have a primkey-field.
    *
    * @return the EntityBean field that corresponds to the simple primary key.  
    *         return null if the bean is not a CMP Entity bean with a simple Primary key
    *
    */
//    public java.lang.reflect.Field getPrimaryKeyField( );
    
    
    /**
    * Useful for Container-Managed Persistence (CMP) Entity beans. Returns true
    * if entity allows reentrant. Session bean types will always return false;
    *
    * @return true if entity bean allows reentrant access
    *
    */
    public boolean isReentrant();
    
    
}
