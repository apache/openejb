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
package org.openejb.alt.containers.castor_cmp11;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.net.URL;
import java.net.MalformedURLException;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EnterpriseBean;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDOManager;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.exolab.castor.persist.spi.Complex;
import org.exolab.castor.persist.spi.InstanceFactory;
import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.spi.SecurityService;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;
import org.openejb.util.LinkedListStack;
import org.openejb.util.Logger;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.Stack;

/**
 * Container-Managed Persistence EntityBean container based on Castor
 *
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class CastorCMP11_EntityContainer
implements RpcContainer, TransactionContainer, CallbackInterceptor, InstanceFactory {


    /*
     * Bean instances that are currently in use are placed in the txReadyPoolMap indexed
     * by their object instance with a reference to deployment's methodReadyPoolMap entry
     * as the value.
     *
     * A bean instance is added to the txReadyPool when the fetchFreeInstance( ) method is invoked.
     *
     * When a bean is released from a transaction the entry is removed from the hashtable.
     * This can occur in the CallbackInterceptor.releasing( ) method implemented by this class
     * which is called when Castor has either committed or rollback a transaction involving the bean
     * instance OR in the TransactionScopeHandler.discardBeanInstance(), which is called when a
     * transaction fails due to a runtime exception.
     */
    protected Hashtable txReadyPoolMap = new Hashtable();

    //DMB: The actual stacks of instances should be kept in the DeploymentInfo also
    protected Hashtable pooledInstancesMap = new Hashtable();
    protected Hashtable readyInstancesMap = new Hashtable();

    /*
     * Contains all the KeyGenerator objects for each Deployment, indexed by deployment id.
     * The KeyGenerator objects provide quick extraction of primary keys from entity bean
     * classes and conversion between a primary key and a Castor Complex identity.
        DMB: Instead of looking up an KeyGenerator for the deployment, we could attach it
        to the DeploymentInfo, or a new DeploymentInfo subclass for the CMP container.
     */
//    protected HashMap keyGeneratorMap = new HashMap();

    /*
     * contains a collection of LinkListStacks indexed by deployment id. Each
     * indexed stack represents the method ready pool of for that class.
     */
    protected HashMap methodReadyPoolMap = new HashMap();

    /* The default size of the method ready bean pools. Every bean class gets its own pool of this size */
    protected int poolsize = 0;

    /*
     * The javax.ejb.EntityBean.setEntityContext(...) method is used for
     * processing bean instances returing to the method ready pool
     * This variable is esbalished in the contructor so that it doesn't
     * have to be re-obtained every time we want to passivate an entity instance.
     */
    protected static Method SET_ENTITY_CONTEXT_METHOD;

    /*
     * The javax.ejb.EntityBean.unsetEntityContext(...) method is used for
     * processing bean instances that are being evicted from memory.
     * This variable is esbalished in the contructor so that it doesn't
     * have to be re-obtained every time we want to passivate an entity instance.
     * DMB: This isn't being called anywhere.
     */
    protected static Method UNSET_ENTITY_CONTEXT_METHOD;

    /*
     * The javax.ejb.EntityBean.ejbRemove() method is used for processing bean
     * instances that are about to be deleted from the database. This variable
     * is esbalished in the contructor so that it doesn't have to be re-obtained
     * every time we want to passivate an entity instance.
     */
    protected static Method EJB_REMOVE_METHOD;

    /*
     * This static block sets up the EJB_PASSIVATE_METHOD, EJB_LOAD_METHOD, SET_ENTITY_CONTEXT_METHOD static methods, which are used
     * in the poolInstance() and obtainInstance() methods of this type. Saves method lookup cycles at runtime.
     */
    static {
        try {
            SET_ENTITY_CONTEXT_METHOD = javax.ejb.EntityBean.class.getMethod( "setEntityContext", new Class[]{javax.ejb.EntityContext.class} );
            UNSET_ENTITY_CONTEXT_METHOD = javax.ejb.EntityBean.class.getMethod( "unsetEntityContext", null );
            EJB_REMOVE_METHOD = javax.ejb.EntityBean.class.getMethod( "ejbRemove", null );
        } catch ( NoSuchMethodException nse ) {
        }
    }


    public Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.alt.util.resources" );

    // contains deployment information for each by deployed to this container
    HashMap deploymentRegistry;
    // the unique id for this container
    Object containerID = null;

    java.util.Hashtable syncWrappers = new java.util.Hashtable();

    // this map contains the Java language initial values for all all data types
    protected HashMap resetMap;

    private Properties props;

    private JDOManager localJdoManager;
    private JDOManager globalJdoManager;

    //DMB:TODO:1: make logger for life cycle info.

    /**
     * Construct this container with the specified container id, deployments,
     * container manager and properties. The properties can include the class
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
    public void init( Object id, HashMap registry, Properties properties ) throws org.openejb.OpenEJBException {
        SafeToolkit toolkit = SafeToolkit.getToolkit( "CastorCMP11_EntityContainer" );
        SafeProperties safeProps = toolkit.getSafeProperties( properties );

        int poolsize = safeProps.getPropertyAsInt(EnvProps.IM_POOL_SIZE, 100);
        String engine = safeProps.getProperty("Engine");
        String resourceName = safeProps.getProperty("ConnectorName");
        String driverClassName = safeProps.getProperty("JdbcDriver");
        String driverUrl = safeProps.getProperty("JdbcUrl");
        String username = safeProps.getProperty("UserName");
        String password = safeProps.getProperty("Password");
        init(id, registry, poolsize, engine, resourceName, driverClassName, driverUrl, username, password);
    }

    private void init(Object id, HashMap registry, int poolsize, String engine, String resourceName, String driverClassName, String driverUrl, String username, String password) throws OpenEJBException {
        this.containerID = id;
        this.deploymentRegistry = registry;

        if (registry.size() == 0){
            return;
        }

        String transactionManagerJndiName = "java:openejb/TransactionManager";

        /*
         * This block of code is necessary to avoid a chicken and egg problem.
         * The DeploymentInfo objects must have a reference to their container
         * during this assembly process, but the container is created after the
         * DeploymentInfo necessitating this loop to assign all deployment info
         * object's their containers.
         *
         * In addition the loop is leveraged for other oprations like creating
         * the method ready pool and the keyGenerator pool.
         */
        DeploymentInfo[] deploys = this.deployments();

        Map mappings = new HashMap();
        JndiTxReference txReference = new JndiTxReference();
        for ( int x = 0; x < deploys.length; x++ ) {
            org.openejb.core.DeploymentInfo di = ( org.openejb.core.DeploymentInfo ) deploys[x];
            di.setContainer( this );

            URL url = null;
            try {
                String jarPath = di.getJarPath();
                File file = new File(jarPath);

                if (file.isDirectory()){
                    file = new File(file, "META-INF");
                    file = new File(file, "cmp.mapping.xml");
                    url = file.toURL();
                } else {
                    url = file.toURL();
                    url = new URL("jar:"+ url.toExternalForm() + "!/META-INF/cmp.mapping.xml");
                }
                mappings.put(url.toExternalForm(), url);
            } catch (MalformedURLException e) {
                throw new OpenEJBException("Error locating mapping file "+url+" for deployment "+di.getDeploymentID(), e);
            }
            methodReadyPoolMap.put(di.getDeploymentID(), new LinkedListStack(poolsize / 2));

            bindTransactionManagerReference(di, transactionManagerJndiName, txReference);

            configureKeyGenerator(di);
        }


            try{
            JDOManagerBuilder jdoManagerBuilder = new JDOManagerBuilder(engine, transactionManagerJndiName);
//            File mappingFile = new File("/Users/dblevins/work/openejb3/container/openejb-core/target/test-classes/conf/default.cmp_mapping.xml");

            Collection urls = mappings.values();
            for (Iterator iterator = urls.iterator(); iterator.hasNext();) {
                URL url = (URL) iterator.next();
                logger.debug("Mapping file: "+url.toExternalForm());
                jdoManagerBuilder.addMapping(url);
            }

            globalJdoManager = jdoManagerBuilder.buildGlobalJDOManager("java:openejb/connector/"+resourceName);
            globalJdoManager.setDatabasePooling(true);
            globalJdoManager.setCallbackInterceptor(this);
            globalJdoManager.setInstanceFactory(this);

            localJdoManager = jdoManagerBuilder.buildLocalJDOManager(driverClassName, driverUrl, username, password);
            localJdoManager.setCallbackInterceptor(this);
            localJdoManager.setInstanceFactory(this);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OpenEJBException("Unable to construct the Castor JDOManager objects: "+e.getClass().getName()+": "+e.getMessage(), e);
        }

        buildResetMap();
    }
    //===============================
    // begin Container Implementation
    //

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed
     * in this container.
     *
     * @return an array of DeploymentInfo objects
     * @see org.openejb.DeploymentInfo
     * @see org.openejb.Container#deployments()
     */
    public DeploymentInfo[] deployments() {
        return( DeploymentInfo[] ) deploymentRegistry.values().toArray( new DeploymentInfo[deploymentRegistry.size()] );
    }

    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the
     * specified deployment id.
     *
     * @param deploymentID
     * @return the DeploymentInfo object associated with the bean.
     * @see org.openejb.DeploymentInfo
     * @see org.openejb.Container#getDeploymentInfo(Object)
     * @see org.openejb.DeploymentInfo#getDeploymentID()
     */
    public DeploymentInfo getDeploymentInfo( Object deploymentID ) {
        return( DeploymentInfo ) deploymentRegistry.get( deploymentID );
    }

    /**
     * Gets the type of container (STATELESS, STATEFUL, ENTITY, or MESSAGE_DRIVEN
     *
     * @return id type bean container
     */
    public int getContainerType() {
        return Container.ENTITY;
    }

    /**
     * Gets the id of this container.
     *
     * @return the id of this container.
     * @see org.openejb.Container#getContainerID
     */
    public Object getContainerID() {
        return containerID;
    }

    /**
     * Adds a bean to this container.
     * @param deploymentID the deployment id of the bean to deploy.
     * @param info the DeploymentInfo object associated with the bean.
     * @throws org.openejb.OpenEJBException
     *      Occurs when the container is not able to deploy the bean for some
     *      reason.
     */
    public void deploy( Object deploymentID, DeploymentInfo info ) throws OpenEJBException {
        HashMap registry = ( HashMap ) deploymentRegistry.clone();
        registry.put( deploymentID, info );
        deploymentRegistry = registry;
    }

    /**
     * Invokes a method on an instance of the specified bean deployment.
     *
     * @param deployID the dployment id of the bean deployment
     * @param callMethod the method to be called on the bean instance
     * @param args the arguments to use when invoking the specified method
     * @param primKey the primary key class of the bean or null if the bean does not need a primary key
     * @param securityIdentity
     * @return the result of invoking the specified method on the bean instance
     * @throws org.openejb.OpenEJBException
     * @see org.openejb.RpcContainer#invoke
     * @see org.openejb.core.stateful.StatefulContainer#invoke StatefulContainer.invoke
     */
    public Object invoke( Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity )
    throws org.openejb.OpenEJBException {
        try {
            org.openejb.core.DeploymentInfo deployInfo = ( org.openejb.core.DeploymentInfo ) this.getDeploymentInfo( deployID );

            ThreadContext callContext = ThreadContext.getThreadContext();
            callContext.set( deployInfo, primKey, securityIdentity );

            // check authorization to invoke

            boolean authorized = OpenEJB.getSecurityService().isCallerAuthorized( securityIdentity, deployInfo.getAuthorizedRoles( callMethod ) );
            if ( !authorized )
                throw new org.openejb.ApplicationException( new RemoteException( "Unauthorized Access by Principal Denied" ) );

            // process home interface methods
            Class declaringClass = callMethod.getDeclaringClass();
            String methodName = callMethod.getName();

    		if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass) ){
				if ( declaringClass != EJBHome.class && declaringClass != EJBLocalHome.class) {
                    // Its a home interface method, which is declared by the bean provider, but not a EJBHome method.
                    // only create() and find<METHOD>( ) are declared by the bean provider.
                    if ( methodName.equals( "create" ) ) {
                        // create( ) method called, execute ejbCreate() method
                        return createEJBObject( callMethod, args, callContext );
                    } else if ( methodName.startsWith( "find" ) ) {
                        // find<METHOD> called, execute ejbFind<METHOD>
                        return findEJBObject( callMethod, args, callContext );
                    } else {
                        // home method called, execute ejbHome method
                        throw new org.openejb.InvalidateReferenceException( new java.rmi.RemoteException( "Invalid method " + methodName + " only find<METHOD>( ) and create( ) method are allowed in EJB 1.1 container-managed persistence" ) );
                    }
                } else if ( methodName.equals( "remove" ) ) {
                    removeEJBObject( callMethod, args, callContext );
                    return null;
                }
	        } else if((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove") ) {
                removeEJBObject( callMethod, args, callContext );
                return null;
            }



            // retreive instance from instance manager
            callContext.setCurrentOperation( Operations.OP_BUSINESS );
            Method runMethod = deployInfo.getMatchingBeanMethod( callMethod );

            Object retValue = businessMethod( callMethod, runMethod, args, callContext );

            // see comments in org.openejb.core.DeploymentInfo.
            return deployInfo.convertIfLocalReference( callMethod, retValue );


        } finally {
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
            ThreadContext.setThreadContext( null );
        }
    }
    //
    // end ContainerManager Implementation
    //====================================


    //============================================
    // begin methods unique to this implementation
    //

    /**
     * Discards this instance so that it may be garbage collected
     *
     * @param bean
     * @param threadContext
     */
    public void discardInstance( EnterpriseBean bean, ThreadContext threadContext ) {
        if ( bean != null ) txReadyPoolMap.remove( bean );
    }

    /**
     * Obtains a bean instance from the method ready pool. If the pool is empty
     * a new instance is instantiated,
     * and the setEntityContext method is called.
     *
     * The bean instance is transitioned into the tx method ready pool before
     * its returned to the caller. this ensures it can returned to the method
     * ready pool when its released from the transaction.
     *
     * @param callContext
     * @return EntityBean
     * @exception java.lang.IllegalAccessException
     * @exception java.lang.reflect.InvocationTargetException
     * @exception java.lang.InstantiationException
     */
    public EntityBean fetchFreeInstance( ThreadContext callContext ) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        org.openejb.core.DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        /*
        Obtain the stack of instances of this deployment that are in the method ready state.
        */
        Stack methodReadyPool = ( Stack ) methodReadyPoolMap.get( deploymentInfo.getDeploymentID() );

        if ( methodReadyPool == null ) {
            // TODO:3: Localize this message
            throw new java.lang.RuntimeException( "Invalid deployment id " + deploymentInfo.getDeploymentID() + " for this container" );
        }

        /*
        Get a method ready instance from the top of the stack.
        */
        //DMB: This is funny, pop will always return null because we _never_ add
        // any instances to the stack.  What is the point of this pool?
        EntityBean bean = ( EntityBean ) methodReadyPool.pop();

        if ( bean == null ) {
            byte currentOperation = callContext.getCurrentOperation();
            try {
                bean = ( EntityBean ) deploymentInfo.getBeanClass().newInstance();
                /*
                setEntityContext executes in an unspecified transactional context.
                In this case we choose to allow it to have what every transaction
                context is current. Better then suspending it unnecessarily.
                */
                callContext.setCurrentOperation( Operations.OP_SET_CONTEXT );
                Object[] params = new javax.ejb.EntityContext[]{( javax.ejb.EntityContext ) deploymentInfo.getEJBContext()};
                //logger.debug(bean + ".setEntityContext("+params[0]+")");
                SET_ENTITY_CONTEXT_METHOD.invoke( bean, params );
            } finally {
                callContext.setCurrentOperation( currentOperation );
            }
        } else {
            // Here we need to reset all fields to their default values ( 0 for primitive types, null for pointers )
            resetBeanFields( bean, deploymentInfo );
        }
        // move the bean instance to the tx method ready pool
        txReadyPoolMap.put( bean, methodReadyPool );
        return bean;
    }


    /**
     * Processes a business method invokation
     *
     * @param callMethod
     * @param runMethod
     * @param args
     * @param callContext
     * @return Object
     * @exception org.openejb.OpenEJBException
     */
    protected Object businessMethod( Method callMethod, Method runMethod, Object[] args, ThreadContext callContext )
    throws org.openejb.OpenEJBException {

        EntityBean bean = null;

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext( callContext );

        txPolicy.beforeInvoke( bean, txContext );

        Object returnValue = null;
        try {

            Database db = getDatabase( callContext );

            bean = fetchAndLoadBean( callContext, db );
            //logger.debug("Invoking business method on "+bean);
            if ( OpenEJB.getTransactionManager().getTransaction() != null ) {
                try {
                    Key key = new Key( OpenEJB.getTransactionManager().getTransaction(),
                                       callContext.getDeploymentInfo().getDeploymentID(),
                                       callContext.getPrimaryKey() );
                    SynchronizationWrapper sync = new SynchronizationWrapper( ( ( javax.ejb.EntityBean ) bean ), key );

                    OpenEJB.getTransactionManager().getTransaction().registerSynchronization( sync );

                    syncWrappers.put( key, sync );
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }

            returnValue = runMethod.invoke( bean, args );

        } catch ( java.lang.reflect.InvocationTargetException ite ) {
            // handle enterprise bean exceptions
            if ( ite.getTargetException() instanceof RuntimeException ) {
                /* System Exception ****************************/
                txPolicy.handleSystemException( ite.getTargetException(), bean, txContext );

            } else {
                /* Application Exception ***********************/
                txPolicy.handleApplicationException( ite.getTargetException(), txContext );
            }
        } catch ( org.exolab.castor.jdo.DuplicateIdentityException e ) {
            /* Application Exception ***********************/
            //TODO:3: Localize this message
            Exception re = new javax.ejb.DuplicateKeyException( "Attempt to update an entity bean (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\") with an primary key that already exsists. Castor nested exception message = " + e.getMessage() );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.ClassNotPersistenceCapableException e ) {
            /* System Exception ****************************/
            //TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to update an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") that can not be persisted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionAbortedException e ) {
            /* System Exception ****************************/
            //TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to update an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because transaction was aborted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionNotInProgressException e ) {
            /* System Exception ****************************/
            //TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to update an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because a transaction didn't exist.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.DatabaseNotFoundException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( org.exolab.castor.jdo.PersistenceException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( Throwable e ) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                InstantiationException - if the bean instance can not be instantiated. Thrown by fetchAndLoadBean()
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } finally {
            txPolicy.afterInvoke( bean, txContext );
        }

        return returnValue;


    }

    /**
     * This method is responsible for delegating the ejbCreate() and
     * ejbPostCreate() methods on the an entity bean.  Transaction attributes are
     * applied to determine the correct transaction context.
     *
     * Allowed operations are imposed according to the EJB 1.1 specification.
     *
     * @param callMethod
     * @param args
     * @param callContext
     * @return ProxyInfo
     * @exception org.openejb.OpenEJBException
     */
    protected ProxyInfo createEJBObject( Method callMethod, Object[] args, ThreadContext callContext )
    throws org.openejb.OpenEJBException {
        org.openejb.core.DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        EntityBean bean = null;
        Object primaryKey = null;

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext( callContext );

        txPolicy.beforeInvoke( bean, txContext );


        try {

            /*
              Obtain a bean instance from the method ready pool
            */
            bean = fetchFreeInstance( callContext );

            /*
               Obtain the proper ejbCreate() method
            */
            Method ejbCreateMethod = deploymentInfo.getMatchingBeanMethod( callMethod );

            /*
              Set the context for allowed operations
            */
            callContext.setCurrentOperation( Operations.OP_CREATE );

            /*
              Invoke the proper ejbCreate() method on the instance
            */
            ejbCreateMethod.invoke( bean, args );

            int txStatus = OpenEJB.getTransactionManager().getStatus();
            if ( txStatus == Status.STATUS_ACTIVE || txStatus == Status.STATUS_NO_TRANSACTION ) {

                /*
                  Get the JDO database for this deployment
                */
                Database db = getDatabase( callContext );

                /*
                  Create a Castor Transaction if there isn't one in progress
                */
                if ( !db.isActive() ) db.begin();

                /*
                  Use Castor JDO to insert the entity bean into the database
                */
                db.create( bean );

            }

            /*
            Each bean deployment has a unique KeyGenerator that is responsible
            for two operations.
            1. Convert EJB developer defined complex primary keys to Castor
               JDO Complex objects
            2. Extract a primary key object from a loaded Entity bean instance.
            */
            KeyGenerator kg = deploymentInfo.getKeyGenerator();

            /*
            The KeyGenerator creates a new primary key and populates its fields with the
            primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
            */
            primaryKey = kg.getPrimaryKey( bean );

            /*
              place the primary key into the current ThreadContext so its available for
              the ejbPostCreate()
            */
            callContext.setPrimaryKey( primaryKey );

            /*
              Set the current operation for the allowed operations check
            */
            callContext.setCurrentOperation( Operations.OP_POST_CREATE );

            /*
              Obtain the ejbPostCreate method that matches the ejbCreate method
            */
            Method ejbPostCreateMethod = deploymentInfo.getMatchingPostCreateMethod( ejbCreateMethod );

            /*
              Invoke the ejbPostCreate method on the bean instance
            */
            ejbPostCreateMethod.invoke( bean, args );

            /*
            According to section 9.1.5.1 of the EJB 1.1 specification, the "ejbPostCreate(...)
            method executes in the same transaction context as the previous ejbCreate(...) method."

            The bean is first insterted using db.create( ) and then after ejbPostCreate( ) its
            updated using db.update(). This protocol allows for visablity of the bean after ejbCreate
            within the current trasnaction.
            */
            //DMB: Why is update commented out?
            //db.update(bean);

            /*
              Reset the primary key in the ThreadContext to null, its original value
            */
            callContext.setPrimaryKey( null );

        } catch ( java.lang.reflect.InvocationTargetException ite ) {// handle enterprise bean exceptions
            if ( ite.getTargetException() instanceof RuntimeException ) {
                /* System Exception ****************************/
                txPolicy.handleSystemException( ite.getTargetException(), bean, txContext );
            } else {
                /* Application Exception ***********************/
                txPolicy.handleApplicationException( ite.getTargetException(), txContext );
            }
        } catch ( org.exolab.castor.jdo.DuplicateIdentityException e ) {
            /* Application Exception ***********************/
            Exception re = new javax.ejb.DuplicateKeyException( "Attempt to create an entity bean (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\") with an primary key that already exsists. Castor nested exception message = " + e.getMessage() );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.ClassNotPersistenceCapableException e ) {
            /* System Exception ****************************/
            RemoteException re = new RemoteException( "Attempt to create an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") that can not be persisted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionAbortedException e ) {
            /* System Exception ****************************/
            //TransactionRolledbackException re = new TransactionRolledbackException("Attempt to create an entity bean (DeploymentID=\""+ThreadContext.getThreadContext().getDeploymentInfo().getDeploymentID()+"\") failed because transaction was aborted. Nested exception message = "+tae.getMessage()));
            RemoteException re = new RemoteException( "Attempt to create an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because transaction was aborted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionNotInProgressException e ) {
            /* System Exception ****************************/
            RemoteException re = new RemoteException( "Attempt to create an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because a transaction didn't exist.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.DatabaseNotFoundException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( org.exolab.castor.jdo.PersistenceException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( Throwable e ) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                InstantiationException - if the bean instance can not be instantiated. Thrown by fetchAndLoadBean()
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );
        } finally {
            txPolicy.afterInvoke( bean, txContext );
        }
        Class callingClass = callMethod.getDeclaringClass();
		boolean isLocalInterface = EJBLocalHome.class.isAssignableFrom(callingClass);

        return new ProxyInfo( deploymentInfo, primaryKey, isLocalInterface, this );
    }

    protected static final Object[] noArgs = new Object[0];

    /**
     * This method is used to execute the find methods which are considered
     * global in scope. Global methods use bean instances from the MethodReady
     * pool and are not specific to on bean identity.
     *
     * The return value will be either a single ProxyInfo object or collection of
     * ProxyInfo objects representing one or more remote references.
     *
     * @param callMethod
     * @param args
     * @param callContext
     * @return Object
     * @exception org.openejb.OpenEJBException
     */
    protected Object findEJBObject( Method callMethod, Object[] args, ThreadContext callContext ) throws org.openejb.OpenEJBException {

        org.openejb.core.DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        QueryResults results = null;
        Object returnValue = null;
        EntityBean bean = null;

        /* Obtain the OQL statement that matches the find method of the remote interface  */
        String queryString = deploymentInfo.getQuery( callMethod );

        /* Get the transaction policy assigned to this method */
        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );
        TransactionContext txContext = new TransactionContext( callContext );

        txPolicy.beforeInvoke( bean, txContext );


        try {

            /*
              Get the JDO database for this deployment
            */
            Database db = getDatabase( callContext );

            /*
              Create a Castor Transaction if there isn't one in progress
            */
            if ( !db.isActive() ) db.begin();

            /*
              Obtain a OQLQuery object based on the String query
            */
            OQLQuery query = db.getOQLQuery( queryString );


            if ( callMethod.getName().equals( "findByPrimaryKey" ) ) {
                // bind complex primary key to query
                KeyGenerator kg = deploymentInfo.getKeyGenerator();

                if ( kg.isKeyComplex() ) {
                    /*
                    * This code moves the fields of the primary key into a JDO Complex object
                    * which can then be used in the database.bind operation
                    */
                    org.exolab.castor.persist.spi.Complex c = kg.getJdoComplex( args[0] );
                    args = new Object[c.size()];
                    for ( int i = 0; i < args.length; i++ )
                        args[i] = c.get( i );
                }
            }


            if ( args == null ) args = noArgs;

            for ( int i = 0; i < args.length; i++ ) {
                if ( args[i] instanceof javax.ejb.EJBObject ) {
                    /*
                    Its possible that the finder method's arguments are actually EJBObject reference in
                    which case the EJBObject reference is replaced with the EJB object's primary key.
                    The limitation of this facility is that the EJB object must use a single field primary key
                    and not a complex primary key. Complex primary keys of EJBObject argumetns are not supported.
                    For Example:

                    EJB Home Interface Find method:
                    public Collection findThings(Customer customer);

                    OQL in deployment descriptor
                    "SELECT t FROM Thing t WHERE t.customer_id = $1"

                    */
                    try {
                        args[i] = ( ( javax.ejb.EJBObject ) args[i] ).getPrimaryKey();
                    } catch ( java.rmi.RemoteException re ) {
                        //TODO:3: Localize this message
                        throw new javax.ejb.FinderException( "Could not extract primary key from EJBObject reference; argument number " + i );
                    }
                }

                /*
                Bind the arguments of the home interface find method to the query.
                The big assumption here is that the arguments of the find operation
                are in the same order as the arguments in the query.  The bean developer
                must declare the OQL arguments with the proper number so that match the order
                of the find arguments.
                For Example:

                EJB Home Interface Find method:
                public Collection findThings(String name, double weight, String Type);

                OQL in deployment descriptor
                "SELECT t FROM Thing t WHERE t.weight = $2 AND t.type = $3 AND t.name = $1"
                */

                query.bind( args[i] );
            }


            /*  execute the query */
            results = query.execute();

            /*
            Each bean deployment has a unique KeyGenerator that is responsible for two operations.
            1. Convert EJB developer defined complex primary keys to Castor JDO Complex objects
            2. Extract a primary key object from a loaded Entity bean instance.
            */
            KeyGenerator kg = deploymentInfo.getKeyGenerator();

            Object primaryKey = null;
            Class callingClass = callMethod.getDeclaringClass();
    		boolean isLocalInterface = EJBLocalHome.class.isAssignableFrom(callingClass);

            /*
            The following block of code is responsible for returning ProxyInfo object(s) for each
            matching entity bean found by the query.  If its a multi-value find operation a Vector
            of ProxyInfo objects will be returned. If its a single-value find operation then a
            single ProxyInfo object is returned.
            */
            if ( callMethod.getReturnType() == java.util.Collection.class || callMethod.getReturnType() == java.util.Enumeration.class ) {
                java.util.Vector proxies = new java.util.Vector();
                while ( results.hasMore() ) {
                    /*  Fetch the next entity bean from the query results */
                    bean = ( EntityBean ) results.next();

                    /*
                    The KeyGenerator creates a new primary key and populates its fields with the
                    primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
                    */
                    primaryKey = kg.getPrimaryKey( bean );
                    /*   create a new ProxyInfo based on the deployment info and primary key and add it to the vector */
                    proxies.addElement( new ProxyInfo( deploymentInfo, primaryKey, isLocalInterface, this ) );
                }
                if ( callMethod.getReturnType() == java.util.Enumeration.class )
                    returnValue = new org.openejb.util.Enumerator( proxies );
                else
                    returnValue = proxies;
            } else {
                /*  Fetch the entity bean from the query results */
                if ( !results.hasMore() )
                    throw new javax.ejb.ObjectNotFoundException( "A Enteprise bean with deployment_id = " + deploymentInfo.getDeploymentID() + " and primarykey = " + args[0] + " Does not exist" );

                bean = ( EntityBean ) results.next();
                /*
                    The KeyGenerator creates a new primary key and populates its fields with the
                    primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
                */
                primaryKey = kg.getPrimaryKey( bean );
                /*   create a new ProxyInfo based on the deployment info and primary key */
                returnValue = new ProxyInfo( deploymentInfo, primaryKey, isLocalInterface, this );
            }

        } catch ( javax.ejb.FinderException fe ) {
            /* Application Exception *********************** thrown when attempting to extract EJBObject argument */
            txPolicy.handleApplicationException( fe, txContext );

        } catch ( org.exolab.castor.jdo.QueryException qe ) {
            /* Application Exception ***********************/
            javax.ejb.FinderException fe = new javax.ejb.FinderException( "Castor JDO could not execute query for this finder method. QueryException: " + qe.getMessage() );
            // TODO:3: Localize this message
            txPolicy.handleApplicationException( fe, txContext );

        } catch ( org.exolab.castor.jdo.TransactionNotInProgressException e ) {
            /* System Exception ****************************/
            // TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to create an entity bean (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\") failed because a transaction didn't exist.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.DatabaseNotFoundException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( org.exolab.castor.jdo.PersistenceException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( Throwable e ) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                InstantiationException - if the bean instance can not be instantiated. Thrown by fetchAndLoadBean()
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );
        } finally {
            if ( results != null ) results.close();
            txPolicy.afterInvoke( bean, txContext );
        }
        return returnValue;
    }


    /**
     * Removes the EJBObject
     *
     * @param callMethod
     * @param args
     * @param callContext
     * @exception org.openejb.OpenEJBException
     */
    protected void removeEJBObject( Method callMethod, Object[] args, ThreadContext callContext )
    throws org.openejb.OpenEJBException {
        EntityBean bean = null;
        TransactionContext txContext = new TransactionContext( callContext );
        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy( callMethod );

        txPolicy.beforeInvoke( bean, txContext );

        try {
            int status = OpenEJB.getTransactionManager().getStatus();
            // are the other statuses possible here ?
            if ( status == Status.STATUS_ACTIVE || status == Status.STATUS_NO_TRANSACTION ) {

                /*
                  Get the JDO database for this deployment
                */
                Database db = getDatabase( callContext );

                /*
                  Create a Castor Transaction if there isn't one in progress
                */
                if ( !db.isActive() ) db.begin();

                bean = fetchAndLoadBean( callContext, db );

                callContext.setCurrentOperation( Operations.OP_REMOVE );
                EJB_REMOVE_METHOD.invoke( bean, null );

                db.remove( bean );
            }
        } catch ( java.lang.reflect.InvocationTargetException ite ) {
            // handle enterprise bean exceptions
            if ( ite.getTargetException() instanceof RuntimeException ) {
                /* System Exception ****************************/
                txPolicy.handleSystemException( ite.getTargetException(), bean, txContext );
            } else {
                /* Application Exception ***********************/
                txPolicy.handleApplicationException( ite.getTargetException(), txContext );
            }
        } catch ( org.exolab.castor.jdo.DuplicateIdentityException e ) {
            /* Application Exception ***********************/
            // TODO:3: Localize this message
            Exception re = new javax.ejb.DuplicateKeyException( "Attempt to remove an entity bean (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\") with an primary key that already exsists. Castor nested exception message = " + e.getMessage() );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.ClassNotPersistenceCapableException e ) {
            /* System Exception ****************************/
            // TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to remove an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") that can not be persisted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionAbortedException e ) {
            /* System Exception ****************************/
            //TransactionRolledbackException re = new TransactionRolledbackException("Attempt to remove an entity bean (DeploymentID=\""+ThreadContext.getThreadContext().getDeploymentInfo().getDeploymentID()+"\") failed because transaction was aborted. Nested exception message = "+tae.getMessage()));
            // TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to remove an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because transaction was aborted.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.TransactionNotInProgressException e ) {
            /* System Exception ****************************/
            // TODO:3: Localize this message
            RemoteException re = new RemoteException( "Attempt to remove an entity bean (DeploymentID=\"" + txContext.callContext.getDeploymentInfo().getDeploymentID() + "\") failed because a transaction didn't exist.", e );
            txPolicy.handleSystemException( re, bean, txContext );

        } catch ( org.exolab.castor.jdo.DatabaseNotFoundException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( org.exolab.castor.jdo.PersistenceException e ) {
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );

        } catch ( Throwable e ) {// handle reflection exception
            /*

              Any exception thrown by reflection; not by the enterprise bean.
              Possible Exceptions are:
              InstantiationException -
                if the bean instance can not be instantiated. Thrown by
                fetchAndLoadBean()

              IllegalAccessException -
                if the underlying method is inaccessible.

              IllegalArgumentException -
                if the number of actual and formal parameters differ, or
                if an unwrapping conversion fails.

              NullPointerException -
                if the specified object is null and the method is an
                instance method.

              ExceptionInInitializerError -
                if the initialization provoked by this method fails.

            */
            /* System Exception ****************************/
            txPolicy.handleSystemException( e, bean, txContext );
        } finally {
            txPolicy.afterInvoke( bean, txContext );
        }
    }

    /**
     * This method is responsible for loading the bean from the database based
     * on the primary key identity contained in the callContext parameter. If
     * the primary key is complex (a custom class with one or more fields) the
     * key is converted into a Castor JDO Complex identity object which is
     * used by the Database.load() method. If the primary key is a single
     * field key (usally a primitive wrapper (Integer, Boolean, etc.) or
     * String) then the primary key is used by the Database.load() method
     * directly.
     *
     * @param callContext
     * @param db
     * @return EntityBean
     * @exception org.exolab.castor.jdo.PersistenceException
     * @exception org.exolab.castor.jdo.ObjectNotFoundException
     * @exception org.exolab.castor.jdo.TransactionNotInProgressException
     * @exception org.exolab.castor.jdo.LockNotGrantedException
     * @exception java.lang.InstantiationException
     * @exception java.lang.reflect.InvocationTargetException
     * @exception java.lang.IllegalAccessException
     */
    protected EntityBean fetchAndLoadBean( ThreadContext callContext, Database db )
    throws org.exolab.castor.jdo.PersistenceException, org.exolab.castor.jdo.ObjectNotFoundException,
    org.exolab.castor.jdo.TransactionNotInProgressException, org.exolab.castor.jdo.LockNotGrantedException,
    java.lang.InstantiationException, java.lang.reflect.InvocationTargetException,
    java.lang.IllegalAccessException {
        /*
          Each bean deployment has a unique KeyGenerator that is responsible
          for two operations.

          1. Convert EJB developer defined complex primary keys to Castor JDO
             Complex objects

          2. Extract a primary key object from a loaded Entity bean instance.
        */
        KeyGenerator kg = callContext.getDeploymentInfo().getKeyGenerator();

        /*
            obtains a bean instance from the method ready pool, or
            instantiates a new one calling setEntityContext.
            Also places the bean instance in the tx method ready pool.
        */
        EntityBean bean = null;

        /*
            Castor JDO doesn't recognize EJB complex primary keys, so if the
            key is complex it must be marshalled into a Castor JDO Complex
            object in order to perform a load operation.
        */
        if ( kg.isKeyComplex() ) {
            Complex complexIdentity = kg.getJdoComplex( callContext.getPrimaryKey() );
            /*
             * yip: Castor JDO bases on and maintains one instance of object of
             * the same type and identity in each transaction. fetchFreeInstance
             * didn't take accout of it and always return another instance; passing
             * another instance to load the same type and identity as an existing
             * object will casuses PersistenceException to be thrown. It's why "bean"
             * is commented out.
             */
            bean = ( EntityBean ) db.load( callContext.getDeploymentInfo().getBeanClass(),
                                           complexIdentity/*,
                                       bean*/ );

        } else {
            bean = ( EntityBean ) db.load( callContext.getDeploymentInfo().getBeanClass(),
                                           callContext.getPrimaryKey()/*,
                                       bean*/ );
        }

        return bean;
    }

    /**
     * If their is no transaction the CastorTransactionScopeManager.begin()
     * method would have set the unspecified value of the ThreadContext to a
     * non-transaction managed database object.
     *
     * Otherwise if their is a transction contrext, the unspecified value
     * will be null.
     *
     * This allows us to know when an operation (createEJBObject,
     * removeEJBObject, busienssMethod) requires transaction-managed Database
     * object or a non-transaction managed database object.
     *
     * @param callContext
     * @return Database
     * @exception org.exolab.castor.jdo.DatabaseNotFoundException
     * @exception org.exolab.castor.jdo.PersistenceException
     * @exception javax.transaction.SystemException
     */
    protected Database getDatabase( ThreadContext callContext )
    throws org.exolab.castor.jdo.DatabaseNotFoundException,
    org.exolab.castor.jdo.PersistenceException,
    javax.transaction.SystemException {
        /*
         If their is no transaction the CastorTransactionScopeManager.begin()
         method would have set the unspecified value of the ThreadContext to a
         non-transaction managed database object.

         Otherwise if their is a transction context, the unspecified value
         will be null.

         This allows us to know when an operation (createEJBObject,
         removeEJBObject, busienssMethod) requires transaction-managed
         Database object or a non-transaction managed database object.
         */
        Database db = ( Database ) callContext.getUnspecified();

        if ( db != null ) {
            return db;
        } else {
            /*
             BIG PROBLEM: Transacitons should use the same Database object.
             If Thomas won't put this into JDO then I'll have to put into the
             container.

             1. Check thread to see if current transacion is mapped to any
                existing Database object.

             2. If it is, return that Database object.

             3. If not obtain new Database object

             4. Register the Tranaction and Database object in a hashmap keyed
                by tx.

             5. When transaction completes, remove tx-to-database mapping from
                hashmap.

             */
            return globalJdoManager.getDatabase();
        }
    }

    /**
     Section 9.2.4 EJB 1.1:
     "The Container must ensure that the values of the container-managed fields are set to the Java language
     defaults (e.g. 0 for integer, null for pointers) prior to invoking an ejbCreate(...) method on an
     instance."
     */
    protected void resetBeanFields( java.lang.Object bean, org.openejb.core.DeploymentInfo info ) {
        final String[] cmFields = info.getCmrFields();
        final Class beanClass = bean.getClass();

        try {
            for ( int i = 0; i < cmFields.length; i++ ) {
                Field field = beanClass.getDeclaredField( cmFields[i] );
                Object value = resetMap.get( field.getType() );
//                System.out.println("Setting field "+cmFields[i]+" to "+value);
                field.set( bean, value );
            }
        } catch ( Exception e ) {
            // NoSuchFieldException or IllegalAccessException
            // internal inconistency. This should have been handled at start time.
            logger.error( "Internal inconsistency accessing the fields of a CMP entity bean" + bean + ":" + e );
        }
    }

    /******************************************************************************
     *                                                                             *
     *             CallbackInterceptor methods                                     *
     *                                                                             *
     ******************************************************************************/

    /**
     * Called to indicate that an object needs to be instatiated.
     * <p>
     * The parameters are ignored.  Data is obtained from the deployment info
     * which has been obtained, in turn, from the current call context.
     *
     * @return an instance of the object needs to be instatiated
     * @param className The name of the class of the object to be created
     * @param loader The class loader to use when creating the object
     */
    public Object newInstance( String className, ClassLoader loader ) {

        Object obj = null;

        try {
            obj = fetchFreeInstance( ThreadContext.getThreadContext() );
        } catch ( IllegalAccessException iae ) {
            throw new RuntimeException( iae.getLocalizedMessage() );
        } catch ( InvocationTargetException ite ) {
            throw new RuntimeException( ite.getLocalizedMessage() );
        } catch ( InstantiationException ie ) {
            throw new RuntimeException( ie.getLocalizedMessage() );
        }

        return obj;
    }

    /**
     * Called to indicate that the object has been loaded from persistent
     * storage.
     *
     * @return null or the extending Class. In the latter case Castor will
     * reload the object of the given class with the same identity.
     * @param object The object
     */
    public Class loaded( Object object, short accessMode ) {
        return null;
    }


    /**
     * Called to indicate that an object is to be stored in persistent
     * storage.
     *
     * @param object The object
     * @param modified Is the object modified?
     */
    public void storing( Object object, boolean modified ) {
    }

    /**
     * Called to indicate that an object is to be created in persistent
     * storage.
     *
     * @param object The object
     * @param db The database in which this object will be created
     */
    public void creating( Object object, Database db ) {
    }


    /**
     * Called to indicate that an object has been created.
     *
     * @param object The object
     */
    public void created( Object object ) {
    }


    /**
     * Called to indicate that an object is to be deleted.
     * <p>
     * This method is made at commit time on objects deleted during the
     * transaction before setting their fields to null.
     *
     * @param object The object
     */
    public void removing( Object object ) {
    }


    /**
     * Called to indicate that an object has been deleted.
     * <p>
     * This method is called during db.remove().
     *
     * @param object The object
     */
    public void removed( Object object ) {
    }


    /**
     * Called to indicate that an object has been made transient.
     * <p>
     * This method is made at commit or rollback time on all objects
     * that were presistent during the life time of the transaction.
     *
     * @param object The object
     * @param committed True if the object has been commited, false
     *  if rollback or otherwise cancelled
     */
    public void releasing( Object object, boolean committed ) {
        /*
        Every time a bean instance is fetched using fetchFreeInstance( ) it is
        automatically added to the txReadyPoolMap indexed by the bean instance
        with the value being the MethodReadPool.

        This allows bean instances to be pooled as this is the Castor JDO only
        method that provides any notification that a bean instance is no
        longer in use.
        */

        LinkedListStack stack = ( LinkedListStack ) txReadyPoolMap.remove( object );
        if ( stack != null ) stack.push( object );
    }


    /**
     * Called to indicate that an object has been made persistent.
     *
     * @param object The object
     * @param db The database to which this object belongs
     */
    public void using( Object object, Database db ) {
    }


    /**
     * Called to indicate that an object has been updated at the end of
     * a "long" transaction.
     *
     * @param object The object
     */
    public void updated( Object object ) {
    }

    public JDOManager getGlobalTxJDO() {
        return globalJdoManager;
    }

    public JDOManager getLocalTxJDO() {
        return localJdoManager;
    }

    public static class Key {
        Object deploymentID, primaryKey;
        Transaction transaction;

        public Key( Transaction tx, Object depID, Object prKey ) {
            transaction = tx;
            deploymentID = depID;
            primaryKey = prKey;
        }

        public int hashCode() {
            return transaction.hashCode() ^ deploymentID.hashCode() ^ primaryKey.hashCode();
        }

        public boolean equals( Object other ) {
            if ( other != null && other.getClass() == CastorCMP11_EntityContainer.Key.class ) {
                Key otherKey = ( Key ) other;
                if ( otherKey.transaction.equals( transaction ) && otherKey.deploymentID.equals( deploymentID ) && otherKey.primaryKey.equals(
                                                                                                                                             primaryKey ) )
                    return true;
            }
            return false;
        }
    }

    public class SynchronizationWrapper
    implements javax.transaction.Synchronization {
        EntityBean bean;
        Key myIndex;

        public SynchronizationWrapper( EntityBean ebean, Key key ) {
            bean = ebean;
            myIndex = key;
        }

        public void beforeCompletion() {
            try {
                bean.ejbStore();
            } catch ( Exception re ) {
                javax.transaction.TransactionManager txmgr = OpenEJB.getTransactionManager();
                try {
                    txmgr.setRollbackOnly();
                } catch ( javax.transaction.SystemException se ) {
                    // log the exception
                }

            }
        }

        public void afterCompletion( int status ) {
            syncWrappers.remove( myIndex );
        }

    }

    /*
     *  (non-Javadoc)
     * @see org.exolab.castor.persist.spi.CallbackInterceptor#loaded(java.lang.Object, org.exolab.castor.mapping.AccessMode)
     */
	public Class loaded(Object loaded, AccessMode mode) throws Exception {
		return loaded(loaded, mode.getId());
	}

    private void buildResetMap() {
        resetMap = new HashMap();
        resetMap.put(byte.class, new Byte((byte) 0));
        resetMap.put(boolean.class, new Boolean(false));
        resetMap.put(char.class, new Character((char) 0));
        resetMap.put(short.class, new Short((short) 0));
        resetMap.put(int.class, new Integer(0));
        resetMap.put(long.class, new Long(0));
        resetMap.put(float.class, new Float(0));
        resetMap.put(double.class, new Double(0.0));
    }

    /**
     * Castor JDO obtains a reference to the TransactionManager throught the InitialContext.
     * The new InitialContext will use the deployment's JNDI Context, which is normal inside
     * the container system, so we need to bind the TransactionManager to the deployment's name space
     * The biggest problem with this is that the bean itself may access the TransactionManager if it
     * knows the JNDI name, so we bind the TransactionManager into dynamically created transient name
     * space based every time the container starts. It nearly impossible for the bean to anticipate
     * and use the binding directly.  It may be possible, however, to locate it using a Context listing method.
     */
    private void bindTransactionManagerReference(org.openejb.core.DeploymentInfo di, String transactionManagerJndiName, JndiTxReference txReference) throws org.openejb.SystemException {
        try {
            di.getJndiEnc().bind(transactionManagerJndiName, txReference);
        } catch (Exception e) {
            logger.error("Unable to bind TransactionManager to deployment id = " + di.getDeploymentID() + " using JNDI name = \"" + transactionManagerJndiName + "\"", e);
            throw new org.openejb.SystemException("Unable to bind TransactionManager to deployment id = " + di.getDeploymentID() + " using JNDI name = \"" + transactionManagerJndiName + "\"", e);
        }
    }

    private void configureKeyGenerator(org.openejb.core.DeploymentInfo di) throws org.openejb.SystemException {
        KeyGenerator kg = null;
        try {
            kg = KeyGeneratorFactory.createKeyGenerator(di);
            di.setKeyGenerator(kg);
        } catch (Exception e) {
            logger.error("Unable to create KeyGenerator for deployment id = " + di.getDeploymentID(), e);
            throw new org.openejb.SystemException("Unable to create KeyGenerator for deployment id = " + di.getDeploymentID(), e);
        }

        try {
            StringBuffer findByPrimarKeyQuery = new StringBuffer("SELECT e FROM " + di.getBeanClass().getName() + " e WHERE ");

            if (kg.isKeyComplex()) {

                Field[] pkFields = di.getPrimaryKeyClass().getFields();
                for (int i = 1; i <= pkFields.length; i++) {
                    findByPrimarKeyQuery.append("e." + pkFields[i - 1].getName() + " = $" + i);
                    if ((i + 1) <= pkFields.length)
                        findByPrimarKeyQuery.append(" AND ");
                }

            } else {
                findByPrimarKeyQuery.append("e." + di.getPrimaryKeyField().getName() + " = $1");
            }

            if (di.getHomeInterface() != null) {
                Method findByPrimaryKeyMethod = di.getHomeInterface().getMethod("findByPrimaryKey", new Class[]{di.getPrimaryKeyClass()});
                di.addQuery(findByPrimaryKeyMethod, findByPrimarKeyQuery.toString());
            }
            if (di.getLocalHomeInterface() != null) {
                Method findByPrimaryKeyMethod = di.getLocalHomeInterface().getMethod("findByPrimaryKey", new Class[]{di.getPrimaryKeyClass()});
                di.addQuery(findByPrimaryKeyMethod, findByPrimarKeyQuery.toString());
            }
        } catch (Exception e) {
            throw new org.openejb.SystemException("Could not generate a query statement for the findByPrimaryKey method of the deployment = " + di.getDeploymentID(), e);
        }
    }
}