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
package org.openejb.nova.transaction;

import java.util.HashMap;
import java.util.Map;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.openejb.nova.EJBInstanceContext;
import org.openejb.nova.InstanceID;
import org.openejb.nova.entity.cmp.InstanceData;


/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class TransactionContext {
    private static ThreadLocal CONTEXT = new ThreadLocal();

    public static TransactionContext getContext() {
        return (TransactionContext) CONTEXT.get();
    }

    public static void setContext(TransactionContext context) {
        CONTEXT.set(context);
    }

    private final Map contextCache = new HashMap();
    private final Map instanceDataCache = new HashMap();

    public abstract void begin() throws SystemException, NotSupportedException;

    public abstract void suspend() throws SystemException;

    public abstract void resume() throws SystemException, InvalidTransactionException;

    public abstract void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    public abstract void rollback() throws SystemException;

    public final void putContext(InstanceID id, EJBInstanceContext context) {
        contextCache.put(id, context);
    }

    public final EJBInstanceContext getContext(InstanceID id) {
        return (EJBInstanceContext) contextCache.get(id);
    }

    public final void putInstanceData(InstanceID id, InstanceData data) {
        instanceDataCache.put(id, data);
    }

    public final InstanceData getInstancedata(InstanceID id) {
        return (InstanceData) instanceDataCache.get(id);
    }
}
