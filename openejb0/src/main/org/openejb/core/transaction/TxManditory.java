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


/**
 * 17.6.2.5 Mandatory
 * 
 * The Container must invoke an enterprise Bean method whose transaction 
 * attribute is set to Mandatory in a client's transaction context. The client
 * is required to call with a transaction context.
 * 
 * • If the client calls with a transaction context, the container invokes the 
 *   enterprise Bean's method in the client's transaction context.
 * 
 * • If the client calls without a transaction context, the Container throws 
 *   the javax.transaction.TransactionRequiredException exception if the 
 *   client is a remote client, or the 
 *   javax.ejb.TransactionRequiredLocalException if the client is a local 
 *   client.
 *
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TxManditory extends TransactionPolicy {
    
    public TxManditory(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxManditory(){
        policyType = Manditory;
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            context.clientTx = getTxMngr().getTransaction();
        
            if ( context.clientTx == null ){
                // TODO:3: Add a message to the TransactionRequiredException thrown.
                throw new ApplicationException(new javax.transaction.TransactionRequiredException());
            }
                    
            context.currentTx = context.clientTx;

        } catch ( javax.transaction.SystemException se ) {
            //TODO:0: Log the transaction system exception
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        // Nothing to do
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
        // [1] Re-throw AppException
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
        
        /* [1] Log the system exception or error *********/
        logSystemException( sysException );
        
        /* [2] Mark the transaction for rollback. ********/
        markTxRollbackOnly( context.currentTx );
        
        /* [3] Discard instance. *************************/
        discardBeanInstance( instance, context.callContext );
        
        /* [4] TransactionRolledbackException to client **/
        throwTxExceptionToServer( sysException );
    }


}

