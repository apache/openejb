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
package org.apache.openejb.deployment.mdb;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jms.Message;
import javax.jms.MessageListener;

import edu.emory.mathcs.backport.java.util.concurrent.Semaphore;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class MockEJB implements MessageDrivenBean, MessageListener, TimedObject {

    public static final Semaphore messageCounter = new Semaphore(0);
    private boolean ejbCreateCalled;
    private boolean ejbRemoveCalled;
    private MessageDrivenContext messageDrivenContext;
    public static Message lastMessage;
    public static boolean timerFired;

    public void ejbCreate() throws EJBException {
        ejbCreateCalled = true;
    }

    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException {
        ejbRemoveCalled = true;
    }

    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) throws EJBException {
        this.messageDrivenContext = messageDrivenContext;
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message) {
        lastMessage = message;
        messageCounter.release();
        TimerService timerService = messageDrivenContext.getTimerService();
        timerService.createTimer(100L, null);
    }

    public void ejbTimeout(Timer timer) {
        timerFired = true;
    }

}
