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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core.transaction;

import java.rmi.RemoteException;

import javax.ejb.EnterpriseBean;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.SystemException;
import org.openejb.core.ThreadContext;
import org.openejb.util.Logger;

/**
 * Use container callbacks so containers can implement any special behavior
 * 
 * Callbacks are similar to sessionsynchronization with the addition of
 * discardInstace
 * createSystemException
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public abstract class TransactionPolicy {
    
    public static final int Mandatory    = 0;
    public static final int Never 	 = 1;
    public static final int NotSupported = 2;
    public static final int Required     = 3;
    public static final int RequiresNew  = 4;
    public static final int Supports     = 5;
    public static final int BeanManaged  = 6;
    
    public int policyType;
    private TransactionManager manager;
    protected TransactionContainer container;

    protected final static Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.util.resources" );
    protected final static Logger txLogger = Logger.getInstance( "Transaction", "org.openejb.util.resources" );

    protected TransactionManager getTxMngr( ) {
        if(manager==null) {
            manager = OpenEJB.getTransactionManager();
        }
        return manager;
    }
    
    public TransactionContainer getContainer(){
        return container;
    }
    
    public String policyToString() {
        return "Internal Error: no such policy";
    }
    
    public abstract void handleApplicationException( Throwable appException, TransactionContext context ) throws org.openejb.ApplicationException;
    public abstract void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context ) throws org.openejb.ApplicationException, org.openejb.SystemException;
    public abstract void beforeInvoke( EnterpriseBean bean, TransactionContext context ) throws org.openejb.SystemException, org.openejb.ApplicationException;
    public abstract void afterInvoke(EnterpriseBean bean, TransactionContext context ) throws org.openejb.ApplicationException, org.openejb.SystemException;

    protected void markTxRollbackOnly( Transaction tx ) throws SystemException{
        try {
            if ( tx != null ) {
                tx.setRollbackOnly();
                if(txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString()+"setRollbackOnly() on transaction "+tx);
                }
            }
        } catch ( javax.transaction.SystemException se ) {
            logger.error("Exception during setRollbackOnly()", se);
            throw new org.openejb.SystemException(se);
        }
    }

    protected Transaction suspendTransaction() throws SystemException{
        try {
            Transaction tx = getTxMngr( ).suspend();
            if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"Suspended transaction "+tx);
            }
            return tx;
        } catch ( javax.transaction.SystemException se ) {
            logger.error("Exception during suspend()", se);
            throw new org.openejb.SystemException(se);
        }
    }
    
    protected void resumeTransaction(Transaction tx) throws SystemException{
        try {
            if ( tx == null) {
                if(txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString()+"No transaction to resume");
                }
            } else {
                if(txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString()+"Resuming transaction "+tx);
                }
                getTxMngr( ).resume(tx);
            }
        }catch(javax.transaction.InvalidTransactionException ite){
            // TODO:3: Localize the message; add to Messages.java
            txLogger.error("Could not resume the client's transaction, the transaction is no longer valid: "+ite.getMessage());
            throw new org.openejb.SystemException(ite);
        }catch(IllegalStateException e){
            // TODO:3: Localize the message; add to Messages.java
            txLogger.error("Could not resume the client's transaction: "+e.getMessage());
            throw new org.openejb.SystemException(e);
        }catch(javax.transaction.SystemException e){
            // TODO:3: Localize the message; add to Messages.java
            txLogger.error("Could not resume the client's transaction: The transaction reported a system exception: "+e.getMessage());
            throw new org.openejb.SystemException(e);
        }
    }
    
    protected void commitTransaction( Transaction tx ) throws SystemException{
        try {
            if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"Committing transaction "+tx);
            }
            if(tx.equals(getTxMngr().getTransaction())) {
                // this solves the problem that rolling back the transaction does not
                // remove the association between the transaction and the current thread
                // e.g. a subsequent resume() will fail
                getTxMngr().commit();
            } else {
                tx.commit();
            }
        } catch ( javax.transaction.RollbackException e ) {
            // TODO:3: Localize the message; add to Messages.java
            txLogger.info("The transaction has been rolled back rather than commited: "+e.getMessage());
        
        } catch ( javax.transaction.HeuristicMixedException e ) {
            // TODO:3: Localize the message; add to Messages.java
            txLogger.info("A heuristic decision was made, some relevant updates have been committed while others have been rolled back: "+e.getMessage());
        
        } catch ( javax.transaction.HeuristicRollbackException e ) {
            // TODO:3: Localize the message; add to Messages.java
            txLogger.info("A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back: "+e.getMessage());

        } catch (SecurityException e){
            // TODO:3: Localize the message; add to Messages.java
            txLogger.error("The current thread is not allowed to commit the transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );
        
        } catch (IllegalStateException e){
            // TODO:3: Localize the message; add to Messages.java
            txLogger.error("The current thread is not associated with a transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );

        } catch (javax.transaction.SystemException e){
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to commit the transaction: "+e.getMessage());
            // TODO:3: Localize the message; add to Messages.java
            throw new org.openejb.SystemException( e );
        }
    }
    
    protected void rollbackTransaction( Transaction tx ) throws SystemException{
        try {
            if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"Rolling back transaction "+tx);
            }
            if(tx.equals(getTxMngr().getTransaction())) {
                // this solves the problem that rolling back the transaction does not
                // remove the association between the transaction and the current thread
                // e.g. a subsequent resume() will fail
                getTxMngr().rollback();
            } else {
            tx.rollback();
            }
        } catch (IllegalStateException e){
            // TODO:3: Localize the message; add to Messages.java
            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );

        } catch (javax.transaction.SystemException e){
            // TODO:3: Localize the message; add to Messages.java
            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );
        }
    }
    
    protected void throwAppExceptionToServer( Throwable appException ) throws ApplicationException{
        throw new ApplicationException( appException );
    }

    protected void throwTxExceptionToServer( Throwable sysException ) throws ApplicationException{
        /* Throw javax.transaction.TransactionRolledbackException to remote client */
        // TODO:3: Localize the message; add to Messages.java
        String message = "The transaction was rolled back because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : "+sysException.getMessage();
        javax.transaction.TransactionRolledbackException txException = new javax.transaction.TransactionRolledbackException(message);

        // See section 2.1.3.7 of the OpenEJB Specification
        throw new InvalidateReferenceException( txException );

        // TODO:3: throw javax.ejb.TransactionRolledbackLocalException to local client.
    }

    /**
     * Throw RemoteException to remote client; throw EJBException to local client.
     * 
     * @param sysException
     * @exception ApplicationException
     */
    protected void throwExceptionToServer( Throwable sysException ) throws ApplicationException{
        // Throw RemoteException to remote client.
        // TODO:3: Localize the message; add to Messages.java
        RemoteException re = new RemoteException("The bean encountered a non-application exception.", sysException);
        
        // See section 2.1.3.7 of the OpenEJB Specification
        throw new InvalidateReferenceException( re );

        // TODO:3: throw EJBException to local client.

    }
    
    protected void logSystemException(Throwable sysException){
        // TODO:2: Put information about the instance and deployment in the log message.
        // TODO:3: Localize the message; add to Messages.java
        logger.error( "The bean instances business method encountered a system exception:"+sysException.getMessage(), sysException);
    }
    
    protected void discardBeanInstance(EnterpriseBean instance, ThreadContext callContext){
        container.discardInstance( instance, callContext );
    }
    
    protected void beginTransaction() throws javax.transaction.SystemException{
        try {
            getTxMngr( ).begin();
            if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"Started transaction "+getTxMngr( ).getTransaction());
            }
        } catch ( javax.transaction.NotSupportedException nse ) {
            logger.error("", nse);
        }
    }


    /**
     * <B>18.3.3 Exceptions from container-invoked callbacks</B>
     * 
     * <P>
     * Handles the exceptions thrown from the from the following container-invoked
     * callback methods of the enterprise bean.
     * 
     * EntityBean:
     * • ejbActivate()
     * • ejbLoad()
     * • ejbPassivate()
     * • ejbStore()
     * • setEntityContext(EntityContext)
     * • unsetEntityContext()
     * 
     * SessionBean:
     * • ejbActivate()
     * • ejbPassivate()
     * • setSessionContext(SessionContext)
     * 
     * MessageDrivenBean:
     * • setMessageDrivenContext(MessageDrivenContext)
     * 
     * SessionSynchronization interface:
     * • afterBegin()
     * • beforeCompletion()
     * • and afterCompletion(boolean)
     * </P>
     * <P>
     * The Container must handle all exceptions or errors from these methods 
     * as follows:
     * 
     * • Log the exception or error to bring the problem to the attention of the
     *   System Administrator.
     * 
     * • If the instance is in a transaction, mark the transaction for rollback.
     * 
     * • Discard the instance. The Container must not invoke any business methods
     *   or container callbacks on the instance.
     * 
     * • If the exception or error happened during the processing of a client 
     *   invoked method, throw the java.rmi.RemoteException to the client if the 
     *   client is a remote client or throw the javax.ejb.EJBException to the 
     *   client if the client is a local client. 
     *   
     *   If the instance executed in the client's transaction, the Container 
     *   should throw the javax.transaction.TransactionRolledbackException to a 
     *   remote client or the javax.ejb.TransactionRolledbackLocalException to a 
     *   local client, because it provides more information to the client. 
     *   (The client knows that it is fruitless to continue the transaction.)
     */
    protected void handleCallbackException(){
    }
}


