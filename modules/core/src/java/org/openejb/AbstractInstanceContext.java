/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.apache.geronimo.core.service.Interceptor;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.TimerServiceImpl;
import org.openejb.timer.BasicTimerService;
import org.openejb.timer.UnavailableTimerService;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Revision$ $Date$
 *
 * */
public abstract class AbstractInstanceContext implements EJBInstanceContext {

    private final Map connectionManagerMap = new HashMap();
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    //this not being final sucks, but the CMP instance is not available until after the superclass constructor executes.
    protected EnterpriseBean instance;

    private final EJBProxyFactory proxyFactory;
    //initialized in subclass, can't be final :-((
    protected EJBInvocation setContextInvocation;
    protected EJBInvocation unsetContextInvocation;
    protected final Interceptor systemChain;
    private final BasicTimerService activeTimer;
    private final TimerService timerService;

    private BasicTimerService timerState = UnavailableTimerService.INSTANCE;

    public AbstractInstanceContext(Interceptor systemChain, Set unshareableResources, Set applicationManagedSecurityResources, EnterpriseBean instance, EJBProxyFactory proxyFactory, BasicTimerService basicTimerService) {
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.instance = instance;
        this.proxyFactory = proxyFactory;
        this.systemChain = systemChain;
        this.activeTimer = basicTimerService;
        this.timerService = basicTimerService == null? null: new TimerServiceImpl(this);
    }

    public Object getId() {
        return null;
    }

    public void setId(Object id) {
    }

    public Object getContainerId() {
        return null;
    }

    public void associate() throws Throwable {
    }

    public void flush() throws Throwable {
    }

    public void beforeCommit() throws Throwable {
    }

    public void afterCommit(boolean status) throws Throwable {
    }

    public Map getConnectionManagerMap() {
        return connectionManagerMap;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public EnterpriseBean getInstance() {
        return instance;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public void setContext() throws Throwable {
        systemChain.invoke(setContextInvocation);
    }

    public void unsetContext() throws Throwable {
        systemChain.invoke(unsetContextInvocation);
    }

    public TimerService getTimerService() {
        return timerService;
    }

    public BasicTimerService getBasicTimerService() {
        return timerState;
    }

    public void setTimerServiceAvailable(boolean available) {
        if (available) {
            timerState = activeTimer;
        } else {
            timerState = UnavailableTimerService.INSTANCE;
        }
    }

}
