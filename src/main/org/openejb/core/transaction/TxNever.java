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
 * 17.6.2.6 Never
 * 
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to Never without a transaction context defined by the EJB spec. 
 * 
 * The client is required to call without a transaction context.
 * 
 * • If the client calls with a transaction context, the Container throws the 
 *   java.rmi.RemoteException exception if the client is a remote client, or 
 *   the javax.ejb.EJBException if the client is a local client.
 * • If the client calls without a transaction context, the Container performs
 *   the same steps as described in the NotSupported case.
 *
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TxNever extends TransactionPolicy {
    
    public TxNever(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxNever(){
        policyType = Never;
    }
    
    public String policyToString() {
        return "TX_Never: ";
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            if ( getTxMngr().getTransaction() != null ){
                // TODO:3: Localize the message; add to Messages.java
                throw new ApplicationException(new java.rmi.RemoteException("Transactions not supported"));
            }
        
        } catch ( javax.transaction.SystemException se ) {
            logger.error("Exception during getTransaction()", se);
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
     * Receives AppException.
     * 
     * If the client executes in a transaction, the client's transaction is not 
     * marked for rollback, and client can continue its work.
     * </P>
     */
    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        // Re-throw AppException
        throw new ApplicationException( appException );
    }
    
    /**
     * A system exception is any exception that is not an Application Exception.
     * 
     * <B>Container's action</B>
     * 
     * <P>
     * <OL>
     * <LI>
     * Log the exception or error so that the System Administrator is alerted of
     * the problem.
     * </LI>
     * <LI>
     * Discard instance.  The Container must not invoke any business methods or
     * container callbacks on the instance.
     * </LI>
     * <LI>
     * Throw RemoteException to remote client;
     * throw EJBException to local client.
     * </LI>
     * </OL>
     * 
     * </P>
     * 
     * <B>Client's view</B>
     * 
     * <P>
     * Receives RemoteException or EJBException.
     * 
     * If the client executes in a transaction, the client's transaction may or
     * may not be marked for rollback.
     * </P>
     */
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        /* [1] Log the system exception or error *********/
        logSystemException( sysException );

        /* [2] Discard instance. *************************/
        discardBeanInstance( instance, context.callContext);

        /* [3] Throw RemoteException to client ***********/
        throwExceptionToServer( sysException );
    }

}

