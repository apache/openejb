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
package org.apache.openejb.deployment.entity;

import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class MockBMPEJB implements EntityBean, TimedObject {
    private int field;

    private static int timeoutCount = 0;
    private String value;

    public Object ejbCreate(Integer id, String value) throws CreateException {
        return id;
    }

    public void ejbPostCreate(Integer id, String value) {
    }

    public Object ejbFindByPrimaryKey(Integer pk) throws FinderException {
        return pk;
    }

    public int ejbHomeIntMethod(int i) {
        return i + 1;
    }

    public String ejbHomeSingleSelect(Integer i) throws FinderException {
        return null;
    }

    public Collection ejbHomeMultiSelect(Integer i) throws FinderException {
        return null;
    }

    public Collection ejbHomeMultiObject(Integer i) throws FinderException {
        return null;
    }

    public int intMethod(int i) {
        return 1 + i + ((Integer) context.getPrimaryKey()).intValue();
    }

    public int getIntField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getString() {
        return "Hello";
    }

    public void setString(String s) {
    }

    public void startTimer() {
        timeoutCount = 0;
        TimerService timerService = context.getTimerService();
        timerService.createTimer(100L, null);
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    private EntityContext context;

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
        field = 0;
    }

    public void ejbPassivate() {
        field = 0;
    }

    public void ejbLoad() {
        field = ((Integer) context.getPrimaryKey()).intValue();
    }

    public void ejbStore() {
    }

    public void ejbRemove() throws RemoveException {
    }

    public void ejbTimeout(Timer timer) {
        timeoutCount++;
    }
}
