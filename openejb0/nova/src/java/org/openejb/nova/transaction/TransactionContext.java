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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReleaser;
import org.openejb.nova.EJBContainer;
import org.openejb.nova.EJBInstanceContext;
import org.openejb.nova.entity.cmp.InstanceData;
import org.openejb.nova.util.DoubleKeyedHashMap;


/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class TransactionContext implements ConnectorTransactionContext{
    protected static final Log log = LogFactory.getLog(TransactionContext.class);
    private static ThreadLocal CONTEXT = new ThreadLocal();
    private Map managedConnections;

    public static TransactionContext getContext() {
        return (TransactionContext) CONTEXT.get();
    }

    public static void setContext(TransactionContext context) {
        CONTEXT.set(context);
    }

    private EJBInstanceContext currentContext;
    private final DoubleKeyedHashMap associatedContexts = new DoubleKeyedHashMap();
    private final DoubleKeyedHashMap dirtyContexts = new DoubleKeyedHashMap();
    private final DoubleKeyedHashMap instanceDataCache = new DoubleKeyedHashMap();

    public abstract void begin() throws SystemException, NotSupportedException;

    public abstract void suspend() throws SystemException;

    public abstract void resume() throws SystemException, InvalidTransactionException;

    public abstract void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException;

    public abstract void rollback() throws SystemException;

    public final void associate(EJBInstanceContext context) throws Exception {
        if (associatedContexts.put(context.getContainer(), context.getId(), context) == null) {
            context.associate();
        }
    }

    public final EJBInstanceContext beginInvocation(EJBInstanceContext context) {
        if (context.getId() != null) {
            dirtyContexts.put(context.getContainer(), context.getId(), context);
        }
        EJBInstanceContext caller = currentContext;
        currentContext = context;
        return caller;
    }

    public final void endInvocation(EJBInstanceContext caller) {
        currentContext = caller;
    }

    public final void flushState() throws Exception {
        while (dirtyContexts.isEmpty() == false) {
            ArrayList toFlush = new ArrayList(dirtyContexts.values());
            dirtyContexts.clear();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                EJBInstanceContext context = (EJBInstanceContext) i.next();
                context.flush();
            }
        }
        if (currentContext != null && currentContext.getId() != null) {
            dirtyContexts.put(currentContext.getContainer(), currentContext.getId(), currentContext);
        }
    }

    protected void beforeCommit() throws Exception {
        // @todo allow for enrollment during pre-commit
        ArrayList toFlush = new ArrayList(associatedContexts.values());
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            EJBInstanceContext context = (EJBInstanceContext) i.next();
            context.beforeCommit();
        }
    }

    protected void afterCommit(boolean status) throws Exception {
        // @todo allow for enrollment during pre-commit
        ArrayList toFlush = new ArrayList(associatedContexts.values());
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            EJBInstanceContext context = (EJBInstanceContext) i.next();
            context.afterCommit(status);
        }
    }

    public final EJBInstanceContext getContext(EJBContainer container, Object id) {
        return (EJBInstanceContext) associatedContexts.get(container, id);
    }

    public final void putInstanceData(EJBContainer container, Object id, InstanceData data) {
        instanceDataCache.put(container, id, data);
    }

    public final InstanceData getInstancedata(EJBContainer container, Object id) {
        return (InstanceData) instanceDataCache.get(container, id);
    }

    //Geronimo connector framework support
    public void setManagedConnectionInfo(ConnectionReleaser key, ManagedConnectionInfo info) {
        if (managedConnections == null) {
            managedConnections = new HashMap();
        }
        managedConnections.put(key, info);
    }

    public ManagedConnectionInfo getManagedConnectionInfo(ConnectionReleaser key) {
        if (managedConnections == null) {
            return null;
        }
        return (ManagedConnectionInfo) managedConnections.get(key);
    }

    public abstract boolean isActive();

    protected void connectorAfterCommit() {
        if (managedConnections != null) {
            for (Iterator entries = managedConnections.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                key.afterCompletion((ManagedConnectionInfo)entry.getValue());
            }
            //If BeanTransactionContext never reuses the same instance for sequential BMT, this
            //clearing is unnecessary.
            managedConnections.clear();
        }
    }

}
