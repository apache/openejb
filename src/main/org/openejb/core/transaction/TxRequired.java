package org.openejb.core.transaction;

import java.rmi.RemoteException;
import javax.ejb.EnterpriseBean;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import org.apache.log4j.Category;
import org.openejb.ApplicationException;
import org.openejb.OpenEJB;
import org.openejb.SystemException;
import org.openejb.core.ThreadContext;

/**
 * 17.6.2.2 Required
 * 
 * The Container must invoke an enterprise Bean method whose transaction 
 * attribute is set to Required with a valid transaction context.
 * 
 * If a client invokes the enterprise Bean's method while the client is 
 * associated with a transaction context, the container invokes the enterprise
 * Bean's method in the client's transaction context.
 * 
 * If the client invokes the enterprise Bean's method while the client is not 
 * associated with a transaction context, the container automatically starts a 
 * new transaction before delegating a method call to the enterprise Bean 
 * business method. The Container automatically enlists all the resource 
 * managers accessed by the business method with the transaction. If the 
 * business method invokes other enterprise beans, the Container passes the 
 * transaction context with the invocation. The Container attempts to commit
 * the transaction when the business method has completed. The container 
 * performs the commit protocol before the method result is sent to the client.
 *
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TxRequired extends TransactionPolicy {
    
    public TxRequired(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxRequired(){
        policyType = Required;
    }
    
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            context.clientTx =  getTxMngr().getTransaction();

            if ( context.clientTx == null ) {
                beginTransaction();
            } 

            context.currentTx = getTxMngr().getTransaction();

        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        try {
            if ( context.clientTx != null ) return;

            if ( context.currentTx.getStatus() == Status.STATUS_ACTIVE ) {
                commitTransaction( context.currentTx );
            } else {
                rollbackTransaction( context.currentTx );
            }

        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    /**
     * <B>Container's action</B>
     * 
     * <P>
     * Re-throw AppException
     * </P>
     * 
     * <B>Client's view</B>
     * 
     * <P>
     * Client receives AppException.  Can attempt to continue computation in the
     * transaction, and eventually commit the transaction (the commit would fail
     * if the instance called setRollbackOnly()).
     * </P>
     */
    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        
        boolean runningInContainerTransaction = (!context.currentTx.equals( context.clientTx ));
        
        if (runningInContainerTransaction) {
            try{
                /*
                 * If the instance called setRollbackOnly(), then rollback the transaction, 
                 * and re-throw AppException.
                 * 
                 * Otherwise, attempt to commit the transaction, and then re-throw 
                 * AppException.
                 */
                if ( context.currentTx.getStatus() == Status.STATUS_ACTIVE ) {
                    commitTransaction( context.currentTx );
                } else {
                    rollbackTransaction( context.currentTx );
                }
            } catch (javax.transaction.SystemException e){
                // TODO:3: Localize the message; add to Messages.java
                logger.error("The transaction manager encountered an unexpected system error attempting to rollback or commit the transaction while handling an application exception: "+e.getMessage());
            } catch (org.openejb.SystemException e){
                // TODO:3: Localize the message; add to Messages.java
                logger.error("Unexpected error attempting to rollback or commit the transaction while handling an application exception: "+e.getRootCause().getClass().getName()+" "+e.getRootCause().getMessage());
            }
        }

        // Re-throw AppException
        throw new ApplicationException( appException );
    }
    
    /**
     * A system exception is any exception that is not an Application Exception.
     * <BR>
     * <B>Container's action</B>
     * 
     * <P>
     * <OL>
     * <LI>
     * Log the exception or error so that the System Administrator is alerted of
     * the problem.
     * </LI>
     * <LI>
     * Mark the transaction for rollback.
     * </LI>
     * <LI>
     * Discard instance.  The Container must not invoke any business methods or
     * container callbacks on the instance.
     * </LI>
     * <LI>
     * Throw javax.transaction.TransactionRolledbackException to remote client;
     * throw javax.ejb.TransactionRolledbackLocalException to local client.
     * </LI>
     * </OL>
     * 
     * </P>
     * 
     * <B>Client's view</B>
     * 
     * <P>
     * Receives javax.transaction.TransactionRolledbackException or
     * javax.ejb.TransactionRolledbackLocalException.
     * 
     * Continuing transaction is fruitless.
     * </P>
     */
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        
        boolean runningInContainerTransaction = (!context.currentTx.equals( context.clientTx ));

        if (runningInContainerTransaction) {
            /* [1] Log the system exception or error **********/
            logSystemException( sysException );

            /* [2] Rollback the container-started transaction */
            rollbackTransaction( context.currentTx );

            /* [3] Discard instance. **************************/
            discardBeanInstance( instance, context.callContext);

            /* [4] Throw RemoteException to client ************/
            throwExceptionToServer( sysException );
        
        } else {
            /* [1] Log the system exception or error **********/
            logSystemException( sysException );
            
            /* [2] Mark the transaction for rollback. *********/
            markTxRollbackOnly( context.clientTx );
            
            /* [3] Discard instance. **************************/
            discardBeanInstance( instance, context.callContext);
            
            /* [4] TransactionRolledbackException to client ***/
            throwTxExceptionToServer( sysException );
        }
    }
}
