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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

/**
 * This class intercepts requests to the TransactonManager so that it
 * can provide wrappers for javax.transaction.Transaction objects. 
 * The Transaction wrappers allow Synchronization objects can be more
 * finely managed. This allows, for example, enterprise beans to have
 * their synchronization callback methods executed before
 * synchronization objects registered by the Persistence Manager 
 * instances or Connectors.
 * <p>
 * Synchronized objects can be registered in groups organized by 
 * priority. The Synchronization group with the highest priority, 
 * priority = 1, is handled first, so that all of 1st (priority=1) 
 * synchronization group beforeCompletion() and afterCompletion( ) 
 * methods are executed first.  The synchronization group with the 
 * second highest priority (priority = 2) is handled second and so on.
 * <p>
 * Their are 3 priorities (1, 2, and 3).  Synchronization objects may
 * be added with any one of these priorities.  If a Synchronization 
 * object is added with a priority higher then 3, its added to the 
 * third priority group. If a Synchronization object is added with a
 * priority lower then 1, its added to the first priority group.
 * <p>
 * Within a synchronization group, Synchronization objects are 
 * handled in the order they were registered. The first 
 * Synchronization object added to the group is handled first.
 * <p>
 * All the beforeCompletion() methods on all the Synchronization 
 * objects will be executed before any of the afterCompletion() 
 * methods are executed. Both are executed according to priority and
 * order registered.
 */
public class TransactionManagerWrapper  implements TransactionManager {
    /**
     * Transaction Manager Instance
     * 
     * @see org.openejb.spi.TransactionService
     */
    TransactionManager transactionManager;
    /**
     */
    Hashtable wrapperMap = new Hashtable();

    /**
     * Constructor
     * 
     * @param txMngr The Transaction Manager plugged into OpenEJB
     */
    public TransactionManagerWrapper(TransactionManager txMngr) {
        transactionManager = txMngr;
    }
    
    public javax.transaction.TransactionManager getTxManager() {
        return transactionManager;    
    }

    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @exception javax.transaction.SystemException
     * @exception javax.transaction.NotSupportedException
     */
    public void begin( )throws javax.transaction.SystemException, javax.transaction.NotSupportedException{
        transactionManager.begin();
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @exception javax.transaction.SystemException
     * @exception javax.transaction.RollbackException
     * @exception javax.transaction.HeuristicRollbackException
     * @exception javax.transaction.HeuristicMixedException
     */
    public void commit()throws javax.transaction.SystemException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException   {
        transactionManager.commit();
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @return 
     * @exception javax.transaction.SystemException
     */
    public int getStatus()throws javax.transaction.SystemException{
        return transactionManager.getStatus();
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @return 
     * @exception javax.transaction.SystemException
     */
    public Transaction getTransaction( )throws javax.transaction.SystemException{
        return getTxWrapper(transactionManager.getTransaction());
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @param tx
     * @exception javax.transaction.SystemException
     * @exception javax.transaction.InvalidTransactionException
     */
    public void resume(Transaction tx)
    throws javax.transaction.SystemException, javax.transaction.InvalidTransactionException{
        if ( tx instanceof TransactionWrapper ) {
            tx = ((TransactionWrapper)tx).transaction;
        }
        transactionManager.resume(tx);
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @return 
     * @exception javax.transaction.SystemException
     */
    public Transaction suspend( )throws javax.transaction.SystemException{
        return getTxWrapper(transactionManager.suspend());
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @exception javax.transaction.SystemException
     */
    public void rollback()throws javax.transaction.SystemException{
        transactionManager.rollback();
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @exception javax.transaction.SystemException
     */
    public void setRollbackOnly( )throws javax.transaction.SystemException{
        transactionManager.setRollbackOnly();
    }
    /**
     * Delegates the call to the Transaction Manager 
     * passed into the constructor.
     * 
     * @param x
     * @exception javax.transaction.SystemException
     */
    public void setTransactionTimeout(int x)throws javax.transaction.SystemException{
        transactionManager.setTransactionTimeout(x);
    }

    /**
     * Returns the wrapper for a given transaction
     * 
     * @param tx
     * @return 
     * @exception javax.transaction.SystemException
     */
    public Transaction getTxWrapper(Transaction tx)throws javax.transaction.SystemException{
        if ( tx == null )return null;
        
        if(tx.getStatus()==javax.transaction.Status.STATUS_COMMITTED||
           tx.getStatus()==javax.transaction.Status.STATUS_ROLLEDBACK)
          return null;

        TransactionWrapper txW = (TransactionWrapper)wrapperMap.get(tx);
        if ( txW==null ) {
            txW = new TransactionWrapper(tx);
            if ( tx.getStatus()== Status.STATUS_ACTIVE ) {
                try {
                    tx.registerSynchronization(txW);
                } catch ( javax.transaction.RollbackException re ) {
                    /* 
                    * Its not possible to enlist the TransactionWrapper as a Synchronization if the transaction
                    * has already been marked for rollback.  We don't want to propagate this exception.
                    */
                }
            }
            wrapperMap.put(tx,txW);
        }
        return txW;
    }



    /**
     * Wraps the Transaction Manager's transaction implementation to
     * facilitate a finer grain control for the Container.
     */
    public class TransactionWrapper 
    implements Transaction, javax.transaction.Synchronization {

        /**
         * The Transaction Manager's transaction instance.
         */
        Transaction transaction;
        /**
         * TODO: Add comment
         */
        Vector registeredSynchronizations;
        /**
         * TODO: Add comment
         */
        final public static int MAX_PRIORITY_LEVEL = 3;
        /*
        * This may be marked as true by the TransactionManagerWrapper.getTxWrapper( ) method or
        * this object's XAResource.rollback(javax.transaction.xa.Xid xid) method.
        */
        public TransactionWrapper(Transaction tx) {
            transaction = tx;
            registeredSynchronizations = new Vector();
            registeredSynchronizations.addElement(new Vector());
            registeredSynchronizations.addElement(new Vector());
            registeredSynchronizations.addElement(new Vector());

        }


        ///////////////////////////////////////////////
        ///           Transaction Methods           ///
        ///////////////////////////////////////////////

        public Transaction getTransaction() {

            return transaction;
        }  

        /**
         * TODO: Add comment
         * 
         * @param obj
         * @return 
         */
        public boolean equals(java.lang.Object obj) {

            try {

                return transaction.equals( ((TransactionWrapper)obj).getTransaction() );
            } catch ( java.lang.Throwable ex ) {
            }

            return false;
        }       

        public String toString(){
            return transaction.toString();
        } 

        /**
         * TODO: Add comment
         * 
         * @exception javax.transaction.SystemException
         * @exception javax.transaction.RollbackException
         * @exception javax.transaction.HeuristicRollbackException
         * @exception javax.transaction.HeuristicMixedException
         */
        public void commit() 
        throws javax.transaction.SystemException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {
            transaction.commit();
        }
        /**
         * TODO: Add comment
         * 
         * @param xaRes
         * @param flag
         * @return 
         * @exception javax.transaction.SystemException
         */
        public boolean delistResource(XAResource xaRes, int flag)throws javax.transaction.SystemException {
            return transaction.delistResource(xaRes,flag);
        }
        /**
         * TODO: Add comment
         * 
         * @param xaRes
         * @return 
         * @exception javax.transaction.SystemException
         * @exception javax.transaction.RollbackException
         */
        public boolean enlistResource(XAResource xaRes)throws javax.transaction.SystemException, javax.transaction.RollbackException {
            return transaction.enlistResource(xaRes);
        }
        /**
         * TODO: Add comment
         * 
         * @return 
         * @exception javax.transaction.SystemException
         */
        public int getStatus()throws javax.transaction.SystemException{
            return transaction.getStatus();
        }
        /*
        * Automatically add the Synchronization object to the lowest priority register.
        * Synchronization objects are executed in groups according to their priority 
        * and within each group according to the order they were registered.
        */
        public void registerSynchronization(Synchronization sync)
        throws javax.transaction.SystemException, javax.transaction.RollbackException{

            registerSynchronization(sync,MAX_PRIORITY_LEVEL);
        }

        /**
         * TODO: Add comment
         * 
         * @param sync
         * @param priority
         * @exception javax.transaction.SystemException
         * @exception javax.transaction.RollbackException
         */
        public void registerSynchronization(Synchronization sync, int priority)
        throws javax.transaction.SystemException, javax.transaction.RollbackException{
            if ( transaction.getStatus()== Status.STATUS_ACTIVE ) {
                if ( priority > MAX_PRIORITY_LEVEL )
                    priority = MAX_PRIORITY_LEVEL;
                else if ( priority < 1 )
                    priority = 1;
                ((Vector)registeredSynchronizations.elementAt(priority-1)).addElement(sync);
            } else if ( transaction.getStatus() == Status.STATUS_ROLLEDBACK || transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK || transaction.getStatus()== Status.STATUS_ROLLING_BACK )
                throw new javax.transaction.RollbackException();
            else {
                throw new java.lang.IllegalStateException(Thread.currentThread() + " The status of " + transaction + " is " + TransactionManagerWrapper.getStatus(transaction.getStatus()));

            }

        }
        /**
         * TODO: Add comment
         * 
         * @exception javax.transaction.SystemException
         */
        public void rollback()throws javax.transaction.SystemException{
            transaction.rollback();
        }
        /**
         * TODO: Add comment
         * 
         * @exception javax.transaction.SystemException
         */
        public void setRollbackOnly() throws javax.transaction.SystemException{
            transaction.setRollbackOnly();
        }
        ///////////////////////////////////////////////
        ///           Synchronization Methods       ///
        ///////////////////////////////////////////////

        public void beforeCompletion() {
            for ( int i = 0; i < registeredSynchronizations.size(); i++ ) {
                Vector synchronizations = (Vector)registeredSynchronizations.elementAt(i);
                java.util.Enumeration enum = synchronizations.elements();
                while ( enum.hasMoreElements() ) {
                    try {
                        Synchronization sync = (Synchronization)enum.nextElement();
                        sync.beforeCompletion();
                    } catch ( RuntimeException re ) {
                    }
                }
            }
        }
        /**
         * TODO: Add comment
         * 
         * @param status
         */
        public void afterCompletion(int status) {
            for ( int i = 0; i < registeredSynchronizations.size(); i++ ) {
                Vector synchronizations = (Vector)registeredSynchronizations.elementAt(i);
                java.util.Enumeration enum = synchronizations.elements();
                while ( enum.hasMoreElements() ) {
                    try {
                        Synchronization sync = (Synchronization)enum.nextElement();
                        sync.afterCompletion(status);
                    } catch ( RuntimeException re ) {
                        //TODO:2: Log the callback system exception
                    }
                }
                synchronizations.clear();
            }
            registeredSynchronizations.clear();
            wrapperMap.remove(transaction);
        }          

    }// End Innerclass: TransctionWrapper

    /**
     * Returns the readable name for the specified status.
     *
     * @param status The status
     * @return The status
     */
    private static String getStatus( int status )
    {
        StringBuffer buffer;

        buffer = new StringBuffer();
        switch ( status ) {
        case Status.STATUS_ACTIVE:
            buffer.append( "STATUS_ACTIVE: " );
            buffer.append( "A transaction is associated with the target object and it is in the active state." );
            break;
        case Status.STATUS_COMMITTED:
            buffer.append( "STATUS_COMMITTED: " );
            buffer.append( "A transaction is associated with the target object and it has been committed." );
            break;
        case Status.STATUS_COMMITTING:
            buffer.append( "STATUS_COMMITTING: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of committing." );
            break;
        case Status.STATUS_MARKED_ROLLBACK:
            buffer.append( "STATUS_MARKED_ROLLBACK: " );
            buffer.append( "A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation." );
            break;
        case Status.STATUS_NO_TRANSACTION:
            buffer.append( "STATUS_NO_TRANSACTION: " );
            buffer.append( "No transaction is currently associated with the target object." );
            break;
        case Status.STATUS_PREPARED:
            buffer.append( "STATUS_PREPARED: " );
            buffer.append( "A transaction is associated with the target object and it has been prepared, i.e." );
            break;
        case Status.STATUS_PREPARING:
            buffer.append( "STATUS_PREPARING: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of preparing." );
            break;           
        case Status.STATUS_ROLLEDBACK:
            buffer.append( "STATUS_ROLLEDBACK: " );
            buffer.append( "A transaction is associated with the target object and the outcome has been determined as rollback." );
            break;
        case Status.STATUS_ROLLING_BACK:
            buffer.append( "STATUS_ROLLING_BACK: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of rolling back." );
            break;
        default:
            buffer.append( "Unknown status " + status );
            break;
        }
        return buffer.toString();
    }
}
