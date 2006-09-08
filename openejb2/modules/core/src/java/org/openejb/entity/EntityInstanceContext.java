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

import java.util.Set;
import javax.ejb.EnterpriseBean;
import javax.ejb.EntityContext;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.AbstractInstanceContext;
import org.openejb.EJBInvocation;
import org.openejb.EJBOperation;
import org.openejb.EJBContextImpl;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.BasicTimerService;

/**
 * @version $Revision$ $Date$
 */
public abstract class EntityInstanceContext extends AbstractInstanceContext {
    private final EntityContextImpl entityContext;
    private final EJBInvocation setContextInvocation;
    private final EJBInvocation unsetContextInvocation;
    private final EJBInvocation ejbActivateInvocation;
    private final EJBInvocation ejbPassivateInvocation;
    private final EJBInvocation loadInvocation;
    private final EJBInvocation storeInvocation;
    private Object id;
    private boolean loaded = false;
    private InstancePool pool;

    public EntityInstanceContext(Object containerId, EJBProxyFactory proxyFactory, EnterpriseBean instance, Interceptor lifecycleInterceptorChain, SystemMethodIndices systemMethodIndices, Set unshareableResources, Set applicationManagedSecurityResources, TransactionContextManager transactionContextManager, BasicTimerService timerService) {
        super(containerId, instance, lifecycleInterceptorChain, proxyFactory, timerService, unshareableResources, applicationManagedSecurityResources);
        entityContext = new EntityContextImpl(this, transactionContextManager);
        ejbActivateInvocation = systemMethodIndices.getEjbActivateInvocation(this);
        ejbPassivateInvocation = systemMethodIndices.getEjbPassivateInvocation(this);
        loadInvocation = systemMethodIndices.getEjbLoadInvocation(this);
        storeInvocation = systemMethodIndices.getEjbStoreInvocation(this);
        setContextInvocation = systemMethodIndices.getSetContextInvocation(this, entityContext);
        unsetContextInvocation = systemMethodIndices.getUnsetContextInvocation(this);
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public InstancePool getPool() {
        return pool;
    }

    public void setPool(InstancePool pool) {
        this.pool = pool;
    }

    public void setOperation(EJBOperation operation) {
        entityContext.setState(operation);
    }

    public boolean setTimerState(EJBOperation operation) {
        return entityContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return entityContext;
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        loadInvocation.setTransactionContext(transactionContext);
        storeInvocation.setTransactionContext(transactionContext);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void die() {
        if (pool != null) {
            pool.remove(this);
            pool = null;
        }
        loaded = false;
        setTransactionContext(null);
        super.die();
    }

    public void associate() throws Throwable {
        super.associate();
        if (id != null && !loaded) {
            ejbActivate();
            ejbLoad();
            loaded = true;
        }
    }

    public void unassociate() throws Throwable {
        super.unassociate();
        try {
            if (!isDead()) {
                if (id != null) {
                    ejbPassivate();
                }
                loaded = false;
                setTransactionContext(null);
                if (pool != null) {
                    pool.release(this);
                }
            }
        } catch (Throwable t) {
            // problem passivating instance - discard it and throw the problem (will cause rollback)
            if (pool != null) {
                pool.remove(this);
            }
            throw t;
        }
    }

    public void beforeCommit() throws Throwable {
        super.beforeCommit();
        flush();
    }

    public void flush() throws Throwable {
        super.flush();
        if (id != null) {
            if (!loaded) {
                throw new IllegalStateException("Trying to invoke ejbStore on an unloaded instance");
            }
            ejbStore();
        }
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(setContextInvocation);
    }

    public void unsetContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(unsetContextInvocation);
    }

    protected void ejbActivate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(ejbActivateInvocation);
    }

    protected void ejbPassivate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(ejbPassivateInvocation);
    }

    protected void ejbLoad() throws Throwable {
        systemChain.invoke(loadInvocation);
    }

    public void ejbStore() throws Throwable {
        systemChain.invoke(storeInvocation);
    }
}
