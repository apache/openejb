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


package org.openejb.core.stateful;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.IntraVmCopyMonitor;
import org.openejb.util.Logger;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;

/**
 * 
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class StatefulInstanceManager {

    /**
     * Represents the time-out period for a stateful bean instance in milliseconds.
     * Measured as the time between method invocations.
     */
    protected long timeOUT = 0;

    /**
     * This index keeps track of all beans that are not passivated.  A bean in the
     * method ready or "method ready in transaction" pools will be in this index. 
     * Passivated beans are not in this index.
     */
    protected Hashtable beanINDEX = new Hashtable();

    /**
     * This colleciton keeps track of all beans that are in the method ready pool 
     * and are not passivated.  Beans that are enrolled in a current transaction 
     * are NOT elements of this collection.  Only beans in the lruQUE may be 
     * passivated or timeout.
     */
    protected BeanEntryQue lruQUE;// que of beans for LRU algorithm

    /**
     * The passivator is responsible for writing beans to disk at passivation time.
     * Different passivators can be used and are chosen by setting the 
     * EnvProps.IM_PASSIVATOR property used in initialization of this instance to 
     * a the fully qualified class name of the PassivationStrategy. The passivator 
     * is not responsible for invoking any callbacks or other processing. Its only
     * responsibly is to write the bean state to disk.
     */
    protected PassivationStrategy passivator;

    /**
     * Timeout Manager
     */
    protected int BULK_PASSIVATION_SIZE = 100;

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");

    /**
     * CONSTRUCTOR METHODS
     */
    public StatefulInstanceManager( ) {
    }

    /**********************************************************************
            InstanceManager INTERFACE METHODS
    ***********************************************************************/
    /**
     * Fully instaniates this instance manager and assigns it to the specified 
     * ContainerManager. The properities passed in a re retrieved from the section
     * of the OpenEJB XML config that defines this instance manager.
     * 
     * @param props  the properties the instance manager needs to fully initialize and run
     * @exception OpenEJBException
     *                   if there is a problem initializing this instance manager
     */
    public void init(Properties props)
    throws OpenEJBException{

        SafeProperties safeProps = toolkit.getSafeProperties(props);

        String passivatorClass=null;
        try {
            passivatorClass = safeProps.getProperty(EnvProps.IM_PASSIVATOR);
        } catch ( org.openejb.OpenEJBException e ) {
            // try old property name for backward compat
            try {
                passivatorClass = safeProps.getProperty("org/openejb/core/InstanceManager/PASSIVATOR");
            } catch ( org.openejb.OpenEJBException e1 ) {
                //throw old exception
                throw e;
            }
        }

        try {
            passivator = (PassivationStrategy)toolkit.newInstance(passivatorClass);
        } catch ( Exception e ) {
            OpenEJBErrorHandler.propertyValueIsIllegal(EnvProps.IM_PASSIVATOR, passivatorClass, e.getLocalizedMessage());
        }
        passivator.init(props);

        int poolSize = safeProps.getPropertyAsInt(EnvProps.IM_POOL_SIZE, 100);
        int timeOutInMinutes = safeProps.getPropertyAsInt(EnvProps.IM_TIME_OUT,5);
        int bulkPassivationSize = safeProps.getPropertyAsInt(EnvProps.IM_PASSIVATE_SIZE,(int)(poolSize*.25));


        lruQUE = new BeanEntryQue(poolSize);
        BULK_PASSIVATION_SIZE = (bulkPassivationSize > poolSize)? poolSize: bulkPassivationSize;
        timeOUT = timeOutInMinutes*60*1000; // (minutes x 60 sec x 1000 milisec)
    }

    /**
     * Gets the ancillary state object of the instance with the specified 
     * primaryKey.
     * 
     * The Ancillary state object is used to hold additional information specific 
     * to the bean instance that is not captured by the instance itself.  
     * 
     * Example: 
     * 
     * The org.openejb.core.StatefulContainer uses a ancillary object to store the
     * client identity (unique identity of the JVM and computer) that created the 
     * stateful bean instance.
     * 
     * @param primaryKey the primary key of the bean instance
     * @return the ancillary state object
     * @exception OpenEJBException
     *                   if there is a problem retrieving the ancillary state object
     * @see SessionKey
     */
    public Object getAncillaryState(Object primaryKey)
    throws OpenEJBException{
        return this.getBeanEntry(primaryKey).ancillaryState;
    }

    /**
     * Sets the ancillary state of the bean instance with the specified primaryKey
     * 
     * Setting the ancillary state after modifing it is not necessary, because 
     * getAncillary state returns an object reference.
     * 
     * @param primaryKey the unique key that can identify the instance being managed
     * @param ancillaryState
     *                   the new ancillary state of the bean instance in this instance manager
     * @exception OpenEJBException
     *                   if there is a problem setting the ancillary state object
     * @see SessionKey
     */
    public void setAncillaryState(Object primaryKey, Object ancillaryState)
    throws OpenEJBException{
        BeanEntry entry = getBeanEntry(primaryKey);
        entry.ancillaryState = ancillaryState;
        if ( ancillaryState instanceof javax.transaction.Transaction )
            entry.transaction = (javax.transaction.Transaction)ancillaryState;

    }

    /**
     * Instantiates and returns an new instance of the specified bean class.
     * 
     * @param primaryKey the unique key that can identify the instance being managed
     * @param beanClass  the type of the bean's class
     * @return an new instance of the bean class
     * @exception OpenEJBException
     *                   if there is a problem initializing the bean class
     * @see SessionKey
     */
    public EnterpriseBean newInstance(Object primaryKey, Class beanClass)
    throws OpenEJBException{
        return this.newInstance(primaryKey,null, beanClass);
    }

    /**
     * Instantiates and returns an new instance of the specified bean class.
     * 
     * @param primaryKey the unique key that can identify the instance being managed
     * @param ancillaryState
     *                   the ancillary state of the bean instance in this instance manager
     * @param beanClass  the type of the bean's class
     * @return an new instance of the bean class
     * @exception OpenEJBException
     *                   if there is a problem initializing the bean class
     * @see SessionKey
     */
    public EnterpriseBean newInstance(Object primaryKey, Object ancillaryState, Class beanClass)
    throws OpenEJBException{

        SessionBean bean = null;

        try {
            bean = (SessionBean)toolkit.newInstance(beanClass);
        } catch ( OpenEJBException oee ) {
            logger.error("Can't instantiate new instance of class +"+beanClass.getName()+". Received exception "+oee, oee);
            throw (SystemException)oee;
        }

        ThreadContext thrdCntx = ThreadContext.getThreadContext();
        byte currentOp = thrdCntx.getCurrentOperation();
        thrdCntx.setCurrentOperation(Operations.OP_SET_CONTEXT);
        try {
            bean.setSessionContext((javax.ejb.SessionContext)thrdCntx.getDeploymentInfo().getEJBContext());
        } catch ( Throwable callbackException ) {
            /*
            In the event of an exception, OpenEJB is required to log the exception, evict the instance,
            and mark the transaction for rollback.  If there is a transaction to rollback, then the a
            javax.transaction.TransactionRolledbackException must be throw to the client. Otherwise a
            java.rmi.RemoteException is thrown to the client.
            See EJB 1.1 specification, section 12.3.2
            See EJB 2.0 specification, section 18.3.3
            */
            handleCallbackException(callbackException, bean, thrdCntx, "setSessionContext");
        } finally {
            thrdCntx.setCurrentOperation(currentOp);
        }


        BeanEntry entry = new BeanEntry(bean,primaryKey, ancillaryState, timeOUT);

        beanINDEX.put(primaryKey, entry);


        return entry.bean;
    }

    /**
     * Gets a previously instantiated instance of the bean class with the 
     * specified primaryKey
     * 
     * @param primaryKey the unique key that can identify the instance to return
     * @return an instance of the bean class
     * @exception OpenEJBException
     *                   if there is a problem retreiving the instance from the pool
     * @exception InvalidateReferenceException
     *                   if the instance has timed out
     * @see SessionKey
     */
    public SessionBean obtainInstance(Object primaryKey, ThreadContext callContext)throws OpenEJBException{
        if ( primaryKey == null ) {
            throw new org.openejb.SystemException( new NullPointerException("Cannot obtain an instance of the stateful session bean with a null session id"));
        }

        BeanEntry entry = (BeanEntry)beanINDEX.get(primaryKey);
        if ( entry == null ) {
            // if the bean is not in the beanINDEX then it must either be passivated or it doesn't exist.
            entry = activate(primaryKey);
            if ( entry != null ) {
                // the bean instance was passivated
                if ( entry.isTimedOut() ) {
                    /* Since the bean instance hasn't had its ejbActivate() method called yet, 
                       it is still considered to be passivated at this point. Instances that timeout 
                       while passivated must be evicted WITHOUT having their ejbRemove() 
                       method invoked. Section 6.6 of EJB 1.1 specification.  
                    */
                    throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Timed Out"));
                }
                // invoke ejbActivate( ) on bean
                byte currentOp = callContext.getCurrentOperation();
                callContext.setCurrentOperation(Operations.OP_ACTIVATE);

                try {
                    entry.bean.ejbActivate( );
                } catch ( Throwable callbackException ) {
                    /*
                    In the event of an exception, OpenEJB is required to log the exception, evict the instance,
                    and mark the transaction for rollback.  If there is a transaction to rollback, then the a
                    javax.transaction.TransactionRolledbackException must be throw to the client. Otherwise a
                    java.rmi.RemoteException is thrown to the client.
                    See EJB 1.1 specification, section 12.3.2
                    */
                    handleCallbackException(callbackException, entry.bean, callContext, "ejbActivate");
                } finally {
                    callContext.setCurrentOperation(currentOp);
                }

                beanINDEX.put(primaryKey, entry);
                return entry.bean;
            } else {
                throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Not Found"));
            }
        } else {// bean has been created and is pooled
            if ( entry.transaction != null ) { // the bean is pooled in the "tx method ready" pool.
                // session beans can only be accessed by one transaction at a time.
                // session beans involved in a transaction can not time out.
                try {
                    if ( entry.transaction.getStatus() == Status.STATUS_ACTIVE ) {
                        // Unfortunately, we can't enforce this right here, because the
                        // calling sequence in the stateful container calls beforeInvoke AFTER
                        // obtainInstance(), so that the transaction hasn't been resumed yet.
                        // the transaction assocaite with the thread must be the same as the trasnaction associated with the bean entry
//                        if(entry.transaction.equals(OpenEJB.getTransactionManager().getTransaction()))
                        return entry.bean;
//                        else
//                            throw new ApplicationException(new javax.transaction.InvalidTransactionException());
                    } else {
                        // the tx assoc with the entry must have commited or rolledback since the last request.
                        // this means that the bean is not assicated with a live tx can can service the new thread.
                        entry.transaction = null;
                        return entry.bean;
                    }
                } catch ( javax.transaction.SystemException se ) {
                    throw new org.openejb.SystemException(se);
                } catch ( IllegalStateException ise ) {
                    throw new org.openejb.SystemException(ise);
                } catch ( java.lang.SecurityException lse ) {
                    throw new org.openejb.SystemException(lse);
                }
            } else {// bean is pooled in the "method ready" pool.
                // locate bean and return it
                BeanEntry queEntry = lruQUE.remove(entry);// remove from Que so its not passivated while in use
                if ( queEntry != null ) {
                    if ( entry.isTimedOut() ) {
                        // dereference bean instance for GC
                        entry = (BeanEntry)beanINDEX.remove(entry.primaryKey );// remove frm index
                        handleTimeout(entry, callContext);
                        throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Stateful SessionBean has timed-out"));
                    }
                    return entry.bean;
                } else {
                    byte currentOperation = callContext.getCurrentOperation();
                    if ( currentOperation == Operations.OP_AFTER_COMPLETION || currentOperation == Operations.OP_BEFORE_COMPLETION ) {
                        return entry.bean;
                    } else {
                        // if the entry was not in the que and its synchronization methods are not being exeuted, then its in use and this is a concurrent call. Not allowed.
                        throw new ApplicationException(new RemoteException("Concurrent calls not allowed"));
                    }
                }
            }
        }
    }

    protected void handleTimeout(BeanEntry entry, ThreadContext thrdCntx) {

        // current operation to ejbRemove
        byte currentOp = thrdCntx.getCurrentOperation();
        thrdCntx.setCurrentOperation(Operations.OP_REMOVE);

        // instances that timeout while in the method ready pool must have their ejbRemove() 
        // method invoked before being evicted. Section 6.6 of EJB 1.1 specification.
        try {
            entry.bean.ejbRemove();
        } catch ( Throwable callbackException ) {
            /*
              Exceptions are processed "quietly"; they are not reported to the client since 
              the timeout that caused the ejbRemove() operation did not, "technically", take 
              place in the context of a client call. Logically, it may have timeout sometime 
              before the client call.
            */
            String logMessage = "An unexpected exception occured while invoking the ejbRemove method on the timed-out Stateful SessionBean instance; "+callbackException.getClass().getName()+" "+callbackException.getMessage();

            /* [1] Log the exception or error */
            logger.error( logMessage );

        } finally {
            logger.info("Removing the timed-out stateful session bean instance "+entry.primaryKey );
            thrdCntx.setCurrentOperation(currentOp);
        }
    }
    /**
     * Hands an instance of the bean class over to this instance manager to be 
     * managed until the instace is needed again.
     * 
     * @param primaryKey the unique key that can identify the instance being managed
     * @param bean       an instance of the bean class
     * @exception OpenEJBException
     *                   if there is a problem adding the instance to the pool
     * @see SessionKey
     */
    public void poolInstance(Object primaryKey, EnterpriseBean bean)
    throws OpenEJBException{
        if ( primaryKey == null || bean == null )
            throw new SystemException("Invalid arguments");

        BeanEntry entry = (BeanEntry)beanINDEX.get(primaryKey);
        if ( entry == null ) {
            entry = activate(primaryKey);
            if ( entry == null ) {
                throw new SystemException("Invalid primaryKey:"+primaryKey);
            }
        } else if ( entry.bean != bean )
            throw new SystemException("Invalid ID for bean");


        if ( entry.transaction!=null && entry.transaction == entry.ancillaryState ) {
            // only Bean Managed Transaction beans will have their ancillary ...
            // ... state and transaction variable equal to the same object
            return;// don't put in LRU (method ready) pool.
        } else {
            try {
                entry.transaction = OpenEJB.getTransactionManager().getTransaction();
            } catch ( javax.transaction.SystemException se ) {
                throw new org.openejb.SystemException("TransactionManager failure");
            }

            if ( entry.transaction == null ) {// only put in LRU if no current transaction
                lruQUE.add(entry);// add it to end of Que; the most reciently used bean
            }
        }
    }

    /**
     * Permanently removes the bean instance with the specified primaryKey from 
     * this instance manager's pool.  The primaryKey will be of type SessionKey
     * 
     * @param primaryKey the unique key that can identify the instance to be freed
     * @return 
     * @exception OpenEJBException
     *                   if there is a problem removing the bean instance from the pool
     * @exception org.openejb.SystemException
     * @see Sessionkey
     */
    public EnterpriseBean freeInstance(Object primaryKey)
    throws org.openejb.SystemException{
        BeanEntry entry = null; 
        entry = (BeanEntry)beanINDEX.remove(primaryKey);// remove frm index
        if ( entry == null ) {
            entry = activate(primaryKey);
        } else {
            lruQUE.remove(entry); // remove from que
        }

        if ( entry == null )
            return null;

        // bean instanance and entry should be dereferenced and ready for garbage collection
        return entry.bean;
    }

    /**
     * PASSIVATION
     * 
     * @exception SystemException
     */
    protected void passivate( ) throws SystemException {
        final ThreadContext thrdCntx = ThreadContext.getThreadContext();
        Hashtable stateTable = new Hashtable(BULK_PASSIVATION_SIZE);

        BeanEntry currentEntry;
        final byte currentOp = thrdCntx.getCurrentOperation();
        try {
            for ( int i=0; i<BULK_PASSIVATION_SIZE; ++i ) {
                currentEntry=lruQUE.first();
                if ( currentEntry==null )
                    break;
                beanINDEX.remove(currentEntry.primaryKey);
                if ( currentEntry.isTimedOut() ) {
                    handleTimeout(currentEntry, thrdCntx);
                } else {
                    thrdCntx.setCurrentOperation(Operations.OP_PASSIVATE);
                    try {
                        // invoke ejbPassivate on bean instance
                        currentEntry.bean.ejbPassivate();
                    } catch ( Throwable e ) {
                        //FIXME: Register exception so it can thrown when client attempt to obtin this entry
                        //WHY? A NoSuchObjectException will be thrown.
                        String logMessage = "An unexpected exception occured while invoking the ejbPassivate method on the Stateful SessionBean instance; "+e.getClass().getName()+" "+e.getMessage();

                        /* [1] Log the exception or error */
                        logger.error( logMessage );
                    }
                    stateTable.put(currentEntry.primaryKey,  currentEntry);
                }
            }
        } finally {
            thrdCntx.setCurrentOperation(currentOp);
        }

        /*
           the IntraVmCopyMonitor.prePssivationOperation() demarcates 
           the begining of passivation; used by EjbHomeProxyHandler, 
           EjbObjectProxyHandler, IntraVmMetaData, and IntraVmHandle 
           to deterime how serialization for these artifacts.
        */
        try {
            IntraVmCopyMonitor.prePassivationOperation();
            // pass the beans to the PassivationStrategy to write to disk.
            passivator.passivate(stateTable);
        } finally {
            // demarcate the end of passivation.
            IntraVmCopyMonitor.postPassivationOperation();
        }
    }
    protected BeanEntry activate(Object primaryKey) throws SystemException {
        return(BeanEntry)passivator.activate(primaryKey);
    }

    /**
     * 
     * @param entry
     * @param t
     * @return 
     * @exception org.openejb.SystemException
     */
    protected org.openejb.InvalidateReferenceException destroy(BeanEntry entry, Exception t) 
    throws org.openejb.SystemException{

        beanINDEX.remove(entry.primaryKey);// remove frm index
        lruQUE.remove(entry);// remove from que
        if ( entry.transaction != null ) {
            try {
                entry.transaction.setRollbackOnly();
            } catch ( javax.transaction.SystemException se ) {
                throw new org.openejb.SystemException(se);
            } catch ( IllegalStateException ise ) {
                throw new org.openejb.SystemException("Attempt to rollback a non-tx context",ise);
            } catch ( java.lang.SecurityException lse ) {
                throw new org.openejb.SystemException("Container not authorized to rollback tx",lse);
            }
            return new org.openejb.InvalidateReferenceException(new javax.transaction.TransactionRolledbackException(t.getMessage()));
        } else if ( t instanceof RemoteException )
            return new org.openejb.InvalidateReferenceException(t);
        else {
            EJBException ejbE = (EJBException)t;
            return new org.openejb.InvalidateReferenceException(new RemoteException(ejbE.getMessage(), ejbE.getCausedByException()));
        }


        //FIXME: Spec 12.3.6 release all resources

    }
    /**
     * Used by get/setAncillaryState( ) methods
     * 
     * @param primaryKey
     * @return 
     * @exception OpenEJBException
     */
    protected BeanEntry getBeanEntry(Object primaryKey)
    throws OpenEJBException{
        if ( primaryKey == null ) {
            throw new SystemException(new NullPointerException("The primary key is null. Cannot get the bean entry"));
        }
        BeanEntry entry = (BeanEntry)beanINDEX.get(primaryKey);
        if ( entry == null ) {
            EnterpriseBean bean = this.obtainInstance(primaryKey, ThreadContext.getThreadContext());
            this.poolInstance(primaryKey,bean);
            entry = (BeanEntry)beanINDEX.get(primaryKey);
        }
        return entry;
    }


    /**
     * INNER CLASSS
     */
    class BeanEntryQue {
        private final java.util.LinkedList list;
        private final int capacity;

        protected BeanEntryQue(int preferedCapacity) {
            capacity = preferedCapacity;
            list = new java.util.LinkedList();
        }

        protected synchronized BeanEntry  first( ) {
            return(BeanEntry)list.removeFirst();
        }

        protected synchronized void  add(BeanEntry entry) throws org.openejb.SystemException {
            if ( list.size() >= capacity ) {// is the LRU QUE full?
                passivate();
            }
            entry.resetTimeOut();
            // add it to end of Que; the most reciently used bean
            list.addLast(entry);
            entry.inQue = true;
        }
        protected synchronized BeanEntry remove(BeanEntry entry) {
            if ( !entry.inQue )
                return null;
            if ( list.remove(entry)==true ) {
                entry.inQue = false;
                return entry;
            } else {
                // e.g. cleanup from freeInstance()
                return null;
            }
        }
    }

    public Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.util.resources" );


    protected void handleCallbackException(Throwable e, EnterpriseBean instance, ThreadContext callContext, String callBack) throws ApplicationException, org.openejb.SystemException{

        // EJB 2.0: 18.3.3 Exceptions from container-invoked callbacks
        String remoteMessage = "An unexpected exception occured while invoking the "+callBack+" method on the Stateful SessionBean instance";
        String logMessage = remoteMessage +"; "+e.getClass().getName()+" "+e.getMessage();

        /* [1] Log the exception or error */
        logger.error( logMessage );

        /* [2] If the instance is in a transaction, mark the transaction for rollback. */
        Transaction tx = null;
        try {
            tx = getTxMngr().getTransaction();
        } catch ( Throwable t ) {
            logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the "+callBack+" method of bean "+callContext.getPrimaryKey());
        }
        if ( tx !=  null ) markTxRollbackOnly( tx );


        /* [3] Discard the instance */
        freeInstance( callContext.getPrimaryKey() );

        /* [4] throw the java.rmi.RemoteException to the client */
        if ( tx == null ) {
            throw new InvalidateReferenceException( new RemoteException( remoteMessage, e ) );
        } else {
            throw new InvalidateReferenceException( new javax.transaction.TransactionRolledbackException( logMessage ) );
        }

    }


    protected void markTxRollbackOnly( Transaction tx ) throws SystemException{
        try {
            if ( tx != null ) tx.setRollbackOnly();
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    protected TransactionManager getTxMngr( ) {
        return OpenEJB.getTransactionManager();
    }

}

