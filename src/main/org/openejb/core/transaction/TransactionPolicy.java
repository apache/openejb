package org.openejb.core.transaction;

import java.rmi.RemoteException;
import javax.ejb.EnterpriseBean;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import org.openejb.ApplicationException;
import org.openejb.OpenEJB;
import org.openejb.SystemException;
import org.openejb.core.ThreadContext;
import org.openejb.util.Logger;
//import org.openejb.core.Container;
import org.openejb.Container;

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
    
    public static final int Manditory    = 0;
    public static final int Never 	 = 1;
    public static final int NotSupported = 2;
    public static final int Required     = 3;
    public static final int RequiresNew  = 4;
    public static final int Supports     = 5;
    public static final int BeanManaged  = 6;
    
    public int policyType;
    
    protected TransactionContainer container;

    protected TransactionManager getTxMngr( ) {
        return OpenEJB.getTransactionManager();
    }
    
    public TransactionContainer getContainer(){
        return container;
    }
    
    public Logger logger = Logger.getInstance("OpenEJB");

    public abstract void handleApplicationException( Throwable appException, TransactionContext context ) throws org.openejb.ApplicationException;
    public abstract void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context ) throws org.openejb.ApplicationException, org.openejb.SystemException;
    public abstract void beforeInvoke( EnterpriseBean bean, TransactionContext context ) throws org.openejb.SystemException, org.openejb.ApplicationException;
    public abstract void afterInvoke(EnterpriseBean bean, TransactionContext context ) throws org.openejb.ApplicationException, org.openejb.SystemException;

    protected void markTxRollbackOnly( Transaction tx ) throws SystemException{
        try {
        if ( tx != null ) tx.setRollbackOnly();
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }
    
    protected void commitTransaction( Transaction tx ) throws SystemException{
        try {
            tx.commit();
        } catch ( javax.transaction.RollbackException e ) {
            // TODO:3: Localize the message; add to Messages.java
            logger.warning("TX_WARNING: The transaction has been rolled back rather than commited: "+e.getMessage());
        
        } catch ( javax.transaction.HeuristicMixedException e ) {
            // TODO:3: Localize the message; add to Messages.java
            logger.info("TX_INFO: A heuristic decision was made, some relevant updates have been committed while others have been rolled back: "+e.getMessage());
        
        } catch ( javax.transaction.HeuristicRollbackException e ) {
            // TODO:3: Localize the message; add to Messages.java
            logger.info("TX_INFO: A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back: "+e.getMessage());

        } catch (SecurityException e){
            // TODO:3: Localize the message; add to Messages.java
            logger.error("TX_ERROR: The current thread is not allowed to commit the transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );
        
        } catch (IllegalStateException e){
            // TODO:3: Localize the message; add to Messages.java
            logger.error("TX_ERROR: The current thread is not associated with a transaction: "+e.getMessage());
            throw new org.openejb.SystemException( e );

        } catch (javax.transaction.SystemException e){
            logger.error("TX_ERROR: The Transaction Manager has encountered an unexpected error condition while attempting to commit the transaction: "+e.getMessage());
            // TODO:3: Localize the message; add to Messages.java
            throw new org.openejb.SystemException( e );
        }
    }
    
    protected void rollbackTransaction( Transaction tx ) throws SystemException{
        try {
            tx.rollback();
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
        throw new ApplicationException( txException );

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
        throw new ApplicationException( re );

        // TODO:3: throw EJBException to local client.

    }
    
    protected void logSystemException(Throwable sysException){
        // TODO:2: Put information about the instance and deployment in the log message.
        // TODO:3: Localize the message; add to Messages.java
        logger.error( "The bean instances business method encountered a non-application excption:"+sysException.getMessage(), sysException);
    }
    
    protected void discardBeanInstance(EnterpriseBean instance, ThreadContext callContext){
        container.discardInstance( instance, callContext );
    }
    
    protected void beginTransaction() throws javax.transaction.SystemException{
        try {
            getTxMngr( ).begin();
        } catch ( javax.transaction.NotSupportedException nse ) {
            /* 
            This exception will never happen. Only thrown if a nested 
            exception is attempted and is not supported by the Tx manager. 
            Since we have already determined that the current tranaction is 
            not active, an attempt to create a nested exception is not possible.
            */
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


