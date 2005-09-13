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

import java.util.Set;
import javax.ejb.EntityBean;
import javax.ejb.NoSuchEntityException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.entity.EntityInstanceContext;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.BasicTimerService;
import org.tranql.cache.CacheRow;
import org.tranql.cache.CacheRowState;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.identity.IdentityTransform;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class CMPInstanceContext extends EntityInstanceContext {
    private final CMP1Bridge cmp1Bridge;
    private final FaultHandler loadFault;
    private final IdentityTransform primaryKeyTransform;
    private CacheRow cacheRow;
    private TransactionContext transactionContext;

    public CMPInstanceContext(Object containerId,
            EJBProxyFactory proxyFactory,
            EntityBean entityBean,
            CMP1Bridge cmp1Bridge,
            FaultHandler loadFault,
            IdentityTransform primaryKeyTransform,
            Interceptor lifecycleInterceptorChain,
            SystemMethodIndices systemMethodIndices,
            Set unshareableResources,
            Set applicationManagedSecurityResources,
            TransactionContextManager transactionContextManager,
            BasicTimerService timerService) throws Exception {

        super(containerId,
                proxyFactory,
                entityBean,
                lifecycleInterceptorChain,
                systemMethodIndices,
                unshareableResources,
                applicationManagedSecurityResources,
                transactionContextManager,
                timerService);

        this.cmp1Bridge = cmp1Bridge;
        this.loadFault = loadFault;
        this.primaryKeyTransform = primaryKeyTransform;
    }

    public CacheRow getCacheRow() {
        return cacheRow;
    }

    public void setCacheRow(CacheRow cacheRow) {
        this.cacheRow = cacheRow;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public void associate() throws Throwable {
        //
        // don't call super because it activates and
        // loads the ejb and we want to controll that
        //

        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }

        Object id = getId();
        if (id != null && !isLoaded()) {
            // acitvate the instance
            ejbActivate();

            // locate the cache row for this instance
            GlobalIdentity globalId = primaryKeyTransform.getGlobalIdentity(id);
            InTxCache inTxCache = (InTxCache) transactionContext.getInTxCache();
            cacheRow = inTxCache.get(globalId);

            // if we don't already have the row execute the load fault handler
            if (cacheRow == null) {
                loadFault.rowFault(inTxCache, globalId);
                cacheRow = inTxCache.get(globalId);
            }

            // if we still don't have a row, we can only assume that they have an old ref to the ejb
            if(cacheRow == null) {
                // how to get the name of the bean
                throw new NoSuchEntityException("Entity " + this + " not found");
            }

            // check that the row is not tagged as removed
            if(cacheRow.getState() == CacheRowState.REMOVED) {
                throw new NoSuchEntityException("Entity " + this + " has been removed");
            }

            // copy data from tranql into instance
            if (cmp1Bridge != null) {
                cmp1Bridge.loadEntityBean(cacheRow, this);
            }

            // call ejbLoad
            ejbLoad();
            setLoaded(true);
        }
    }

    public void flush() throws Throwable {
        // calls ejbStore
        super.flush();

        // copy data from instance into tranql
        if (cmp1Bridge != null && cacheRow != null) {
            cmp1Bridge.loadCacheRow(this, cacheRow);
        }
    }

    public void afterCommit(boolean status) throws Throwable {
        super.afterCommit(status);
        transactionContext = null;
    }

    public void addRelation(int slot, Object primaryKey) {
    }

    public void removeRelation(int slot, Object primaryKey) {
    }
}
