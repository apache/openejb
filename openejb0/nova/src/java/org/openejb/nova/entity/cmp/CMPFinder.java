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
package org.openejb.nova.entity.cmp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;

import org.openejb.nova.EJBContainer;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.persistence.QueryCommand;
import org.openejb.nova.persistence.Tuple;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPFinder implements VirtualOperation {
    private final EJBContainer container;
    private final QueryCommand finderCommand;
    private final boolean multiValued;

    public CMPFinder(EJBContainer container, QueryCommand command, boolean multiValue) {
        this.container = container;
        this.finderCommand = command;
        this.multiValued = multiValue;
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        List finderResult = finderCommand.executeQuery(invocation.getArguments());

        boolean local = invocation.getType().isLocal();

        if (multiValued) {
            ArrayList result = new ArrayList(finderResult.size());
            for (Iterator iterator = finderResult.iterator(); iterator.hasNext();) {
                Tuple tuple = (Tuple) iterator.next();
                Object pk = tuple.getValue(0);
                result.add(getReference(local, pk));
            }
            return new SimpleInvocationResult(true, result);
        } else {
            if (finderResult.size() == 0) {
                return new SimpleInvocationResult(false, new ObjectNotFoundException());
            } else if (finderResult.size() > 1) {
                return new SimpleInvocationResult(false, new FinderException("Query returned more than one result"));
            }
            Tuple tuple = (Tuple)finderResult.get(0);
            Object pk = tuple.getValue(0);
            return new SimpleInvocationResult(true, getReference(local, pk));
        }
    }

    private Object getReference(boolean local, Object id) {
        if (id == null) {
            // yes, finders can return null
            return null;
        } else if (local) {
            return container.getEJBLocalObject(id);
        } else {
            return container.getEJBObject(id);
        }
    }
}
