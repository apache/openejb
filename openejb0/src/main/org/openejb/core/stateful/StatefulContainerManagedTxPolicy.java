package org.openejb.core.stateful;

import java.rmi.RemoteException;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionSynchronization;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import org.openejb.ApplicationException;
import org.openejb.OpenEJB;
import org.openejb.SystemException;
import org.openejb.InvalidateReferenceException;
import org.openejb.core.ThreadContext;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

/**
 * Wraps the TxPolicies for Stateful Session beans with container-managed
 * transaction demarkation that do not implement the SessionSynchronization 
 * interface.
 *
 * The following method TxPolicies are wrapped regardless:
 * 
 * TX_NEVER 
 * TX_NOT_SUPPORTED 
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class StatefulContainerManagedTxPolicy extends org.openejb.core.transaction.TransactionPolicy {
    
    protected TransactionPolicy policy;

    public StatefulContainerManagedTxPolicy(TransactionPolicy policy){
        this.policy     = policy;
        this.container  = policy.getContainer();
        this.policyType = policy.policyType;
        if(container instanceof org.openejb.Container &&
           ((org.openejb.Container)container).getContainerType()!=org.openejb.Container.STATEFUL) {
            throw new IllegalArgumentException();
        }
    }

    public String policyToString() {
        return policy.policyToString();
    }
    
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        policy.beforeInvoke( instance, context );
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        policy.afterInvoke( instance, context );
    }

    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        policy.handleApplicationException( appException, context );
    }
    
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {
            policy.handleSystemException( sysException, instance, context );
        } catch ( ApplicationException e ){
            throw new InvalidateReferenceException( e.getRootCause() );
        }
    }

}

