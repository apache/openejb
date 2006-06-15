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
package org.openejb.sfsb;

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.EJBInvocation;
import org.openejb.NotReentrantException;
import org.openejb.NotReentrantLocalException;
import org.openejb.cache.InstanceCache;
import org.openejb.cache.InstanceFactory;
import org.openejb.transaction.UncommittedTransactionException;

/**
 * Interceptor for Stateful Session EJBs that acquires an instance for execution.
 * For create methods it creates a new context using the factory, and for every
 * thing else it gets the context from either the main or transaction cache.
 *
 * @version $Revision$ $Date$
 */
public final class StatefulInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    private final Object containerId;
    private final InstanceFactory factory;
    private final InstanceCache cache;
    private final TransactionContextManager transactionContextManager;

    public StatefulInstanceInterceptor(Interceptor next, Object containerId, InstanceFactory factory, InstanceCache cache, TransactionContextManager transactionContextManager) {
        this.next = next;
        this.containerId = containerId;
        this.factory = factory;
        this.cache = cache;
        this.transactionContextManager = transactionContextManager;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation) invocation;

        // initialize the context and set it into the invocation
        StatefulInstanceContext ctx = getInstanceContext(ejbInvocation);
        ejbInvocation.setEJBInstanceContext(ctx);

        // resume the preexisting transaction context
        if (ctx.getPreexistingContext() != null) {
            TransactionContext preexistingContext = ctx.getPreexistingContext();
            transactionContextManager.resumeBeanTransactionContext(preexistingContext);
            ctx.setPreexistingContext(null);
        }

        // check reentrancy
        if (ctx.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("Stateful session beans do not support reentrancy: " + containerId);
            } else {
                throw new NotReentrantException("Stateful session beans do not support reentrancy: " + containerId);
            }
        }

        TransactionContext oldTransactionContext = ejbInvocation.getTransactionContext();
        InstanceContext oldInstanceContext = oldTransactionContext.beginInvocation(ctx);
        try {
            // invoke next
            InvocationResult invocationResult = next.invoke(invocation);

            // if we have a BMT still associated with the thread, suspend it and save it off for the next invocation
            TransactionContext currentContext = transactionContextManager.getContext();
            if (oldTransactionContext != currentContext) {
                TransactionContext preexistingContext = transactionContextManager.suspendBeanTransactionContext();
                ctx.setPreexistingContext(preexistingContext);
                if (transactionContextManager.getContext() != oldTransactionContext) {
                    throw new UncommittedTransactionException("Found an uncommitted bean transaction from another session bean");
                }
            }

            return invocationResult;
        } catch(Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();

            throw t;
        } finally {
            oldTransactionContext.endInvocation(oldInstanceContext);
            ejbInvocation.setEJBInstanceContext(null);
        }
    }

    private StatefulInstanceContext getInstanceContext(EJBInvocation ejbInvocation) throws Throwable {
        StatefulInstanceContext ctx;
        Object id = ejbInvocation.getId();
        if (id == null) {
            // we don't have an id so we are a create method
            ctx = (StatefulInstanceContext) factory.createInstance();
            assert ctx.getInstance() != null: "Got a context with no instance assigned";
            id = ctx.getId();
            ctx.setCache(cache);
            cache.putActive(id, ctx);
        } else {
            // first check the transaction cache
            TransactionContext transactionContext = ejbInvocation.getTransactionContext();
            ctx = (StatefulInstanceContext) transactionContext.getContext(containerId, id);
            if (ctx == null) {
                // next check the main cache
                ctx = (StatefulInstanceContext) cache.get(id);
                if (ctx == null) {
                    // bean is no longer cached or never existed
                    if (ejbInvocation.getType().isLocal()) {
                        throw new NoSuchObjectLocalException(id.toString());
                    } else {
                        throw new NoSuchObjectException(id.toString());
                    }
                }
            }

            if (ctx.isDead()) {
                if (ejbInvocation.getType().isLocal()) {
                    throw new NoSuchObjectLocalException("Instance has been removed or threw a system exception: id=" + id.toString());
                } else {
                    throw new NoSuchObjectException("Instance has been removed or threw a system exception: id=" + id.toString());
                }
            }
        }
        return ctx;
    }
}
