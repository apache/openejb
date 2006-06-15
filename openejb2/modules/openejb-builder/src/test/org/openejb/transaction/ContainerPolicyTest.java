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
package org.openejb.transaction;

import javax.transaction.xa.XAResource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.Flushable;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.ConnectionReleaser;
import junit.framework.TestCase;

import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.EJBInterfaceType;
import org.tranql.cache.InTxCache;

/**
 * @version $Revision$ $Date$
 */
public class ContainerPolicyTest extends TestCase {
    private MockInterceptor interceptor;
    private EJBInvocation invocation;
    private MockTransactionManager txnManager;
    private TransactionContextManager transactionContextManager;

    public void testNotSupportedNoContext() throws Throwable {
        transactionContextManager.setContext(null);
        ContainerPolicy.NotSupported.invoke(interceptor, invocation, transactionContextManager);
        assertFalse(interceptor.context.isInheritable());
    }

    public void testNotSupportedInContext() throws Throwable {
        MockUnspecifiedTransactionContext outer = new MockUnspecifiedTransactionContext(interceptor);
        transactionContextManager.setContext(outer);

        ContainerPolicy.NotSupported.invoke(interceptor, invocation, transactionContextManager);
        assertTrue(transactionContextManager.getContext() == outer);
        assertFalse(interceptor.context.isInheritable());
        assertTrue(interceptor.context != outer);
        assertTrue(outer.suspended);
        assertTrue(outer.resumed);
    }

    public void testRequiredNoContext() throws Throwable {
        transactionContextManager.setContext(null);
        ContainerPolicy.Required.invoke(interceptor, invocation, transactionContextManager);
        assertTrue(interceptor.context instanceof TransactionContext);
        assertTrue(txnManager.isCommitted());
        assertFalse(txnManager.isRolledBack());

        txnManager.clear();
        interceptor.throwException = true;
        try {
            ContainerPolicy.Required.invoke(interceptor, invocation, transactionContextManager);
        } catch (MockSystemException e) {
        }
        assertTrue(interceptor.context instanceof TransactionContext);
        assertFalse(txnManager.isCommitted());
        assertTrue(txnManager.isRolledBack());
    }

    protected void setUp() throws Exception {
        super.setUp();
        txnManager = new MockTransactionManager();
        transactionContextManager = new TransactionContextManager(txnManager, null);
        interceptor = new MockInterceptor(transactionContextManager);
        invocation = new EJBInvocationImpl(EJBInterfaceType.LOCAL, 0, null);
    }

    private static class MockInterceptor implements Interceptor {
        private final TransactionContextManager transactionContextManager;
        private boolean throwException;
        private TransactionContext context;

        public MockInterceptor(TransactionContextManager transactionContextManager) {
            this.transactionContextManager = transactionContextManager;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            context = transactionContextManager.getContext();
            if (throwException) {
                throw new MockSystemException();
            } else {
                return new SimpleInvocationResult(true, null);
            }
        }

    }

    private static class MockUnspecifiedTransactionContext implements TransactionContext {
        MockInterceptor interceptor;
        boolean suspended;
        boolean resumed;

        public MockUnspecifiedTransactionContext(MockInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        public void suspend() {
            assertTrue(interceptor.context == null);
            assertFalse(suspended);
            assertFalse(resumed);
            suspended = true;
        }

        public void resume() {
            assertTrue(interceptor.context != null);
            assertTrue(suspended);
            assertFalse(resumed);
            resumed = true;
        }

        public boolean isInheritable() {
            return false;
        }

        public boolean isActive() {
            return true;
        }

        public boolean enlistResource(XAResource xaResource) throws RollbackException, SystemException {
            return false;
        }

        public boolean delistResource(XAResource xaResource, int flag) throws SystemException {
            return false;
        }

        public void registerSynchronization(Synchronization synchronization) throws RollbackException, SystemException {
        }

        public boolean getRollbackOnly() throws SystemException {
            return false;
        }

        public void setRollbackOnly() throws SystemException {
        }

        public boolean commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
            return false;
        }

        public void rollback() throws SystemException {
        }

        public void associate(InstanceContext context) throws Throwable {
        }

        public void unassociate(InstanceContext context) throws Throwable {
        }

        public void unassociate(Object containerId, Object id) throws Throwable {
        }

        public InstanceContext getContext(Object containerId, Object id) {
            return null;
        }

        public InstanceContext beginInvocation(InstanceContext context) throws Throwable {
            return null;
        }

        public void endInvocation(InstanceContext caller) {
        }

        public void flushState() throws Throwable {
        }

        public void setInTxCache(Flushable flushable) {
        }

        public Flushable getInTxCache() {
            return null;
        }

        public void setManagedConnectionInfo(ConnectionReleaser key, Object info) {
        }

        public Object getManagedConnectionInfo(ConnectionReleaser key) {
            return null;
        }
    }

    public static class MockSystemException extends RuntimeException {
    }
}
