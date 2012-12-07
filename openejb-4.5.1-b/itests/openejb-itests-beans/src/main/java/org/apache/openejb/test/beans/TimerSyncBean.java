/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.beans;

import javax.ejb.Stateless;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Stateless
public class TimerSyncBean implements TimerSync {
    private static Map<String, CountDownLatch> latches = new TreeMap<String, CountDownLatch>();

    public boolean waitFor(String name) {
        CountDownLatch latch = getLatch(name);
        try {
            return latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void countDown(String name) {
        CountDownLatch latch = getLatch(name);
        latch.countDown();
    }

    private synchronized CountDownLatch getLatch(String name) {
        CountDownLatch latch = latches.get(name);
        if (latch == null) {
            latch = new CountDownLatch(1);
            latches.put(name, latch);
        }
        return latch;
    }

}
