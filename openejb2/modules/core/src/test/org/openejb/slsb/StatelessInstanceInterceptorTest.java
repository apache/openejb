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
package org.openejb.slsb;

import java.util.HashSet;

import javax.ejb.Timer;
import javax.ejb.SessionContext;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationKey;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;

import junit.framework.TestCase;

import org.openejb.cache.InstancePool;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.EJBInterfaceType;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.dispatch.InterfaceMethodSignature;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessInstanceInterceptorTest extends TestCase {
    private static final InvocationKey KEY = new InvocationKey() {
        public boolean isTransient() {
            return false;
        }
    };
    private TransactionManagerImpl transactionManager;
    private TransactionContextManager transactionContextManager;
    private MockPool pool;
    private StatelessInstanceInterceptor interceptor;
    private EJBInstanceContext ctx;
    private MockEJB mockEJB;
    private final static InterfaceMethodSignature[] signatures = new InterfaceMethodSignature[] {
        new InterfaceMethodSignature("ejbActivate", false),
        new InterfaceMethodSignature("ejbLoad", false),
        new InterfaceMethodSignature("ejbPassivate", false),
        new InterfaceMethodSignature("ejbStore", false),
        new InterfaceMethodSignature("ejbCreate", false),
        new InterfaceMethodSignature("ejbRemove", false),
        new InterfaceMethodSignature("ejbTimeout", new Class[] {Timer.class}, false),
        new InterfaceMethodSignature("setSessionContext", new Class[] {SessionContext.class}, false),
    };


    public void testNormalInvocation() throws Throwable {
        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInterfaceType.REMOTE, 0, null);
        invocation.setTransactionContext(transactionContextManager.newUnspecifiedTransactionContext());
        invocation.put(KEY, Boolean.FALSE);
        assertNull(interceptor.invoke(invocation));
        assertNull(invocation.getEJBInstanceContext());
        assertNotNull(ctx);
        assertTrue(mockEJB == ctx.getInstance());
        assertTrue(pool.released);
    }

    public void testSystemException() throws Throwable {
        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInterfaceType.REMOTE, 0, null);
        invocation.setTransactionContext(transactionContextManager.newUnspecifiedTransactionContext());
        invocation.put(KEY, Boolean.TRUE);
        try {
            interceptor.invoke(invocation);
            fail("Expected system exception");
        } catch (RuntimeException e) {
            // ok
        }
        assertNull(invocation.getEJBInstanceContext());
        assertNotNull(ctx);
        assertTrue(mockEJB == ctx.getInstance());
        assertTrue(pool.removed);
    }

    protected void setUp() throws Exception {
        super.setUp();

        transactionManager = new TransactionManagerImpl(10 * 1000, 
                new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes()), null, null);
        transactionContextManager = new TransactionContextManager(transactionManager, transactionManager);
        mockEJB = new MockEJB();
        pool = new MockPool(mockEJB);
        interceptor = new StatelessInstanceInterceptor(new MockInterceptor(), pool);
    }

    private static class MockPool implements InstancePool {
        private final EJBInstanceContext ctx;
        private boolean released;
        private boolean removed;

        public MockPool(MockEJB mockEJB) {
            ctx = new StatelessInstanceContext("containerId",
                    mockEJB,
                    null,
//                    transactionContextManager,
                    null,
                    null,
                    SystemMethodIndices.createSystemMethodIndices(signatures, "setSessionContext", SessionContext.class.getName(), null),
                    null,
                    new HashSet(),
                    new HashSet(),
                    null);
        }

        public Object acquire() throws InterruptedException, Exception {
            return ctx;
        }

        public boolean release(Object instance) {
            released = true;
            return false;
        }

        public void remove(Object instance) {
            removed = true;
        }
    }

    private class MockInterceptor implements Interceptor {
        public InvocationResult invoke(Invocation invocation) throws Throwable {
            ctx = ((EJBInvocation) invocation).getEJBInstanceContext();
            if (invocation.get(KEY) == Boolean.FALSE) {
                return null;
            } else {
                throw new RuntimeException();
            }
        }
    }
}
