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

import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.RollbackException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class ContainerPolicy {
    private static final Log log = LogFactory.getLog(ContainerPolicy.class);

    public static final TransactionPolicy NotSupported = new TxNotSupported();
    public static final TransactionPolicy Required = new TxRequired();
    public static final TransactionPolicy Supports = new TxSupports();
    public static final TransactionPolicy RequiresNew = new TxRequiresNew();
    public static final TransactionPolicy Mandatory = new TxMandatory();
    public static final TransactionPolicy Never = new TxNever();

    private static final class TxNotSupported implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();
            if (callerContext != null) {
                callerContext.suspend();
            }
            try {
                TransactionContext beanContext = transactionContextManager.newUnspecifiedTransactionContext();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    return result;
                } catch (Throwable t) {
                    beanContext.setRollbackOnly();
                    throw t;
                } finally {
                    beanContext.commit();
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                transactionContextManager.setContext(callerContext);
                if (callerContext != null) {
                    callerContext.resume();
                }
            }
        }
        public String toString() {
            return "NotSupported";
        }
        private Object readResolve() {
            return ContainerPolicy.NotSupported;
        }
    }

    private static final class TxRequired implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();
            if (callerContext != null && callerContext.isInheritable()) {
                try {
                    ejbInvocation.setTransactionContext(callerContext);
                    return interceptor.invoke(ejbInvocation);
                } catch (Throwable t){
                    callerContext.setRollbackOnly();
                    if (ejbInvocation.getType().isLocal()) {
                        throw new TransactionRolledbackLocalException().initCause(t);
                    } else {
                        // can't set an initCause on a TransactionRolledbackException
                        throw new TransactionRolledbackException(t.getMessage());
                    }
                } finally {
                    ejbInvocation.setTransactionContext(null);
                }
            }

            if (callerContext != null) {
                callerContext.suspend();
            }
            try {
                TransactionContext beanContext = transactionContextManager.newContainerTransactionContext();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    return result;
                } catch (RollbackException re) {
                    throw re;
                } catch (Throwable t) {
                    try {
                        beanContext.setRollbackOnly();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                } finally {
                    beanContext.commit();
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                transactionContextManager.setContext(callerContext);
                if (callerContext != null) {
                    callerContext.resume();
                }
            }
        }
        public String toString() {
            return "Required";
        }

        private Object readResolve() {
            return ContainerPolicy.Required;
        }
    }

    private static final class TxSupports implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();
            if (callerContext != null && callerContext.isInheritable()) {
                try {
                    ejbInvocation.setTransactionContext(callerContext);
                    return interceptor.invoke(ejbInvocation);
                } catch (Throwable t){
                    callerContext.setRollbackOnly();
                    if (ejbInvocation.getType().isLocal()) {
                        throw new TransactionRolledbackLocalException().initCause(t);
                    } else {
                        // can't set an initCause on a TransactionRolledbackException
                        throw new TransactionRolledbackException(t.getMessage());
                    }
                } finally {
                    ejbInvocation.setTransactionContext(null);
                }
            }

            if (callerContext != null) {
                callerContext.suspend();
            }
            try {
                TransactionContext beanContext = transactionContextManager.newUnspecifiedTransactionContext();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    return result;
                } catch (Throwable t) {
                    beanContext.setRollbackOnly();
                    throw t;
                } finally {
                    beanContext.commit();
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                transactionContextManager.setContext(callerContext);
                if (callerContext != null) {
                    callerContext.resume();
                }
            }
        }
        public String toString() {
            return "Supports";
        }

        private Object readResolve() {
            return ContainerPolicy.Supports;
        }
    }

    private static final class TxRequiresNew implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();

            if (callerContext != null) {
                callerContext.suspend();
            }
            try {
                TransactionContext beanContext = transactionContextManager.newContainerTransactionContext();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    return result;
                } catch (RollbackException re) {
                    throw re;
                } catch (Throwable t) {
                    try {
                        beanContext.setRollbackOnly();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                } finally {
                    beanContext.commit();
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                transactionContextManager.setContext(callerContext);
                if (callerContext != null) {
                    callerContext.resume();
                }
            }
        }
        public String toString() {
            return "RequiresNew";
        }

        private Object readResolve() {
            return ContainerPolicy.RequiresNew;
        }
    }

    private static final class TxMandatory implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();

            // If we don't have a transaction, throw an exception
            if (callerContext == null || !callerContext.isInheritable()) {
                if (ejbInvocation.getType().isLocal()) {
                    throw new TransactionRequiredLocalException();
                } else {
                    throw new TransactionRequiredException();
                }
            }

            try {
                ejbInvocation.setTransactionContext(callerContext);
                return interceptor.invoke(ejbInvocation);
            } catch (Throwable t) {
                callerContext.setRollbackOnly();
                if (ejbInvocation.getType().isLocal()) {
                    throw new TransactionRolledbackLocalException().initCause(t);
                } else {
                    // can't set an initCause on a TransactionRolledbackException
                    throw new TransactionRolledbackException(t.getMessage());
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
            }
        }
        public String toString() {
            return "Mandatory";
        }

        private Object readResolve() {
            return ContainerPolicy.Mandatory;
        }
    }

    private static final class TxNever implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
            TransactionContext callerContext = transactionContextManager.getContext();

            // If we have a transaction, throw an exception
            if (callerContext != null && callerContext.isInheritable()) {
                if (ejbInvocation.getType().isLocal()) {
                    throw new TransactionNotSupportedLocalException();
                } else {
                    throw new TransactionNotSupportedException();
                }
            }

            if (callerContext != null) {
                callerContext.suspend();
            }
            try {
                TransactionContext beanContext = transactionContextManager.newUnspecifiedTransactionContext();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    return result;
                } catch (Throwable t) {
                    beanContext.setRollbackOnly();
                    throw t;
                } finally {
                    beanContext.commit();
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                transactionContextManager.setContext(callerContext);
                if (callerContext != null) {
                    callerContext.resume();
                }
            }
        }
        public String toString() {
            return "Never";
        }

        private Object readResolve() {
            return ContainerPolicy.Never;
        }
    }
}
