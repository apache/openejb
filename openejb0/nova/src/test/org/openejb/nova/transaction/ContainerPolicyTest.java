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

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import junit.framework.TestCase;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.MockTransactionManager;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class ContainerPolicyTest extends TestCase {
    private MockInterceptor interceptor;
    private EJBInvocation invocation;
    private MockTransactionManager txnManager;

    public void testNotSupportedNoContext() throws Throwable {
        TransactionContext.setContext(null);
        ContainerPolicy.NotSupported.invoke(interceptor, invocation, txnManager);
        assertTrue(interceptor.context instanceof UnspecifiedTransactionContext);
    }

    public void testNotSupportedInContext() throws Throwable {
        MockUnspecifiedTransactionContext outer = new MockUnspecifiedTransactionContext(interceptor);
        TransactionContext.setContext(outer);

        ContainerPolicy.NotSupported.invoke(interceptor, invocation, txnManager);
        assertTrue(TransactionContext.getContext() == outer);
        assertTrue(interceptor.context instanceof UnspecifiedTransactionContext);
        assertTrue(interceptor.context != outer);
        assertTrue(outer.suspended);
        assertTrue(outer.resumed);
    }

    public void testRequiredNoContext() throws Throwable {
        TransactionContext.setContext(null);
        ContainerPolicy.Required.invoke(interceptor, invocation, txnManager);
        assertTrue(interceptor.context instanceof ContainerTransactionContext);
        assertTrue(txnManager.committed);
        assertFalse(txnManager.rolledBack);

        txnManager.committed = false;
        interceptor.throwException = true;
        try {
            ContainerPolicy.Required.invoke(interceptor, invocation, txnManager);
        } catch (MockSystemException e) {
        }
        assertTrue(interceptor.context instanceof ContainerTransactionContext);
        assertFalse(txnManager.committed);
        assertTrue(txnManager.rolledBack);
    }

    protected void setUp() throws Exception {
        super.setUp();
        txnManager = new MockTransactionManager();
        interceptor = new MockInterceptor();
        invocation = new EJBInvocationImpl(EJBInvocationType.LOCAL, 0, null);
    }

    private static class MockInterceptor implements Interceptor {
        private boolean throwException;
        private TransactionContext context;

        public Interceptor getNext() {
            throw new UnsupportedOperationException();
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            context = TransactionContext.getContext();
            if (throwException) {
                throw new MockSystemException();
            } else {
                return new SimpleInvocationResult(true, null);
            }
        }

        public void setNext(Interceptor interceptor) throws IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }

    private static class MockUnspecifiedTransactionContext extends UnspecifiedTransactionContext {
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
            super.suspend();
            suspended = true;
        }

        public void resume() {
            assertTrue(interceptor.context != null);
            assertTrue(suspended);
            assertFalse(resumed);
            super.resume();
            resumed = true;
        }
    }

    public static class MockSystemException extends RuntimeException {
    }
}
