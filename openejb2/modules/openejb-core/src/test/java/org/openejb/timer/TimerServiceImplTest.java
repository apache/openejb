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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import junit.framework.TestCase;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.timer.ExecutorTaskFactory;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.TransactionalExecutorTaskFactory;
import org.apache.geronimo.timer.UserTaskFactory;
import org.apache.geronimo.timer.WorkerPersistence;
import org.apache.geronimo.timer.vm.VMWorkerPersistence;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.openejb.EjbContainer;
import org.openejb.EjbDeployment;
import org.openejb.ExtendedEjbDeployment;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.security.PermissionManager;
import org.openejb.transaction.TransactionPolicyManager;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerServiceImplTest extends TestCase {

    private static final long SLOP = 300L;
    private static final long DELAY = 1000L;

    private static final String kernelName = "testKernel";
    private static final ObjectName timerSourceName = ObjectNameUtil.getObjectName("test:type=TimerService");
    private ThreadPool threadPool;
    private ThreadPooledTimer threadPooledTimer;

    private BasicTimerServiceImpl timerService;

    protected TransactionManager transactionManager;
    protected ExecutorTaskFactory executableWorkFactory;
    protected UserTaskFactory userTaskFactory;

    private Object id = null;
    private Serializable userKey = "test user info";
    private MockEjbDeployment ejbDeployment;
    private MockEjbContainer ejbContainer;

    public TimerServiceImplTest() {
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.transactionManager = new GeronimoTransactionManager();
        executableWorkFactory = new TransactionalExecutorTaskFactory(this.transactionManager, 1);
        threadPool = new ThreadPool(5, "TestPool", 10000, this.getClass().getClassLoader(), "test:type=ThreadPool");
        WorkerPersistence workerPersistence = new VMWorkerPersistence();
        threadPooledTimer = new ThreadPooledTimer(executableWorkFactory, workerPersistence, threadPool, this.transactionManager);
        threadPooledTimer.doStart();

        ejbContainer = new MockEjbContainer(this.transactionManager);
        ejbDeployment = new MockEjbDeployment();
        timerService = new BasicTimerServiceImpl(ejbDeployment, ejbContainer, threadPooledTimer, kernelName, timerSourceName.getCanonicalName());
    }

    protected void tearDown() throws Exception {
        threadPooledTimer.doStop();
        threadPool.doStop();
        timerService = null;
        super.tearDown();
    }

    public void testSchedule1() throws Exception {
        Object id = new Integer(1);
        timerService.createTimer(id, 200L, userKey);
        Thread.sleep(200L + SLOP);
        assertEquals(1, ejbContainer.getCount());
        assertSame(id, ejbContainer.getId());
    }

    public void testSchedule2() throws Exception {
        timerService.createTimer(id, new Date(System.currentTimeMillis() + 20L), userKey);
        Thread.sleep(SLOP);
        assertEquals(1, ejbContainer.getCount());
    }

    public void testSchedule3() throws Exception {
        timerService.createTimer(id, 200L, DELAY, userKey);
        Thread.sleep(200L + SLOP + DELAY);
        assertEquals(2, ejbContainer.getCount());
    }

    public void testSchedule4() throws Exception {
        timerService.createTimer(id, new Date(System.currentTimeMillis()), DELAY, userKey);
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, ejbContainer.getCount());
    }

    public void testPersistence() throws Exception {
        Timer timer = timerService.createTimer(id, new Date(System.currentTimeMillis()+ DELAY), DELAY, userKey);
        Collection timers = timerService.getTimers(id);
        assertEquals(1, timers.size());
        assertSame(timer, timers.iterator().next());
        Thread.sleep(SLOP + DELAY);
        assertEquals(1, ejbContainer.getCount());

        threadPooledTimer.doStop();
        threadPooledTimer.doStart();
        timerService = new BasicTimerServiceImpl(ejbDeployment, ejbContainer, threadPooledTimer, kernelName, timerSourceName.getCanonicalName());
//        timerService = new NewBasicTimerServiceImpl(new InvocationFactory(), interceptor, threadPooledTimer, key, kernelName, timerSourceName, transactionManager, classLoader);
        timerService.doStart();

        Collection timers2 = timerService.getTimers(id);
        assertEquals(1, timers2.size());
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, ejbContainer.getCount());
    }

    public void testCancel() throws Exception {
        Timer timer = timerService.createTimer(id, 0L, DELAY, userKey);
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, ejbContainer.getCount());
        TimerState.setTimerState(true);
        timer.cancel();
        Thread.sleep(SLOP + DELAY);
        assertEquals(2, ejbContainer.getCount());
        assertEquals(0, timerService.getTimers(id).size());
        try {
            timer.cancel();
            fail("cancel should throw a NoSuchObjectLocalException");
        } catch (NoSuchObjectLocalException e) {
            //success
        }
    }


    private static class MockEjbContainer implements EjbContainer {
        private final TransactionManager transactionManager;
        private final AtomicInteger counter = new AtomicInteger(0);
        private Object id;

        public MockEjbContainer(TransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        public int getCount() {
            return counter.get();
        }

        public Object getId() {
            return id;
        }


        public void timeout(ExtendedEjbDeployment deployment, Object id, Timer timer, int ejbTimeoutIndex) {
            this.id = id;
            counter.incrementAndGet();
        }

        public TransactionManager getTransactionManager() {
            return transactionManager;
        }

        public UserTransaction getUserTransaction() {
            return null;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            return null;
        }

        public PersistentTimer getTransactedTimer() {
            return null;
        }

        public PersistentTimer getNontransactedTimer() {
            return null;
        }

    }

    private static class MockEjbDeployment implements ExtendedEjbDeployment {
        public InvocationResult invoke(Invocation invocation) throws Throwable {
            return null;
        }

        public BasicTimerServiceImpl getTimerService() {
            return null;
        }

        public String getContainerId() {
            return null;
        }

        public String getEjbName() {
            return null;
        }

        public int getMethodIndex(Method method) {
            return -1;
        }

        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }

        public EjbDeployment getUnmanagedReference() {
            return null;
        }

        public InterfaceMethodSignature[] getSignatures() {
            return new InterfaceMethodSignature[] { new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false)};
        }

        public boolean isSecurityEnabled() {
            return false;
        }

        public Subject getDefaultSubject() {
            return null;
        }

        public Subject getRunAsSubject() {
            return null;
        }

        public Context getComponentContext() {
            return null;
        }

        public void logSystemException(Throwable t) {
            System.out.println(t);
        }

        public VirtualOperation getVirtualOperation(int methodIndex) {
            return null;
        }

        public String getPolicyContextId() {
            return null;
        }

        public PermissionManager getPermissionManager() {
            return null;
        }

        public TransactionPolicyManager getTransactionPolicyManager() {
            return null;
        }

        public Class getBeanClass() {
            return null;
        }

        public Timer getTimerById(Long id) {
            return null;
        }

        public Set getUnshareableResources() {
            return null;
        }

        public Set getApplicationManagedSecurityResources() {
            return null;
        }
    }
}
