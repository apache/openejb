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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.UserTaskFactory;
import org.apache.geronimo.timer.WorkInfo;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.kernel.proxy.DeadProxyException;
import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class BasicTimerServiceImpl implements BasicTimerService {

    private static final Log log = LogFactory.getLog(EJBInvokeTask.class);

    private final EJBTimeoutInvocationFactory invocationFactory;
    private final Interceptor stack;
    private final PersistentTimer persistentTimer;
    private final String key;
    private final UserTaskFactory userTaskFactory;
    private final String kernelName;
    private final ObjectName timerSourceName;
    private final TransactionContextManager transactionContextManager;
//    private final Map idToTimersMap = new HashMap();

    public BasicTimerServiceImpl(EJBTimeoutInvocationFactory invocationFactory, Interceptor stack, ThreadPooledTimer timer, String key, String kernelName, ObjectName timerSourceName, TransactionContextManager transactionContextManager, ClassLoader classLoader) {
        this.invocationFactory = invocationFactory;
        this.stack = stack;
        this.persistentTimer = timer;
        this.key = key;
        this.kernelName = kernelName;
        this.timerSourceName = timerSourceName;
        this.transactionContextManager = transactionContextManager;
        userTaskFactory = new EJBInvokeTaskFactory(this, classLoader, transactionContextManager);
    }

    public void doStart() throws PersistenceException {
        //reconstruct saved timers.
        Collection workInfos = persistentTimer.playback(key, userTaskFactory);
        for (Iterator iterator = workInfos.iterator(); iterator.hasNext();) {
            WorkInfo workInfo = (WorkInfo) iterator.next();
            newTimer(workInfo);
        }
    }

    public void doStop() throws PersistenceException {
        Collection ids = persistentTimer.getIdsByKey(key, null);
        persistentTimer.cancelTimerTasks(ids);
    }


    public Timer createTimer(Object id, Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.scheduleAtFixedRate(key, userTaskFactory, id, info, initialExpiration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.schedule(key, userTaskFactory, id, info, expiration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.scheduleAtFixedRate(key, userTaskFactory, id, info, initialDuration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.schedule(userTaskFactory, key, id, info, duration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Collection getTimers(Object id) throws IllegalStateException, EJBException {
//        synchronized(idToTimersMap) {
//            Set timers = (Set) idToTimersMap.get(id);
//            return timers == null? Collections.EMPTY_SET: Collections.unmodifiableSet(timers);
//        }
        Collection ids = null;
        try {
            ids = persistentTimer.getIdsByKey(key, id);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        }
        Collection timers = new ArrayList();
        for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
            Long timerId = (Long) iterator.next();
            try {
                TimerImpl timer = getTimerById(timerId);
                timers.add(timer);
            } catch (NoSuchObjectLocalException e) {
                System.out.println("could not find timer for timerId " + timerId + "from key " + key + " and " + id);
            }
        }
        return timers;
    }

    //TODO HACK SEE GERONIMO-623
    private boolean notified = false;
    public TimerImpl getTimerById(Long id) {
        WorkInfo workInfo = null;
        try {
            workInfo = persistentTimer.getWorkInfo(id);
        } catch (DeadProxyException e) {
            //TODO HACK SEE GERONIMO-623
            notified = true;
            if (notified) {
                return null;
            }
            throw new RuntimeException("Dead proxy for ejb " + key);
        }
        if (workInfo != null) {
            TimerImpl timer = (TimerImpl) workInfo.getClientHandle();
            return timer;
        } else {
            throw new NoSuchObjectLocalException("No timer");
        }
    }

    void registerCancelSynchronization(Synchronization cancelSynchronization) throws RollbackException, SystemException {
        TransactionContext transactionContext = transactionContextManager.getContext();
        if (transactionContext != null && transactionContext.isInheritable() && transactionContext.isActive()) {
            transactionContext.registerSynchronization(cancelSynchronization);
            return;
        }
        cancelSynchronization.afterCompletion(Status.STATUS_COMMITTED);
    }

    private Timer newTimer(WorkInfo workInfo) {
//        System.out.println("Created timer with timerId " + workInfo.getId() + " for key " + key + " and id " + workInfo.getUserId());
        Timer timer = new TimerImpl(workInfo, this, kernelName, timerSourceName);
        workInfo.setClientHandle(timer);
//        synchronized (idToTimersMap) {
//            Set timers = (Set) idToTimersMap.get(workInfo.getUserId());
//            if (timers == null) {
//                timers = new HashSet();
//                idToTimersMap.put(workInfo.getUserId(), timers);
//            }
//            timers.add(timer);
//        }
        return timer;
    }

    private Interceptor getStack() {
        return stack;
    }

    private static class EJBInvokeTask implements Runnable {
        private final BasicTimerServiceImpl timerService;
        private final long timerId;
        private final ClassLoader classLoader;
        private final TransactionContextManager transactionContextManager;

        public EJBInvokeTask(BasicTimerServiceImpl timerService, long id, ClassLoader classLoader, TransactionContextManager transactionContextManager) {
            this.timerService = timerService;
            this.timerId = id;
            this.classLoader = classLoader;
            this.transactionContextManager = transactionContextManager;
        }

        public void run() {
            TimerImpl timerImpl = timerService.getTimerById(new Long(timerId));
            //TODO HACK SEE GERONIMO-623
            if (timerImpl == null) {
                return;
            }
            EJBInvocation invocation = timerService.invocationFactory.getEJBTimeoutInvocation(timerImpl.getUserId(), timerImpl);

            // set the transaction context into the invocation object
            TransactionContext transactionContext = transactionContextManager.getContext();
            if (transactionContext == null) {
                throw new IllegalStateException("Transaction context has not been set");
            }
            invocation.setTransactionContext(transactionContext);

            Thread currentThread = Thread.currentThread();
            ClassLoader oldClassLoader = currentThread.getContextClassLoader();
            try {
                currentThread.setContextClassLoader(classLoader);
                timerService.getStack().invoke(invocation);
            } catch (Throwable throwable) {
                log.warn("Timer invocation failed", throwable);
            } finally {
                currentThread.setContextClassLoader(oldClassLoader);
            }
        }

    }

    private static class EJBInvokeTaskFactory implements UserTaskFactory {
        private final BasicTimerServiceImpl timerService;
        private final ClassLoader classLoader;
        private final TransactionContextManager transactionContextManager;

        public EJBInvokeTaskFactory(BasicTimerServiceImpl timerService, ClassLoader classLoader, TransactionContextManager transactionContextManager) {
            this.timerService = timerService;
            this.classLoader = classLoader;
            this.transactionContextManager = transactionContextManager;
        }

        public Runnable newTask(long id) {
            return new EJBInvokeTask(timerService, id, classLoader, transactionContextManager);
        }

    }

}
