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

import java.lang.reflect.Method;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Revision$ $Date$
 */
public class MockEndpointWorker implements Work {

    private static final Method ON_MESSAGE_METHOD;

    static {
        try {
            ON_MESSAGE_METHOD = MessageListener.class.getMethod("onMessage", new Class[]{Message.class});
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MockResourceAdapter adapter;
    private MockEndpointActivationKey endpointActivationKey;
    private AtomicBoolean started = new AtomicBoolean(false);
    Latch stopLatch = new Latch();
    boolean stopping = false;
    private MessageEndpointFactory messageEndpointFactory;
    private MockActivationSpec activationSpec;

    /**
     * @param key
     */
    public MockEndpointWorker(MockResourceAdapter adapter, MockEndpointActivationKey key) {
        this.endpointActivationKey = key;
        this.adapter = adapter;
    }

    /**
     *
     */
    public void start() throws WorkException {

        messageEndpointFactory = endpointActivationKey.getMessageEndpointFactory();
        activationSpec = endpointActivationKey.getActivationSpec();
        adapter.getBootstrapContext().getWorkManager().scheduleWork(this);
    }

    /**
     *
     */
    public void stop() throws InterruptedException {
        release();
        if (started.get()) {
            stopLatch.acquire();
        }
    }

    /**
     * @see javax.resource.spi.work.Work#release()
     */
    public void release() {
        stopping = true;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        started.set(true);
        try {

            MessageEndpoint endpoint = null;
            boolean transacted;
            transacted = messageEndpointFactory.isDeliveryTransacted(ON_MESSAGE_METHOD);
            endpoint = messageEndpointFactory.createEndpoint(null);

            for (int i = 0; !stopping; i++) {
                // Delay message delivery a little.
                Thread.sleep(1000);

                MockTextMessage message = new MockTextMessage("Message:" + i);
                endpoint.beforeDelivery(ON_MESSAGE_METHOD);
                ((MessageListener) endpoint).onMessage(message);
                endpoint.afterDelivery();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            stopLatch.release();
            stopping = false;
        }
    }

}
