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
package org.apache.openejb.test.simple.slsb;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.ejb.Timer;
import javax.ejb.TimedObject;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class SimpleStatelessSessionEJB implements SessionBean, TimedObject {
    private SessionContext sessionContext;
    private int timeoutCount = 0;


    public SimpleStatelessSessionEJB() {
    }

    public String echo(String message) {
        return message;
    }


    public void startTimer() {
        TimerService timerService = sessionContext.getTimerService();
        timerService.createTimer(100L, null);
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void ejbTimeout(Timer timer) {
        timeoutCount++;
    }

    public void ejbCreate() throws javax.ejb.CreateException {
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbRemove() throws EJBException {
    }

    public void setSessionContext(SessionContext ctx) throws EJBException {
        this.sessionContext = ctx;
    }
}
