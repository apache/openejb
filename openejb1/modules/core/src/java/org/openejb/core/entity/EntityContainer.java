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
package org.openejb.core.entity;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EnterpriseBean;
import javax.ejb.EntityBean;
import javax.transaction.Transaction;

import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.SystemException;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;
import org.openejb.util.Logger;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;

/**
 * Bean-Managed Persistence EntityBean container
 * 
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class EntityContainer implements org.openejb.RpcContainer, TransactionContainer{

    /**
     * Managed bean instances; transaction ready and ready pools
     */
    protected EntityInstanceManager instanceManager;
    
    /**
     * Contains deployment information for each by deployed to this container
     */
    protected HashMap deploymentRegistry;
    
    /**
     * The unique id for this container
     */
    protected Object containerID = null;

    // manages the transactional scope according to the bean's transaction attributes
    //EntityTransactionScopeHandler txScopeHandle;
    public Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.util.resources" );


    /**
     * Construct this container with the specified container id, deployments, 
     * container manager and properties.  The properties can include the class 
     * name of the preferred InstanceManager, 
     * org.openejb.core.entity.EntityInstanceManager is the default. The 
     * properties should also include the properties for the instance manager.
     * 
     * @param id         the unique id to identify this container in the ContainerSystem
     * @param registry   a hashMap of bean delpoyments that this container will be responsible for
     * @param properties the properties this container needs to initialize and run
     * @exception OpenEJBException
     *                   if there is a problem constructing the container
     * @exception org.openejb.OpenEJBException
     * @see org.openejb.Container
     */
    public void init(Object id, HashMap registry, Properties properties)
    throws org.openejb.OpenEJBException{
        containerID = id;
        deploymentRegistry = registry;

        if(properties == null)properties = new Properties();


        SafeToolkit toolkit = SafeToolkit.getToolkit("EntityContainer");
        SafeProperties safeProps = toolkit.getSafeProperties(properties);
        try{
        String className = safeProps.getProperty(EnvProps.IM_CLASS_NAME, "org.openejb.core.entity.EntityInstanceManager");
        ClassLoader cl = org.openejb.util.ClasspathUtils.getContextClassLoader();
        instanceManager =(EntityInstanceManager)Class.forName(className, true, cl).newInstance();
        }catch(Exception e){
        throw new org.openejb.SystemException("Initialization of InstanceManager for the \""+containerID+"\" entity container failed",e);
        }
        instanceManager.init(this, registry, properties);


        //txScopeHandle = new EntityTransactionScopeHandler(this,instanceManager);

        /*
        * This block of code is necessary to avoid a chicken and egg problem. The DeploymentInfo
        * objects must have a reference to their container during this assembly process, but the
        * container is created after the DeploymentInfo necessitating this loop to assign all
        * deployment info object's their containers.
        */
        org.openejb.DeploymentInfo [] deploys = this.deployments();
        for(int x = 0; x < deploys.length; x++){
            org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)deploys[x];
            di.setContainer(this);
        }

    }
    //===============================
    // begin Container Implementation
    //

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in 
     * this container.
     * 
     * @return an array of DeploymentInfo objects
     * @see org.openejb.DeploymentInfo
     * @see org.openejb.ContainerSystem#deployments() ContainerSystem.deployments()
     */
    public DeploymentInfo [] deployments(){
        return (DeploymentInfo [])deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified
     * deployment id.
     * 
     * @param deploymentID
     * @return the DeploymentInfo object associated with the bean.
     * @see org.openejb.DeploymentInfo
     * @see org.openejb.ContainerSystem#getDeploymentInfo(Object) ContainerSystem.getDeploymentInfo
     * @see org.openejb.DeploymentInfo#getDeploymentID()
     */
    public DeploymentInfo getDeploymentInfo(Object deploymentID){
        return (DeploymentInfo)deploymentRegistry.get(deploymentID);
    }
    /**
     * Gets the type of container (STATELESS, STATEFUL, ENTITY, or MESSAGE_DRIVEN
     *
     * @return id type bean container
     */
    public int getContainerType( ){
        return Container.ENTITY;
    }

    /**
     * Gets the id of this container.
     *
     * @return the id of this container.
     * @see org.openejb.DeploymentInfo#getContainerID() DeploymentInfo.getContainerID()
     */
    public Object getContainerID(){
        return containerID;
    }

    /**
     * Adds a bean to this container.
     * @param deploymentId the deployment id of the bean to deploy.
     * @param info the DeploymentInfo object associated with the bean.
     * @throws org.openejb.OpenEJBException
     *      Occurs when the container is not able to deploy the bean for some
     *      reason.
     */
    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException {
        HashMap registry = (HashMap)deploymentRegistry.clone();
        registry.put(deploymentID, info);
        deploymentRegistry = registry;
    }

    /**
     * Invokes a method on an instance of the specified bean deployment.
     *
     * @param deployID the dployment id of the bean deployment
     * @param callMethod the method to be called on the bean instance
     * @param args the arguments to use when invoking the specified method
     * @param primKey the primary key class of the bean or null if the bean does not need a primary key
     * @param prncpl
     * @return the result of invoking the specified method on the bean instance
     * @throws org.openejb.OpenEJBException
     * @see org.openejb.Container#invoke Container.invoke
     * @see org.openejb.core.stateful.StatefulContainer#invoke StatefulContainer.invoke
     */
    public Object invoke(Object deployID, Method callMethod,Object [] args,Object primKey, Object securityIdentity)    throws org.openejb.OpenEJBException{
        try{

        org.openejb.core.DeploymentInfo deployInfo = (org.openejb.core.DeploymentInfo)this.getDeploymentInfo(deployID);

        ThreadContext callContext = ThreadContext.getThreadContext();
        callContext.set(deployInfo, primKey, securityIdentity);

        // check authorization to invoke

        boolean authorized = OpenEJB.getSecurityService().isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));
        if(!authorized)
            throw new org.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));

        // process home interface methods
        if(EJBHome.class.isAssignableFrom(callMethod.getDeclaringClass())){
            if(callMethod.getDeclaringClass()!= EJBHome.class){
            // Its a home interface method, which is declared by the bean provider, but not a EJBHome method.
            // only create(), find<METHOD>( ) and home business methods are declared by the bean provider.
                if(callMethod.getName().equals("create")){
                    // create( ) method called, execute ejbCreate() method
                    return createEJBObject(callMethod, args, callContext);
                }else if(callMethod.getName().startsWith("find")){
                    // find<METHOD> called, execute ejbFind<METHOD>
                    return findMethod(callMethod, args, callContext);
                }else{
                    // home method called, execute ejbHome method
                    return homeMethod(callMethod, args, callContext);
                }
            }else if(callMethod.getName().equals("remove")){
                removeEJBObject(callMethod, args, callContext);
                return null;
            }
        }else if(EJBObject.class == callMethod.getDeclaringClass()){
            removeEJBObject(callMethod, args, callContext);
            return null;
        }


        // retreive instance from instance manager
        callContext.setCurrentOperation(Operations.OP_BUSINESS);
        Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);
        Object retValue = invoke(callMethod, runMethod, args, callContext) ;

        // see comments in org.openejb.core.DeploymentInfo.
        return deployInfo.convertIfLocalReference(callMethod, retValue);


        }finally{
            /*
                The thread context must be stripped from the thread before returning or throwing an exception
                so that an object outside the container does not have access to a
                bean's JNDI ENC.  In addition, its important for the
                org.openejb.core.ivm.java.javaURLContextFactory, which determines the context
                of a JNDI lookup based on the presence of a ThreadContext object.  If no ThreadContext
                object is available, then the request is assumed to be made from outside the container
                system and is given the global OpenEJB JNDI name space instead.  If there is a thread context,
                then the request is assumed to be made from within the container system and so the
                javaContextFactory must return the JNDI ENC of the current enterprise bean which it
                obtains from the DeploymentInfo object associated with the current thread context.
            */
            ThreadContext.setThreadContext(null);
        }
    }
    //
    // end ContainerManager Implementation
    //====================================


    //============================================
    // begin methods unique to this implementation
    //

    public EntityInstanceManager getInstanceManager( ){
        return instanceManager;
    }

    protected Object invoke(Method callMethod, Method runMethod, Object [] args, ThreadContext callContext)
    throws org.openejb.OpenEJBException{
        
        TransactionPolicy txPolicy   = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext();
        txContext.callContext = callContext;

        EntityBean bean = null;
        txPolicy.beforeInvoke( bean, txContext );
        
        Object returnValue = null;
        
        try{
            // this is nessary to ensure that the ejbLoad method is execute in the context of the business method
            try{
                bean = instanceManager.obtainInstance(callContext);
            }catch(org.openejb.OpenEJBException e){
                //TODO: Shouldn't we be throwing a NoSuchEntityException?
                throw e.getRootCause();
            }
            
            ejbLoad_If_No_Transaction(callContext,bean);
            returnValue = runMethod.invoke(bean, args);
            ejbStore_If_No_Transaction(callContext, bean);
            instanceManager.poolInstance(callContext,bean);
        }catch(java.lang.reflect.InvocationTargetException ite){// handle enterprise bean exceptions
            if ( ite.getTargetException() instanceof RuntimeException ) {
                /* System Exception ****************************/
                // don't pool after system exception
                txPolicy.handleSystemException( ite.getTargetException(), bean, txContext );
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext,bean);
                txPolicy.handleApplicationException( ite.getTargetException(), txContext );
            }
        }catch(org.openejb.SystemException se){
            txPolicy.handleSystemException( se.getRootCause(), bean, txContext );
        }catch(Throwable iae){// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            txPolicy.handleSystemException( iae, bean, txContext );
        }finally{
            txPolicy.afterInvoke( bean, txContext );
        }

        return returnValue;
    }
    
    /**
     * If a business method or remove method is called without a transaction 
     * context the ejbLoad method must be invoked before the call is serviced. 
     * This provides a bean instance that is not in a transaction an opportunity 
     * to load its state.
     * 
     * @param callContext
     * @param bean
     * @exception org.openejb.SystemException
     * @exception Exception
     */
    public void ejbLoad_If_No_Transaction(ThreadContext callContext, EntityBean bean)
    throws org.openejb.SystemException, Exception{
        byte orginalOperation = callContext.getCurrentOperation();
        if(orginalOperation == Operations.OP_BUSINESS || orginalOperation == Operations.OP_REMOVE){
        
            Transaction currentTx = null;
            try{
            currentTx = org.openejb.OpenEJB.getTransactionManager().getTransaction();
            }catch(javax.transaction.SystemException se){
                throw new org.openejb.SystemException("Transaction Manager failure",se);
            }
            
            if(currentTx ==null){
                callContext.setCurrentOperation(org.openejb.core.Operations.OP_LOAD);
                try{
                    ((javax.ejb.EntityBean)bean).ejbLoad();
                }catch(Exception e){
                    // this will always be handled by the invoke( ) method
                    instanceManager.discardInstance(callContext,(EntityBean)bean);
                    throw e;
                }finally{
                    callContext.setCurrentOperation(orginalOperation);
                }
            }
                
        }
    }
    
    /**
     * If a business method is called without a transaction context the ejbStore 
     * method must be invoked after the call is serviced. This provides a bean
     * instance that is not in a transaction an opportunity to store its state.
     * 
     * @param callContext
     * @param bean
     * @exception Exception
     */
    public void ejbStore_If_No_Transaction(ThreadContext callContext, EntityBean bean)
    throws Exception{
        
        byte currentOp = callContext.getCurrentOperation();
        if (currentOp == Operations.OP_BUSINESS){
            
            Transaction currentTx = null;
            try{
            currentTx = org.openejb.OpenEJB.getTransactionManager().getTransaction();
            }catch(javax.transaction.SystemException se){
                throw new org.openejb.SystemException("Transaction Manager failure",se);
            }
            
            if (currentTx == null){
                callContext.setCurrentOperation(org.openejb.core.Operations.OP_STORE);
                try{
                    ((javax.ejb.EntityBean)bean).ejbStore();
                }catch(Exception e){
                    // this will always be handled by the invoke( ) method
                    instanceManager.discardInstance(callContext,(EntityBean)bean);
                    throw e;
                }finally{
                    callContext.setCurrentOperation(currentOp);
                }
            }
        }
    }
        
    protected void didCreateBean(ThreadContext callContext, EntityBean bean) throws org.openejb.OpenEJBException{
    }

    // create methods from home interface
    protected ProxyInfo createEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
    throws org.openejb.OpenEJBException {

        org.openejb.core.DeploymentInfo deploymentInfo = (org.openejb.core.DeploymentInfo)callContext.getDeploymentInfo();
        
        callContext.setCurrentOperation(Operations.OP_CREATE);
        EntityBean bean = null;
        Object primaryKey = null;
        
        TransactionPolicy txPolicy   = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext();
        txContext.callContext = callContext;

        /*
        * According to section 9.1.5.1 of the EJB 1.1 specification, the "ejbPostCreate(...) 
        * method executes in the same transaction context as the previous ejbCreate(...) method."
        *
        * For this reason the TransactionScopeHandler methods usally preformed by the invoke( )
        * operation must be handled here along with the call explicitly.
        * This ensures that the afterInvoke() is not processed between the ejbCreate and ejbPostCreate methods to
        * ensure that the ejbPostCreate executes in the same transaction context of the ejbCreate.
        * This would otherwise not be possible if container-managed transactions were used because 
        * the TransactionScopeManager would attempt to commit the transaction immediately after the ejbCreate 
        * and before the ejbPostCreate had a chance to execute.  Once the ejbPostCreate method execute the 
        * super classes afterInvoke( ) method will be executed committing the transaction if its a CMT.
        */
        
        txPolicy.beforeInvoke( bean, txContext );
        
        try{

            bean = instanceManager.obtainInstance(callContext);
            Method ejbCreateMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
            
            // invoke ejbCreateX
            primaryKey = ejbCreateMethod.invoke(bean, args);
                        
            callContext.setPrimaryKey(primaryKey);        
            didCreateBean(callContext, bean);
            callContext.setCurrentOperation(Operations.OP_POST_CREATE);

            // invoke ejbPostCreateX
            Method ejbPostCreateMethod = deploymentInfo.getMatchingPostCreateMethod(ejbCreateMethod);
            
            ejbPostCreateMethod.invoke(bean, args);
            // might have change in didCreateBean e.g. in the castor container
            primaryKey = callContext.getPrimaryKey();
            callContext.setPrimaryKey(null);
            instanceManager.poolInstance(callContext,bean);            
        }catch(java.lang.reflect.InvocationTargetException ite){// handle enterprise bean exceptions
            if ( ite.getTargetException() instanceof RuntimeException ) {
                /* System Exception ****************************/
                txPolicy.handleSystemException( ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext,bean);
                txPolicy.handleApplicationException( ite.getTargetException(), txContext);
            }
        }catch(OpenEJBException e){
            txPolicy.handleSystemException( e.getRootCause(), bean, txContext);
        }catch(Throwable e){// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            txPolicy.handleSystemException( e, bean, txContext);
        }finally{
            txPolicy.afterInvoke( bean, txContext );
        }
        
        
        return new ProxyInfo(deploymentInfo, primaryKey, deploymentInfo.getRemoteInterface(), this);
        
    }
    /**
     * This method is used to execute the find methods which are considered global
     * in scope.
     * 
     * Global methods use bean instances from the MethodReady pool and are not 
     * specific to on bean identity.
     * 
     * The return value will be either a single ProxyInfo object or collection of 
     * ProxyInfo objects representing one or more remote references.
     * 
     * @param callMethod
     * @param args
     * @param callContext
     * @return 
     * @exception org.openejb.OpenEJBException
     */
    protected Object findMethod(Method callMethod, Object [] args, ThreadContext callContext)
    throws org.openejb.OpenEJBException {
        org.openejb.core.DeploymentInfo deploymentInfo = (org.openejb.core.DeploymentInfo)callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operations.OP_FIND);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        Object returnValue = invoke(callMethod,runMethod, args, callContext);

        /*
        * Find operations return either a single primary key or a collection of primary keys.
        * The primary keys are converted to ProxyInfo objects.
        */
        if(returnValue instanceof java.util.Collection){
            java.util.Iterator keys = ((java.util.Collection)returnValue).iterator();
            java.util.Vector proxies = new java.util.Vector();
            while(keys.hasNext()){
                Object primaryKey = keys.next();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey, deploymentInfo.getRemoteInterface(), this));
            }
            returnValue = proxies;
        }else if(returnValue instanceof java.util.Enumeration){
            java.util.Enumeration keys = (java.util.Enumeration)returnValue;
            java.util.Vector proxies = new java.util.Vector();
            while(keys.hasMoreElements()){
                Object primaryKey = keys.nextElement();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey, deploymentInfo.getRemoteInterface(), this));
            }
            returnValue = new org.openejb.util.ArrayEnumeration(proxies);
        }else
            returnValue = new ProxyInfo(deploymentInfo, returnValue, deploymentInfo.getRemoteInterface(), this);

        return returnValue;
    }

    /**
     * This method is used to execute the home methods which are considered global
     * in scope. 
     * 
     * Global methods use bean instances from the MethodReady pool and are not 
     * specific to on bean identity.
     * 
     * @param callMethod
     * @param args
     * @param callContext
     * @return 
     * @exception org.openejb.OpenEJBException
     */
    protected Object homeMethod(Method callMethod, Object [] args, ThreadContext callContext)
    throws org.openejb.OpenEJBException {
        org.openejb.core.DeploymentInfo deploymentInfo = (org.openejb.core.DeploymentInfo)callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operations.OP_HOME);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        return invoke(callMethod,runMethod, args, callContext);
    }

    protected void didRemove(EntityBean bean, ThreadContext callContext) throws OpenEJBException{
    }
    
    protected void removeEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
    throws org.openejb.OpenEJBException {
        callContext.setCurrentOperation(Operations.OP_REMOVE);

        TransactionPolicy txPolicy   = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext();
        txContext.callContext = callContext;

        EntityBean bean = null;
        txPolicy.beforeInvoke( bean, txContext );

        Object returnValue = null;

        try{
            // this is nessary to ensure that the ejbLoad method is execute in the context of the business method
            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext,bean);
            bean.ejbRemove();
            didRemove(bean, callContext);
            instanceManager.poolInstance(callContext,bean);
        }catch(org.openejb.SystemException se){
            txPolicy.handleSystemException( se.getRootCause(), bean, txContext );
        }catch(Exception e){// handle reflection exception
            if ( e instanceof RuntimeException ) {
                /* System Exception ****************************/
                txPolicy.handleSystemException( e, bean, txContext );
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext,bean);
                txPolicy.handleApplicationException( e, txContext );
            }
        }finally{
            txPolicy.afterInvoke( bean, txContext );
        }
    }


    /**
     * 
     * @param bean
     * @param threadContext
     * @exception org.openejb.SystemException
     */
    public void discardInstance(EnterpriseBean bean, ThreadContext threadContext) {
        if ( bean != null ) {
            try{
                instanceManager.discardInstance(threadContext,(EntityBean)bean);    
            } catch (SystemException e){
                logger.error("The instance manager encountered an unkown system exception while trying to discard the entity instance with primary key "+threadContext.getPrimaryKey());
            }
        }
    }
    

}
