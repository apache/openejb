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
package org.apache.openejb.deployment.mdb.mockra;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

/**
 * @version $Revision$ $Date$
 */
public class MockWorkManager implements WorkManager {

    /**
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work)
     */
    public void doWork(final Work work) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
    }

    /**
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public void doWork(final Work work, long arg1, ExecutionContext arg2, WorkListener arg3) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
    }

    /**
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work)
     */
    public long startWork(final Work work) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
        return 0;
    }

    /**
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public long startWork(final Work work, long arg1, ExecutionContext arg2, WorkListener arg3) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
        return 0;
    }

    /**
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work)
     */
    public void scheduleWork(final Work work) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
    }

    /**
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public void scheduleWork(final Work work, long arg1, ExecutionContext arg2, WorkListener arg3) throws WorkException {
        new Thread(new Runnable() {
            public void run() {
                work.run();                
            }}).start();
    }

}
