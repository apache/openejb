/*
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

package org.apache.openejb.core.timer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;

public interface TimerStore {

    TimerData getTimer(String deploymentId, long timerId);

    Collection<TimerData> getTimers(String deploymentId);

    Collection<TimerData> loadTimers(EjbTimerServiceImpl timerService, String deploymentId) throws TimerStoreException;

    void addTimerData(TimerData timerData) throws TimerStoreException;

    TimerData createSingleActionTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, Date expiration, TimerConfig timerConfig)
            throws TimerStoreException;

    TimerData createIntervalTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, Date initialExpiration, long intervalDuration, TimerConfig timerConfig)
            throws TimerStoreException;

    TimerData createCalendarTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, ScheduleExpression schedule, TimerConfig timerConfig)
            throws TimerStoreException;

    void removeTimer(long timerId);

    void updateIntervalTimer(TimerData timerData);
}
