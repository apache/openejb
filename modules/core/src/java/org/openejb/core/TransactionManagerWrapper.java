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

import java.util.Hashtable;
import java.util.Vector;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
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
    final private TransactionManager transactionManager;
    /**
     */
    final private Hashtable wrapperMap = new Hashtable();

    final static protected org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("Transaction");

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
        int status=transactionManager.getStatus();
        if(status== Status.STATUS_NO_TRANSACTION ||
           status== Status.STATUS_ROLLEDBACK ||
           status== Status.STATUS_COMMITTED ) {
        transactionManager.begin();
            createTxWrapper();
        }else {
            throw new javax.transaction.NotSupportedException("Can't start new transaction."+getStatus(status));
        }
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
    private Transaction getTxWrapper(Transaction tx)throws javax.transaction.SystemException{
        if ( tx == null ) {
          return null;
        }
        return (TransactionWrapper)wrapperMap.get(tx);
    }

    /**
     * to be called ONLY from beginTransaction, to register a synchronization
     * object while we can (e.g. before a rollback)
     */
    private void createTxWrapper() {
                try {
            Transaction tx = transactionManager.getTransaction();
            TransactionWrapper txW = new TransactionWrapper(tx);
                    tx.registerSynchronization(txW);
            wrapperMap.put(tx,txW);
        } catch ( Exception re ) {
            // this should never happen since we register right after
            // the transaction started.
            logger.info("", re);
        }
    }
    
    /**
     * Wraps the Transaction Manager's transaction implementation to intercept calls
     * to the Transaction object, most notably to registerSynchronization
     */
    private class TransactionWrapper 
    implements Transaction, javax.transaction.Synchronization {

        /**
         * The Transaction Manager's transaction instance.
         */
        private final Transaction transaction;
        /**
         * TODO: Add comment
         */
        private final Vector registeredSynchronizations;
        /**
         * TODO: Add comment
         */
        final public static int MAX_PRIORITY_LEVEL = 3;

        private TransactionWrapper(Transaction tx) {
            transaction = tx;
            registeredSynchronizations = new Vector();
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
            if(obj != null && obj instanceof TransactionWrapper) {
                return transaction.equals( ((TransactionWrapper)obj).getTransaction() );
            }

            return false;
        }       
        // equals and hashCode always have to be implemented together!
        public int hashCode() {
            return transaction.hashCode();
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
         * This method allows for notification when the transaction ends.
         * As opposed to the documented behavior of the same method on the
         * Transaction interface the registration will succeed even after
         * the transaction has been marked for rollback. Multiple registrations
         * for the same object will be ignored.
         * @param sync
         * @param priority
         * @exception javax.transaction.SystemException
         * @exception javax.transaction.RollbackException
         */
        private void registerSynchronization(Synchronization sync, int priority) {
            if (!registeredSynchronizations.contains(sync)) {
                registeredSynchronizations.addElement(sync);
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
            int count = registeredSynchronizations.size();
            for ( int i=0; i<count; ++i ) {
                try {
                    Synchronization sync = (Synchronization)registeredSynchronizations.elementAt(i);
                        sync.beforeCompletion();
                    } catch ( RuntimeException re ) {
                    logger.error("", re);
                }
            }
        }
        /**
         * TODO: Add comment
         * 
         * @param status
         */
        public void afterCompletion(int status) {
            int count = registeredSynchronizations.size();
            for ( int i=0; i<count; ++i ) {
                try {
                    Synchronization sync = (Synchronization)registeredSynchronizations.elementAt(i);
                        sync.afterCompletion(status);
                    } catch ( RuntimeException re ) {
                    logger.error("", re);
                }
            }
            wrapperMap.remove(transaction);
        }          

    }// End Innerclass: TransctionWrapper

    /**
     * Returns the readable name for the specified status.
     *
     * @param status The status
     * @return The status
     */
    public static String getStatus( int status )
    {
        StringBuffer buffer;

        buffer = new StringBuffer(100);
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
