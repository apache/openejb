/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;

import org.openejb.ApplicationException;

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
    
    public String policyToString() {
        return "TX_Required: ";
    }
    
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            context.clientTx =  getTxMngr().getTransaction();

            if ( context.clientTx == null ) {
                beginTransaction();
            } 

            context.currentTx = getTxMngr().getTransaction();

        } catch ( javax.transaction.SystemException se ) {
            logger.error("Exception during getTransaction()", se);
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        try {
            if ( context.clientTx != null ) return;
            // we created a new transaction in beforeInvoke, which must be ended.
            if ( context.currentTx.getStatus() == Status.STATUS_ACTIVE ) {
                commitTransaction( context.currentTx );
            } else {
                rollbackTransaction( context.currentTx );
            }

        } catch ( javax.transaction.SystemException se ) {
            logger.error("Exception during getTransaction()", se);
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
        
            /* [1] Log the system exception or error **********/
            logSystemException( sysException );

        boolean runningInContainerTransaction = (!context.currentTx.equals( context.clientTx ));
        if (runningInContainerTransaction) {
            /* [2] Mark the transaction for rollback. afterInvoke() will roll it back */
            markTxRollbackOnly( context.currentTx );

            /* [3] Discard instance. **************************/
            discardBeanInstance( instance, context.callContext);

            /* [4] Throw RemoteException to client ************/
            throwExceptionToServer( sysException );
        } else {
            /* [2] Mark the transaction for rollback. *********/
            markTxRollbackOnly( context.clientTx );
            
            /* [3] Discard instance. **************************/
            discardBeanInstance( instance, context.callContext);
            
            /* [4] Throw TransactionRolledbackException to client ************/
            throwTxExceptionToServer( sysException );
        }
    }
}
