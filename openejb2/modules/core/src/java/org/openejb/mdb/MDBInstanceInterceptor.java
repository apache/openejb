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
package org.openejb.mdb;

import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.cache.InstancePool;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.context.TransactionContext;


/**
 * Interceptor for MDB EJBs that obtains an instance
 * from a pool to execute the method.
 *
 * @version $Revision$ $Date$
 */
public final class MDBInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    private final InstancePool pool;

    public MDBInstanceInterceptor(Interceptor next, InstancePool pool) {
        this.next = next;
        this.pool = pool;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation) invocation;

        assert (ejbInvocation.getType() != EJBInterfaceType.HOME && ejbInvocation.getType() != EJBInterfaceType.LOCALHOME) : "Cannot invoke home method on a MDB";

        // get the context
        MDBInstanceContext ctx = (MDBInstanceContext) pool.acquire();
        assert ctx.getInstance() != null: "Got a context with no instance assigned";
        assert !ctx.isInCall() : "Acquired a context already in an invocation";
        ctx.setPool(pool);

        // initialize the context and set it into the invocation
        ejbInvocation.setEJBInstanceContext(ctx);

        TransactionContext transactionContext = ejbInvocation.getTransactionContext();
        InstanceContext oldContext = transactionContext.beginInvocation(ctx);
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            throw t;
        } finally {
            transactionContext.endInvocation(oldContext);

            // remove the reference to the context from the invocation
            ejbInvocation.setEJBInstanceContext(null);
        }
    }
}
