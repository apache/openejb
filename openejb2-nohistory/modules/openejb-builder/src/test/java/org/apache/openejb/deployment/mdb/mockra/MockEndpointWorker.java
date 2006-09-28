/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: MockEndpointWorker.java 446446 2006-09-11 20:19:03Z kevan $
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
