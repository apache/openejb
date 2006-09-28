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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.openejb.EJBInterfaceType;
import org.openejb.EjbInvocation;
import org.openejb.EjbInvocationImpl;

/**
 * @version $Revision$ $Date$
 */
public class ContainerPolicyTest extends TestCase {
    private MockInterceptor interceptor;
    private EjbInvocation invocation;
    private MockTransactionManager transactionManager;

    public void testNotSupportedNoContext() throws Throwable {
        ContainerPolicy.NotSupported.invoke(interceptor, invocation, transactionManager);
        assertNull(interceptor.transaction);
    }

    public void testRequiredNoContext() throws Throwable {
        ContainerPolicy.Required.invoke(interceptor, invocation, transactionManager);
        assertNotNull(interceptor.transaction);
        assertTrue(transactionManager.wasLastTxCommitted());
        assertFalse(transactionManager.wasLastTxRolledBack());

        transactionManager.clear();
        interceptor.throwException = true;
        try {
            ContainerPolicy.Required.invoke(interceptor, invocation, transactionManager);
        } catch (MockSystemException e) {
        }
        assertNotNull(interceptor.transaction);
        assertFalse(transactionManager.wasLastTxCommitted());
        assertTrue(transactionManager.wasLastTxRolledBack());
    }

    protected void setUp() throws Exception {
        super.setUp();
        transactionManager = new MockTransactionManager();
        interceptor = new MockInterceptor(transactionManager);
        invocation = new EjbInvocationImpl(EJBInterfaceType.LOCAL, 0, null);
    }

    private static class MockInterceptor implements Interceptor {
        private final TransactionManager transactionManager;
        private boolean throwException;
        private Transaction transaction;

        public MockInterceptor(TransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            transaction = transactionManager.getTransaction();
            if (throwException) {
                throw new MockSystemException();
            } else {
                return new SimpleInvocationResult(true, null);
            }
        }

    }

    public static class MockSystemException extends RuntimeException {
    }
}
