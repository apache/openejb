package org.openejb.core.stateful;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionSynchronization;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openejb.ApplicationException;
import org.openejb.OpenEJB;
import org.openejb.SystemException;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.util.Logger;

/**
 * This class manages all the SessionSynchronization instances for a given 
 * transaction.
 * 
 * The SessionSynchronizationCoordinator registers with the transaction and 
 * receives the beforeCompletion and afterCompletion events when the transaction
 * completes.
 * 
 * When this object receives the beforeCompletion and afterCompletion events,
 * it invokes the appropriate container-callback on the SessionSynchronization
 * objects registered with this coordinator.
 *
 * @author <a href="mailto=David.Blevins@visi.com">David Blevins</a>
 * @author <a href="mailto=Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @version $Revision$ $Date$
 */
public class SessionSynchronizationCoordinator implements javax.transaction.Synchronization{
    

    private static java.util.HashMap coordinators = new java.util.HashMap();
    public static Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.util.resources" );

    /**
     * The actual instances are not stored in this hash as we cannot forsee how 
     * long the transaction will take.  It is very possible that the stateful 
     * session instance is passivated and activated several times.  Instead, we
     * hold the primary key of the stateful session instance and ask the 
     * StatefulInstanceManager for the instance when the time is right.
     * key: primaryKey value: ThreadContext
     */
    private java.util.HashMap sessionSynchronizations = new java.util.HashMap();
    

    public static void registerSessionSynchronization(SessionSynchronization session, TransactionContext context) throws javax.transaction.SystemException, javax.transaction.RollbackException{
        SessionSynchronizationCoordinator coordinator = null;
        
        coordinator = (SessionSynchronizationCoordinator)coordinators.get( context.currentTx );

        if (coordinator == null) {
            coordinator = new SessionSynchronizationCoordinator();
            try{
                context.currentTx.registerSynchronization( coordinator);
            }catch(Exception e) {
                // this should never fail because of the bahavior defined in TransactionManagerWrapper
                logger.error("", e);
                return;
            }
            coordinators.put( context.currentTx, coordinator );
        }

        coordinator._registerSessionSynchronization( session, context.callContext );
    }
    
    private void _registerSessionSynchronization(SessionSynchronization session, ThreadContext callContext){
        boolean registered = sessionSynchronizations.containsKey( callContext.getPrimaryKey() );

        if ( registered ) return;

        try{
            callContext = (ThreadContext)callContext.clone();
        }catch(Exception e) {}
        sessionSynchronizations.put(callContext.getPrimaryKey(), callContext);

        byte currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operations.OP_AFTER_BEGIN);        
        try{
            
            session.afterBegin();
        
        } catch (Exception e){
            String message = "An unexpected system exception occured while invoking the afterBegin method on the SessionSynchronization object: "+e.getClass().getName()+" "+e.getMessage();
            logger.error(message, e);
            throw new RuntimeException( message );

        } finally {
           callContext.setCurrentOperation(currentOperation);
        }
    }

    /**
    * This method is called by the transaction 
    * manager prior to the start of the transaction completion process.
    */
    public void beforeCompletion() {
        
        ThreadContext originalContext = ThreadContext.getThreadContext();
        
        Object[] contexts = sessionSynchronizations.values().toArray();

        for(int i = 0; i < contexts.length; i++){

            // Have to reassocaite the original thread context with the thread before invoking
            ThreadContext callContext  = (ThreadContext)contexts[i];
            
            ThreadContext.setThreadContext(callContext);
            StatefulInstanceManager instanceManager = null;
            
            try{
                StatefulContainer container = (StatefulContainer)callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so 
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operations.OP_BEFORE_COMPLETION);
                
                SessionSynchronization bean = (SessionSynchronization)instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);
                bean.beforeCompletion();
                instanceManager.poolInstance(callContext.getPrimaryKey(), (EnterpriseBean)bean);
            }catch(org.openejb.InvalidateReferenceException inv) {
                // the bean doesn't exist anymore, e.g. a system exception occured
                // and the bean hass been discarded. No container callbacks
                // are allowed at this point
                // therefore we simply do nothing() here....
            }catch(Exception e){
                // This exception should go back to the TransactionManager where
                // it will be rethrown by the TransactionManager as the appropriate
                // exception type ( TransactionRolledBackException or whatever) to the
                // bean or client that requested the transaction to be commited or rolledback.
                // The TransactionManager will not understand the OpenEJBException, the 
                // RemoteException is understandable.

                // EJB 2.0: 18.3.3 Exceptions from container-invoked callbacks
                String message = "An unexpected system exception occured while invoking the beforeCompletion method on the SessionSynchronization object: "+e.getClass().getName()+" "+e.getMessage();
                
                /* [1] Log the exception or error */
                logger.error( message, e);
                
                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                Transaction tx = null;
                try{
                    tx = getTxMngr().getTransaction();
                } catch (Throwable t){
                    logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the beforeCompletion method of bean "+callContext.getPrimaryKey());
                }
                try{
                    markTxRollbackOnly( tx );
                } catch (Throwable t){
                    logger.error("Could not mark the current transaction for rollback while handling a callback exception from the beforeCompletion method of bean "+callContext.getPrimaryKey());
                }

                /* [3] Discard the instance */
                discardInstance( instanceManager, callContext );
            
                /* [4] throw the java.rmi.RemoteException to the client */
                throw new RuntimeException( message );
            } finally {
                ThreadContext.setThreadContext( originalContext );
            }
        }
    }
    
    /**
     * This method is called by the transaction 
     * manager after the transaction is committed or rolled back. 
     *
     * @param status The status of the transaction completion.
     */
    public void afterCompletion(int status) {
        
        ThreadContext originalContext = ThreadContext.getThreadContext();
        
        Object[] contexts = sessionSynchronizations.values().toArray();

        try{
            Transaction tx = getTxMngr().getTransaction();
            coordinators.remove( tx );
        }catch(Exception e) {
            logger.error("", e);
        }
        for(int i = 0; i < contexts.length; i++){

            // Have to reassocaite the original thread context with the thread before invoking
            ThreadContext callContext  = (ThreadContext)contexts[i];
            
            ThreadContext.setThreadContext(callContext);
            StatefulInstanceManager instanceManager = null;
            
            try{
                StatefulContainer container = (StatefulContainer)callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so 
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operations.OP_AFTER_COMPLETION);
                
                SessionSynchronization bean = (SessionSynchronization)instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);
                
                bean.afterCompletion( status == Status.STATUS_COMMITTED );
                instanceManager.poolInstance(callContext.getPrimaryKey(), (EnterpriseBean)bean);
            }catch(org.openejb.InvalidateReferenceException inv) {
                // the bean doesn't exist anymore, e.g. a system exception occured
                // and the bean has been discarded. No container callbacks
                // are allowed at this point
                // therefore we simply do nothing() here....
            }catch(Exception e){
                // EJB 2.0: 18.3.3 Exceptions from container-invoked callbacks
                String message = "An unexpected system exception occured while invoking the afterCompletion method on the SessionSynchronization object: "+e.getClass().getName()+" "+e.getMessage();
                
                /* [1] Log the exception or error */
                logger.error( message, e);
                
                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                Transaction tx = null;
                try{
                    tx = getTxMngr().getTransaction();
                } catch (Throwable t){
                    logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the afterCompletion method of bean "+callContext.getPrimaryKey());
                }
                try{
                    markTxRollbackOnly( tx );
                } catch (Throwable t){
                    logger.error("Could not mark the current transaction for rollback while handling a callback exception from the afterCompletion method of bean "+callContext.getPrimaryKey());
                }

                /* [3] Discard the instance */
                discardInstance( instanceManager, callContext );
            
                /* [4] throw the java.rmi.RemoteException to the client */
                // this will be caught in TransactionManagerWrapper
                throw new RuntimeException( message );
            } finally {
                ThreadContext.setThreadContext( originalContext );
            }
        }
    }

    protected void discardInstance(StatefulInstanceManager instanceManager, ThreadContext callContext){
        try{
            instanceManager.freeInstance( callContext.getPrimaryKey() );
        }catch(org.openejb.OpenEJBException oee){
            // stateful session interface doesn't throw an exception
        }
    }
    
    protected void markTxRollbackOnly( Transaction tx ) throws SystemException{
        try {
            if ( tx != null ) tx.setRollbackOnly();
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    protected TransactionManager getTxMngr( ) {
        return OpenEJB.getTransactionManager();
    }
    
    /**
     * Throw RemoteException to remote client; throw EJBException to local client.
     * 
     * @param sysException
     * @exception ApplicationException
     */
    protected void throwExceptionToServer( Throwable sysException ) throws ApplicationException{
        // Throw RemoteException to remote client.
        
        // See section 2.1.3.7 of the OpenEJB Specification
        throw new ApplicationException( sysException );

        // TODO:3: throw EJBException to local client.

    }
}
