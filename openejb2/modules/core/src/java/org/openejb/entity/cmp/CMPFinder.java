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
package org.openejb.entity.cmp;

import java.io.Serializable;

import org.openejb.EJBInvocation;
import org.openejb.dispatch.VirtualOperation;
import org.tranql.cache.InTxCache;
import org.tranql.field.Row;
import org.tranql.identity.IdentityDefiner;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;

/**
 * @version $Revision$ $Date$
 */
public abstract class CMPFinder implements VirtualOperation, Serializable {
    private final QueryCommand localCommand;
    private final QueryCommand remoteCommand;
    private final boolean flushCache;
    private final IdentityDefiner idDefiner;
    private final IdentityDefiner idInjector;

    public CMPFinder(QueryCommand localCommand, QueryCommand remoteCommand, boolean flushCache, IdentityDefiner idDefiner, IdentityDefiner idInjector) {
        this.localCommand = localCommand;
        this.remoteCommand = remoteCommand;
        this.flushCache = flushCache;
        this.idDefiner = idDefiner;
        this.idInjector = idInjector;
    }

    protected QueryCommand getCommand(EJBInvocation invocation) {
        return invocation.getType().isLocal() ? localCommand : remoteCommand;
    }

    protected Object execute(EJBInvocation invocation, ResultHandler handler, Object ctx) throws QueryException {
        InTxCache cache = (InTxCache) invocation.getTransactionContext().getInTxCache();
        if (flushCache) {
            cache.flush();
        }

        QueryCommand command = getCommand(invocation);
        Row arguments = new Row(invocation.getArguments());
        handler = new CacheFiller(handler, idDefiner, idInjector, cache);
        try {
            return command.execute(cache, handler, arguments, ctx);
        } catch (IllegalStateException e) {
            throw new QueryException("Unable to execute finder; perhaps the cmp-connection-factory was not configured correctly?  Error message is: "+e.getMessage());
            // The handling for this is kind of awkward -- the user configured a bad cmp-connection-factory
            // and we didn't validate it during deployment for various reasons.  So the only way we discover
            // it is that at runtime TranQL detects that it doesn't have a database connection.  Since it's
            // a little late to throw a deployment exception, and TranQL doesn't know specifically about
            // OpenEJB, it just throws a sort of generic IllegalStateException.  We catch that here and try
            // to be a little more specific, but it's still not 100% sure that we got into this situation
            // exactly because of that initial cause.  See GERONIMO-1176 for a bit more related information.
            // See TranQL org.tranql.sql.DataSourceDelegate.getConnection for the exception this is catching.
            // todo: see if we can detect this when we set the database connection on TranQL in the first place
        }
    }
}