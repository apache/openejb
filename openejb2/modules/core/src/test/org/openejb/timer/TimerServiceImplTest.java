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
package org.openejb.timer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.management.ObjectName;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.TestCase;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.timer.ExecutorTaskFactory;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.TransactionalExecutorTaskFactory;
import org.apache.geronimo.timer.UserTaskFactory;
import org.apache.geronimo.timer.WorkerPersistence;
import org.apache.geronimo.timer.vm.VMWorkerPersistence;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerServiceImplTest extends TestCase {

    private static final long SLOP = 300L;
    private static final long DELAY = 1000L;

    private static final String key = "testThreadPooledTimer";
    private static final String kernelName = "testKernel";
    private static final ObjectName timerSourceName = ObjectNameUtil.getObjectName("test:type=TimerService");
    private final ClassLoader classLoader = this.getClass().getClassLoader();
    private ThreadPool threadPool;
    private ThreadPooledTimer threadPooledTimer;

    private MockInterceptor interceptor;

    private BasicTimerServiceImpl timerService;

    protected TransactionContextManager transactionContextManager;
    protected ExecutorTaskFactory executableWorkFactory;
    protected UserTaskFactory userTaskFactory;

    private Object id = null;
    private Serializable userKey = "test user info";

    protected void setUp() throws Exception {
        TransactionManagerImpl transactionManager = new TransactionManagerImpl(10 * 1000,
                new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes()), null, null);
        transactionContextManager = new TransactionContextManager(transactionManager, transactionManager);
        executableWorkFactory = new TransactionalExecutorTaskFactory(transactionContextManager, 1);
        threadPool = new ThreadPool(5, "TestPool", 10000, this.getClass().getClassLoader(), "test:type=ThreadPool");
        WorkerPersistence workerPersistence = new VMWorkerPersistence();
        threadPooledTimer = new ThreadPooledTimer(executableWorkFactory, workerPersistence, threadPool, transactionContextManager);
        threadPooledTimer.doStart();

        transactionContextManager.setContext(null);

        interceptor = new MockInterceptor();
        timerService = new BasicTimerServiceImpl(new InvocationFactory(), interceptor, threadPooledTimer, key, kernelName, timerSourceName, transactionContextManager, classLoader);
    }

    protected void tearDown() throws Exception {
        threadPooledTimer.doStop();
        threadPool.doStop();
        timerService = null;
    }

    public void testSchedule1() throws Exception {
        Object id = new Integer(1);
        timerService.createTimer(id, 200L, userKey);
        Thread.sleep(200L + SLOP);
        assertEquals(1, interceptor.getCount());
        assertSame(id, interceptor.getId());
    }

    public void testSchedule2() throws Exception {
        timerService.createTimer(id, new Date(System.currentTimeMillis() + 20L), userKey);
        Thread.sleep(SLOP);
        assertEquals(1, interceptor.getCount());
    }

    public void testSchedule3() throws Exception {
        timerService.createTimer(id, 200L, DELAY, userKey);
        Thread.sleep(200L + SLOP + DELAY);
        assertEquals(2, interceptor.getCount());
    }

    public void testSchedule4() throws Exception {
        timerService.createTimer(id, new Date(System.currentTimeMillis()), DELAY, userKey);
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, interceptor.getCount());
    }

    public void testPersistence() throws Exception {
        Timer timer = timerService.createTimer(id, new Date(System.currentTimeMillis()+ DELAY), DELAY, userKey);
        Collection timers = timerService.getTimers(id);
        assertEquals(1, timers.size());
        assertSame(timer, timers.iterator().next());
        Thread.sleep(SLOP + DELAY);
        assertEquals(1, interceptor.getCount());

        threadPooledTimer.doStop();
        threadPooledTimer.doStart();
        timerService = new BasicTimerServiceImpl(new InvocationFactory(), interceptor, threadPooledTimer, key, kernelName, timerSourceName, transactionContextManager, classLoader);
        timerService.doStart();

        Collection timers2 = timerService.getTimers(id);
        assertEquals(1, timers2.size());
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, interceptor.getCount());
    }

    public void testCancel() throws Exception {
        Timer timer = timerService.createTimer(id, 0L, DELAY, userKey);
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, interceptor.getCount());
        TimerState.setTimerState(true);
        timer.cancel();
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, interceptor.getCount());
        assertEquals(0, timerService.getTimers(id).size());
        try {
            timer.cancel();
            fail("cancel should throw a NoSuchObjectLocalException");
        } catch (NoSuchObjectLocalException e) {
            //success
        }
    }


    private static class InvocationFactory implements EJBTimeoutInvocationFactory {
        public EJBInvocation getEJBTimeoutInvocation(Object id, TimerImpl timer) {
            return new EJBInvocationImpl(EJBInterfaceType.TIMEOUT, id, 0, new Object[] {timer});
        }

    }

    private static class MockInterceptor implements Interceptor {

        private final SynchronizedInt counter = new SynchronizedInt(0);
        private Object id;

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            id = ((EJBInvocation)invocation).getId();
            counter.increment();
            return null;
        }

        public int getCount() {
            return counter.get();
        }

        public Object getId() {
            return id;
        }
    }
}
