package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;
import javax.transaction.Transaction;
import org.openejb.core.ThreadContext;

/**
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public interface TransactionContainer {

    public void discardInstance(EnterpriseBean instance, ThreadContext context);

}

