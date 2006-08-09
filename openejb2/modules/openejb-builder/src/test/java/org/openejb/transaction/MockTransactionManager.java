/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @version $Revision$ $Date$
 */
public class MockTransactionManager implements TransactionManager {
    private MockTransaction transaction;
    private MockTransaction lastTransaction;

    public boolean wasLastTxCommitted() {
        if (lastTransaction == null) throw new IllegalStateException("lastTransaction is null");
        return lastTransaction.committed;
    }

    public boolean wasLastTxRolledBack() {
        if (lastTransaction == null) throw new IllegalStateException("lastTransaction is null");
        return lastTransaction.rolledBack;
    }

    public void clear() {
        transaction = null;
    }

    public void begin() throws NotSupportedException, SystemException {
        if (transaction != null) throw new NotSupportedException("Transacion in progress");
        transaction = new MockTransaction();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.commit();
        lastTransaction = transaction;
        transaction = null;
    }

    public int getStatus() throws SystemException {
        if (transaction == null) return Status.STATUS_NO_TRANSACTION;
        return transaction.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return transaction;
    }

    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        if (transaction == null) {
            throw new InvalidTransactionException("transaction is null");
        }
        if (!(transaction instanceof MockTransaction)) {
            throw new InvalidTransactionException("Expected instance of MockTransaction but was " + transaction.getClass().getName());
        }
        if (this.transaction != null) throw new IllegalStateException("Transacion in progress");
        this.transaction = (MockTransaction) transaction;
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.rollback();
        lastTransaction = transaction;
        transaction = null;
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (transaction == null) throw new IllegalStateException("No transacion in progress");
        transaction.setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
    }

    public Transaction suspend() throws SystemException {
        MockTransaction transaction = this.transaction;
        this.transaction = null;
        return transaction;
    }
}
