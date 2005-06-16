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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
 * 17.6.2.4 RequiresNew
 * 
 * The Container must invoke an enterprise Bean method whose transaction 
 * attribute is set to RequiresNew with a new transaction context.
 * 
 * If the client invokes the enterprise Bean’s method while the client is not 
 * associated with a transaction context, the container automatically starts a
 * new transaction before delegating a method call to the enterprise Bean 
 * business method. The Container automatically enlists all the resource 
 * managers accessed by the business method with the transaction. If the 
 * business method invokes other enterprise beans, the Container passes the 
 * transaction context with the invocation. The Container attempts to commit
 * the transaction when the business method has completed. The container 
 * performs the commit protocol before the method result is sent to the client.
 * 
 * If a client calls with a transaction context, the container suspends the 
 * association of the transaction context with the current thread before 
 * starting the new transaction and invoking the business method. The container
 * resumes the suspended transaction association after the business method and 
 * the new transaction have been completed.
 *
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class TxRequiresNew extends TransactionPolicy {
    
    public TxRequiresNew(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxRequiresNew(){
        policyType = RequiresNew;
    }
    
    public String policyToString() {
        return "TX_RequiresNew: ";
    }
    
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        
        try {
        
            // if no transaction ---> suspend returns null
            context.clientTx  = suspendTransaction();
            beginTransaction();
            context.currentTx = getTxMngr().getTransaction();
        
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        try {
            // Commit or rollback
            if ( context.currentTx.getStatus() == Status.STATUS_ACTIVE ) {
                commitTransaction( context.currentTx );
            } else {
                rollbackTransaction( context.currentTx );
            }
        
        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        } finally {
            if ( context.clientTx != null ) {
                resumeTransaction( context.clientTx );
            } else if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"No transaction to resume");
            }            
        }
    }

    /**
     * <B>Container's action</B>
     * 
     * <P>
     * If the instance called setRollbackOnly(), then rollback the transaction, 
     * and re-throw AppException.
     * 
     * Otherwise, attempt to commit the transaction, and then re-throw 
     * AppException.
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
     * Rollback the container-started transaction.
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
     * Receives RemoteException or EJBException
     * 
     * If the client executes in a transaction, the client's transaction may or 
     * may not be marked for rollback.
     * </P>
     */
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        
        /* [1] Log the system exception or error **********/
        logSystemException( sysException );

        /* [2] afterInvoke will roll back the tx */
        markTxRollbackOnly( context.currentTx );

        /* [3] Discard instance. **************************/
        discardBeanInstance( instance, context.callContext);

        /* [4] Throw RemoteException to client ************/
        throwExceptionToServer( sysException );

    }
}

