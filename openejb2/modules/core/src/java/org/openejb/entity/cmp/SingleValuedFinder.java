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

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.EJBInvocation;
import org.tranql.field.FieldTransform;
import org.tranql.field.FieldTransformException;
import org.tranql.field.Row;
import org.tranql.identity.IdentityDefiner;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
public class SingleValuedFinder extends CMPFinder {
    private static final Object NODATA = new Object();

    public SingleValuedFinder(QueryCommand localCommand, QueryCommand remoteCommand, boolean flushCache, IdentityDefiner idDefiner, IdentityDefiner idInjector) {
        super(localCommand, remoteCommand, flushCache, idDefiner, idInjector);
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        try {
            QueryCommand command = getCommand(invocation);
            FieldTransform resultAccessor = command.getQuery().getResultAccessors()[0];
            SingleValuedResultHandler handler = new SingleValuedResultHandler(resultAccessor, invocation);
            Object o = execute(invocation, handler, NODATA);
            return o == NODATA ? invocation.createExceptionResult(new ObjectNotFoundException()) : (InvocationResult) o;
        } catch (QueryException e) {
            return invocation.createExceptionResult((Exception)new FinderException(e.getMessage()).initCause(e));
        }
    }

    private class SingleValuedResultHandler implements ResultHandler {
        private final EJBInvocation invocation;
        private final FieldTransform accessor;
        public SingleValuedResultHandler(FieldTransform accessor, EJBInvocation invocation) {
            this.accessor = accessor;
            this.invocation = invocation;
        }

        public Object fetched(Row row, Object arg) throws QueryException {
            if (arg == NODATA) {
                try {
                    Object opaque = accessor.get(row);
                    return invocation.createResult(opaque);
                } catch (FieldTransformException e) {
                    throw new QueryException(e);
                }
            }
            return invocation.createExceptionResult(new FinderException("More than one row returned from single valued finder"));
        }
        
        public Object endFetched(Object arg0) throws QueryException {
            return arg0;
        }
    }
}