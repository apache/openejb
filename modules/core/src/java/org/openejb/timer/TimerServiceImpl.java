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
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.UserTaskFactory;
import org.apache.geronimo.timer.WorkInfo;
import org.apache.geronimo.timer.PersistentTimer;
import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerServiceImpl implements TimerService {

    private static final Log log = LogFactory.getLog(EJBInvokeTask.class);

    private final EJBTimeoutInvocationFactory invocationFactory;
    private final Interceptor stack;
    private final PersistentTimer persistentTimer;
    private final String key;
    private final UserTaskFactory userTaskFactory;
    private final String kernelName;
    private final ObjectName timerSourceName;
    private final TransactionContextManager transactionContextManager;

    public TimerServiceImpl(EJBTimeoutInvocationFactory invocationFactory, Interceptor stack, ThreadPooledTimer timer, String key, String kernelName, ObjectName timerSourceName, TransactionContextManager transactionContextManager) throws PersistenceException {
        this.invocationFactory = invocationFactory;
        this.stack = stack;
        this.persistentTimer = timer;
        this.key = key;
        this.kernelName = kernelName;
        this.timerSourceName = timerSourceName;
        this.transactionContextManager = transactionContextManager;
        userTaskFactory = new EJBInvokeTaskFactory(this);
    }

    public void doStart() throws PersistenceException {
        //reconstruct saved timers.
        Collection workInfos = persistentTimer.playback(key, userTaskFactory);
        for (Iterator iterator = workInfos.iterator(); iterator.hasNext();) {
            WorkInfo workInfo = (WorkInfo) iterator.next();
            newTimer(workInfo);
        }
    }

    public void doStop() {
        //TODO remove the ejb timers from the Timer but not the persistence service.
    }
    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.scheduleAtFixedRate(key, userTaskFactory, info, initialExpiration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.schedule(key, userTaskFactory, info, expiration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.scheduleAtFixedRate(key, userTaskFactory, info, initialDuration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = persistentTimer.schedule(userTaskFactory, key, info, duration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Collection getTimers() throws IllegalStateException, EJBException {
        Collection ids = null;
        try {
            ids = persistentTimer.getIdsByKey(key);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        }
        Collection timers = new ArrayList();
        for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
            Long id = (Long) iterator.next();
            TimerImpl timer = getTimerById(id);
            timers.add(timer);
        }
        return timers;
    }

    private TimerImpl getTimerById(Long id) {
        WorkInfo workInfo = persistentTimer.getWorkInfo(id);
        TimerImpl timer = (TimerImpl) workInfo.getClientHandle();
        return timer;
    }

    void registerCancelSynchronization(Synchronization cancelSynchronization) throws RollbackException, SystemException {
        TransactionContext transactionContext = transactionContextManager.getContext();
        if (transactionContext != null && transactionContext.isActive()) {
            transactionContext.getTransaction().registerSynchronization(cancelSynchronization);
        } else {
            cancelSynchronization.afterCompletion(Status.STATUS_COMMITTED);
        }
    }

    private Timer newTimer(WorkInfo workInfo) {
        Timer timer = new TimerImpl(workInfo, this, kernelName, timerSourceName);
        workInfo.setClientHandle(timer);
        return timer;
    }

    private Interceptor getStack() {
        return stack;
    }

    private static class EJBInvokeTask implements Runnable {

        private final TimerServiceImpl timerService;
        private final long id;
        private EJBInvocation invocation;

        public EJBInvokeTask(TimerServiceImpl timerService, long id) {
            this.timerService = timerService;
            this.id = id;
        }

        public void run() {
            if (invocation == null) {
                TimerImpl timerImpl = timerService.getTimerById(new Long(id));
                invocation = timerService.invocationFactory.getEJBTimeoutInvocation(timerImpl);
            }

            try {
                timerService.getStack().invoke(invocation);
            } catch (Throwable throwable) {
                log.info(throwable);
            }
        }

    }

    private static class EJBInvokeTaskFactory implements UserTaskFactory {

        private final TimerServiceImpl timerService;

        public EJBInvokeTaskFactory(TimerServiceImpl timerService) {
            this.timerService = timerService;
        }
        public Runnable newTask(long id) {
            return new EJBInvokeTask(timerService, id);
        }

    }

}
