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
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.BeanTransactionContext;

/**
 * Implementation of UserTransaction for use in an EJB.
 * This adds the ability to enable or disable the operations depending on
 * the lifecycle of the EJB instance.
 *
 * @version $Revision$ $Date$
 */
public class EJBUserTransaction implements UserTransaction {
    private TransactionManager txnManager;
    private TrackedConnectionAssociator trackedConnectionAssociator;
    private final ThreadLocal state = new ThreadLocal() {
        protected Object initialValue() {
            return OFFLINE;
        }
    };

    public EJBUserTransaction() {
        state.set(OFFLINE);
    }

    public void setUp(TransactionManager txnManager, TrackedConnectionAssociator trackedConnectionAssociator) {
        assert !isOnline() : "Only set the tx manager when UserTransaction is offline";
        this.txnManager = txnManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public boolean isOnline() {
        return state.get() == ONLINE;
    }

    public void setOnline(boolean online) {
        //too bad there's no implies operation
        // online implies transactionManager != null
        assert !online & txnManager != null : "online requires a tx manager";
        state.set(online ? ONLINE : OFFLINE);
    }

    private UserTransaction getUserTransaction() {
        return (UserTransaction) state.get();
    }

    public void begin() throws NotSupportedException, SystemException {
        getUserTransaction().begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        getUserTransaction().commit();
    }

    public int getStatus() throws SystemException {
        return getUserTransaction().getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        getUserTransaction().rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getUserTransaction().setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        getUserTransaction().setTransactionTimeout(timeout);
    }

    private final UserTransaction ONLINE = new UserTransaction() {
        public int getStatus() throws SystemException {
            return txnManager.getStatus();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            txnManager.setRollbackOnly();
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            txnManager.setTransactionTimeout(seconds);
        }

        public void begin() throws NotSupportedException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof UnspecifiedTransactionContext == false) {
                throw new NotSupportedException("Previous Transaction has not been committed");
            }
            UnspecifiedTransactionContext oldContext = (UnspecifiedTransactionContext) ctx;
            BeanTransactionContext newContext = new BeanTransactionContext(txnManager, oldContext);
            oldContext.suspend();
            try {
                newContext.begin();
            } catch (SystemException e) {
                oldContext.resume();
                throw e;
            } catch (NotSupportedException e) {
                oldContext.resume();
                throw e;
            }
            TransactionContext.setContext(newContext);
            if (trackedConnectionAssociator != null){
                try {
                    trackedConnectionAssociator.setTransactionContext(newContext);
                } catch (ResourceException e) {
                    throw (SystemException)new SystemException("could not enroll existing connections in transaction").initCause(e);
                }
            }
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof BeanTransactionContext == false) {
                throw new IllegalStateException("Transaction has not been started");
            }
            BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
            try {
                beanContext.commit();
            } finally {
                UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
                TransactionContext.setContext(oldContext);
                if (trackedConnectionAssociator != null){
                    trackedConnectionAssociator.resetTransactionContext(oldContext);
                }
                oldContext.resume();
            }
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            TransactionContext ctx = TransactionContext.getContext();
            if (ctx instanceof BeanTransactionContext == false) {
                throw new IllegalStateException("Transaction has not been started");
            }
            BeanTransactionContext beanContext = (BeanTransactionContext) ctx;
            try {
                beanContext.rollback();
            } finally {
                UnspecifiedTransactionContext oldContext = beanContext.getOldContext();
                TransactionContext.setContext(oldContext);
                oldContext.resume();
            }
        }
    };

    private static final UserTransaction OFFLINE = new UserTransaction() {
        public void begin() throws NotSupportedException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public int getStatus() throws SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            throw new IllegalStateException("Cannot use UserTransaction methods in this state");
        }
    };
}
