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
package org.openejb.entity;

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EJBInvocation;
import org.openejb.NotReentrantException;
import org.openejb.NotReentrantLocalException;
import org.openejb.cache.InstancePool;

/**
 * Simple Instance Interceptor that does not cache instances in the ready state
 * but passivates between each invocation.
 *
 * @version $Revision$ $Date$
 */
public final class EntityInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    private final Object containerId;
    private final InstancePool pool;
    private final boolean reentrant;

    public EntityInstanceInterceptor(Interceptor next, Object containerId, InstancePool pool, boolean reentrant) {
        this.next = next;
        this.containerId = containerId;
        this.pool = pool;
        this.reentrant = reentrant;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation) invocation;
        TransactionContext transactionContext = ejbInvocation.getTransactionContext();
        Object id = ejbInvocation.getId();

        boolean newContext = false;
        EntityInstanceContext context = null;
        // if we have an id then check if there is already a context associated with the transaction
        if ( id != null) {
            context = (EntityInstanceContext) transactionContext.getContext(containerId, id);
        }

        // if we didn't find an existing context, create a new one.
        if (context == null) {
            context = (EntityInstanceContext) pool.acquire();
            newContext = true;

            context.setTransactionContext(transactionContext);
            if (id != null) {
                // always activate on the way in....
                context.setId(id);
                try {
                    context.ejbActivate();
                } catch (Throwable t) {
                    // problem activating instance - discard it and throw the problem (will cause rollback)
                    pool.remove(context);
                    throw t;
                }

                // associate this instance with the TransactionContext
                try {
                    transactionContext.associate(context);
                } catch (NoSuchEntityException e) {
                    if (ejbInvocation.getType().isLocal()) {
                        throw new NoSuchObjectLocalException().initCause(e);
                    } else {
                        throw new NoSuchObjectException(e.getMessage());
                    }
                }
            }
        }

        ejbInvocation.setEJBInstanceContext(context);
        if (!reentrant && context.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("" + containerId);
            } else {
                throw new NotReentrantException("" + containerId);
            }
        }
        InstanceContext oldContext = transactionContext.beginInvocation(context);
        context.enter();
        boolean threwException = false;
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            threwException = true;
            throw t;
        } finally {
            context.exit();
            transactionContext.endInvocation(oldContext);
            ejbInvocation.setEJBInstanceContext(null);

            if (id == null) id = context.getId();

            if (id != null) {
                // passivate on the way out if this is a new context
                if (newContext) {
                    try {
                        context.flush();
                        context.ejbPassivate();
                    } catch (Throwable t) {
                        // problem passivating instance - discard it and throw the problem (will cause rollback)
                        pool.remove(context);
                        // throw this exception only if we are not already throwing a business exception
                        if (!threwException) throw t;
                    } finally {
                        context.setTransactionContext(null);
                        transactionContext.unassociate(context.getContainerId(), id);
                    }
                }
            }
        }
    }
}
