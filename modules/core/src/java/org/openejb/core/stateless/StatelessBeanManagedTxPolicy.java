package org.openejb.core.stateless;

import java.rmi.RemoteException;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;

import org.openejb.ApplicationException;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

/**
 * Transaction policy for Stateless Session beans with 
 * bean-managed transaction demarcation.
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class StatelessBeanManagedTxPolicy extends TransactionPolicy {
    
    public StatelessBeanManagedTxPolicy(TransactionContainer container){
        this();
        if(container instanceof org.openejb.Container &&
           ((org.openejb.Container)container).getContainerType()!=org.openejb.Container.STATELESS) {
            throw new IllegalArgumentException();
        }
           
        this.container = container;
    }

    public StatelessBeanManagedTxPolicy(){
        policyType = BeanManaged;
    }
    
    public String policyToString() {
        return "TX_BeanManaged: ";
    }
    /**
     * When a client invokes a business method via the enterprise bean’s home
     * or component interface, the Container suspends any transaction that may be 
     * associated with the client request.
     * 
     * @param instance
     * @param context
     * @exception org.openejb.SystemException
     * @exception org.openejb.ApplicationException
     */
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
            // if no transaction ---> suspend returns null
        context.clientTx = suspendTransaction();
    }

    /**
     * If a stateless session bean instance starts a transaction in a business 
     * method, it must commit the transaction before the business method returns.
     * 
     * The Container must detect the case in which a transaction was started, but
     * not completed, in the business method, and handle it as follows:
     * 
     * • Log this as an application error to alert the system administrator.
     * • Roll back the started transaction.
     * • Discard the instance of the session bean.
     * • Throw the java.rmi.RemoteException to the client if the client is a 
     *   remote client, or throw the javax.ejb.EJBException if the client is a 
     *   local client.
     * 
     * @param instance
     * @param context
     * @exception org.openejb.ApplicationException
     * @exception org.openejb.SystemException
     */
    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {
            /*
            * The Container must detect the case in which a transaction was started, but
            * not completed, in the business method, and handle it as follows:
            */
            context.currentTx = getTxMngr().getTransaction();

            if (context.currentTx == null) return;
            
            if (context.currentTx.getStatus() != Status.STATUS_ROLLEDBACK && context.currentTx.getStatus() != Status.STATUS_COMMITTED ) {
                String message = "The stateless session bean started a transaction but did not complete it.";

                /* [1] Log this as an application error ********/
                logger.error( message );
                
                /* [2] Roll back the started transaction *******/
                try {
                    rollbackTransaction( context.currentTx );
                } catch (Throwable t){
                    // This exception was logged before leaving the method
                }
                
                /* [3] Throw the RemoteException to the client */
                throwAppExceptionToServer( new RemoteException( message ));
            }

        } catch (javax.transaction.SystemException e){
            throw new org.openejb.SystemException( e );
        } finally {
            resumeTransaction( context.clientTx );
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
     * Client receives AppException.
     * </P>
     */
    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        //re-throw AppException
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
     * Mark for rollback a transaction that has been started, but not yet 
     * completed, by the instance.
     * </LI>
     * <LI>
     * Discard instance.  The Container must not invoke any business methods or
     * container callbacks on the instance.
     * </LI>
     * <LI>
     * Throw RemoteException to remote client; throw EJBException to local client.
     * </LI>
     * </OL>
     * </P>
     * 
     * <B>Client's view</B>
     * 
     * <P>
     * Receives RemoteException or EJBException.
     * </P>
     */
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {
            context.currentTx = getTxMngr().getTransaction();
        } catch (javax.transaction.SystemException e ){
            context.currentTx = null;
        }

        // Log the system exception or error
        logSystemException( sysException );
        
        // Mark for rollback the instance's transaction if it is not completed.
        if ( context.currentTx != null ) markTxRollbackOnly( context.currentTx );

        // Discard instance.  
        discardBeanInstance( instance, context.callContext);

        // Throw RemoteException to remote client; throw EJBException to local client.
        throwExceptionToServer( sysException );
        
    }

}

