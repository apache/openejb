package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;
import javax.transaction.Transaction;
import org.openejb.core.ThreadContext;

/**
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TransactionContext {

    public Transaction clientTx;
    public Transaction currentTx;
    public ThreadContext callContext;

    public TransactionContext(){
    }

    public TransactionContext(ThreadContext callContext){
        this.callContext = callContext;
    }
}

