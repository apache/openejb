package org.openejb.core.stateful;

import java.rmi.RemoteException;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;
import javax.transaction.Transaction;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class StatefulBeanManagedTxPolicy extends TransactionPolicy {
    
    protected StatefulContainer statefulContainer;

    public StatefulBeanManagedTxPolicy(TransactionContainer container){
        this();
        if(container instanceof org.openejb.Container &&
           ((org.openejb.Container)container).getContainerType()!=org.openejb.Container.STATEFUL) {
            throw new IllegalArgumentException();
        }
        this.container = container;
        this.statefulContainer = (StatefulContainer)container;
        
    }

    public StatefulBeanManagedTxPolicy(){
        policyType = BeanManaged;
    }
    
    public String policyToString() {
        return "TX_BeanManaged: ";
    }
    /**
     * When a client invokes a business method via the enterprise bean's home or 
     * component interface, the Container suspends any transaction that may be 
     * associated with the client request. If there is a transaction associated 
     * with the instance (this would happen if the instance started the 
     * transaction in some previous business method), the Container associates the
     * method execution with this transaction.
     * 
     * The Container must make the javax.transaction.UserTransaction interface 
     * available to the enterprise bean's business method or onMessage method via 
     * the javax.ejb.EJBContext interface and under the environment entry 
     * java:comp/UserTransaction. 
     * 
     * When an instance uses the javax.transaction.UserTransaction interface to 
     * demarcate a transaction, the Container must enlist all the resource managers
     * used by the instance between the begin() and commit() or rollback() methods
     * with the transaction. 
     * 
     * When the instance attempts to commit the transaction, the Container is 
     * responsible for the global coordination of the transaction commit.
     * 
     * @param instance
     * @param context
     * @exception org.openejb.SystemException
     * @exception org.openejb.ApplicationException
     */
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        try {
        
            // if no transaction ---> suspend returns null
            context.clientTx = suspendTransaction();

            // Get any previously started transaction
            Object primaryKey = context.callContext.getPrimaryKey();
            Object possibleBeanTx = statefulContainer.getInstanceManager().getAncillaryState( primaryKey );
            if ( possibleBeanTx instanceof Transaction ) {
                context.currentTx =  (Transaction)possibleBeanTx;
                resumeTransaction( context.currentTx );
            }
        } catch ( org.openejb.OpenEJBException e ) {
            handleSystemException( e.getRootCause(), instance, context );
        }
    }

    /**
     * In the case of a stateful session bean, it is possible that the business 
     * method that started a transaction completes without committing or rolling 
     * back the transaction. In such a case, the Container must retain the 
     * association between the transaction and the instance across multiple client 
     * calls until the instance commits or rolls back the transaction. 
     * 
     * When the client invokes the next business method, the Container must invoke
     * the business method in this transaction context.
     * 
     * @param instance
     * @param context
     * @exception org.openejb.ApplicationException
     * @exception org.openejb.SystemException
     */
    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {
            // Get the instance's transaction
            context.currentTx = getTxMngr().getTransaction();

            /*
            // Remeber the instance's transaction if it is not committed or rolledback
            */
            if ( context.currentTx != null &&
                 context.currentTx.getStatus() != Status.STATUS_COMMITTED && 
                 context.currentTx.getStatus() != Status.STATUS_ROLLEDBACK ) {
                // There is a transaction in progress
                // if we have a valid transaction it must be suspended
                suspendTransaction();
            }
                            
            // Remeber the instance's transaction

            Object primaryKey = context.callContext.getPrimaryKey();
            statefulContainer.getInstanceManager().setAncillaryState( primaryKey, context.currentTx );

        } catch ( org.openejb.OpenEJBException e ) {
            handleSystemException( e.getRootCause(), instance, context );
        } catch ( javax.transaction.SystemException e ) {
            handleSystemException( e, instance, context );
        } catch ( Throwable e ){
            handleSystemException( e, instance, context );
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
        
        // Log the system exception or error
        logSystemException( sysException );
        
        // Mark for rollback the instance's transaction if it is not completed.
        if ( context.currentTx != null ) markTxRollbackOnly( context.currentTx );

        // Discard instance.  
        discardBeanInstance( instance, context.callContext);

        // Throw RemoteException to remote client; throw EJBException to local client.
        throwExceptionToServer( sysException );
        
    }

    protected void throwExceptionToServer(Throwable sysException) throws ApplicationException{
        // Throw RemoteException to remote client.
        RemoteException re = new RemoteException("The bean encountered a non-application exception.", sysException);
        
        throw new InvalidateReferenceException( re );

        // TODO:3: throw EJBException to local client.

    }

    protected void throwTxExceptionToServer( Throwable sysException ) throws ApplicationException{
        /* Throw javax.transaction.TransactionRolledbackException to remote client */
        // TODO:3: Internationalize the log message
        String message = "The transaction was rolled back because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : "+sysException.getMessage();
        javax.transaction.TransactionRolledbackException txException = new javax.transaction.TransactionRolledbackException(message);

        throw new InvalidateReferenceException( txException );

        // TODO:3: throw javax.ejb.TransactionRolledbackLocalException to local client.
    }
}

