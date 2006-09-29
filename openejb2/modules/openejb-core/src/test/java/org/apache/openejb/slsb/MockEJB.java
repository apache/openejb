/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.slsb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class MockEJB implements SessionBean, TimedObject {

    private int timeoutCount = 0;

    private SessionContext sessionContext;

    public int intMethod(int i) {
        return i + 1;
    }

    public Integer integerMethod(Integer i) {
        return i;
    }

    public void appException() throws AppException {
        throw new AppException("App Message");
    }

    public void sysException() {
        throw new IllegalArgumentException("Sys Message");
    }

    public void startTimer() {
        TimerService timerService = sessionContext.getTimerService();
        timerService.createTimer(100L, null);
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void ejbTimeout(Timer timer) {
        timeoutCount++;
    }
}
