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
 * 17.6.2.3 Supports
 * 
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to Supports as follows.
 * 
 * • If the client calls with a transaction context, the Container performs 
 *   the same steps as described in the Required case.
 * 
 * • If the client calls without a transaction context, the Container performs
 *   the same steps as described in the NotSupported case.
 * 
 * The Supports transaction attribute must be used with caution. This is 
 * because of the different transactional semantics provided by the two 
 * possible modes of execution. Only the enterprise beans that will
 * execute correctly in both modes should use the Supports transaction 
 * attribute.
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TxSupports extends TransactionPolicy {
    
    public TxSupports(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxSupports(){
        policyType = Supports;
    }
    
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            context.clientTx  = getTxMngr().getTransaction();
            context.currentTx = context.clientTx;
        
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

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
        
        boolean runningInClientTransaction = ( context.clientTx != null );

        if (runningInClientTransaction) {
            /* [1] Log the system exception or error *********/
            logSystemException( sysException );
            
            /* [2] Mark the transaction for rollback. ********/
            markTxRollbackOnly( context.clientTx );
            
            /* [3] Discard instance. *************************/
            discardBeanInstance(instance, context.callContext);
            
            /* [4] TransactionRolledbackException to client **/
            throwTxExceptionToServer( sysException );
        
        } else {
            /* [1] Log the system exception or error *********/
            logSystemException( sysException );

            /* [2] Discard instance. *************************/
            discardBeanInstance( instance, context.callContext);

            /* [3] Throw RemoteException to client ***********/
            throwExceptionToServer( sysException );
        }

    }

}

