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

import java.util.HashMap;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * @version $Revision$ $Date$
 */
public class MockResourceAdapter implements ResourceAdapter {

    private BootstrapContext bootstrapContext;
    private HashMap endpointWorkers = new HashMap();

    /**
     * @return Returns the bootstrapContext.
     */
    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#start(javax.resource.spi.BootstrapContext)
     */
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        this.bootstrapContext = bootstrapContext;
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#stop()
     */
    public void stop() {
        this.bootstrapContext = null;
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
     */
    synchronized public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
        throws ResourceException {
        //spec section 5.3.3
        if (activationSpec.getResourceAdapter() != this) {
            throw new ResourceException("Activation spec not initialized with this ResourceAdapter instance");
        }

        if (activationSpec.getClass().equals(MockActivationSpec.class)) {

            MockEndpointActivationKey key = new MockEndpointActivationKey(messageEndpointFactory, (MockActivationSpec)activationSpec);
            // This is weird.. the same endpoint activated twice.. must be a container error.
            if (endpointWorkers.containsKey(key)) {
                throw new IllegalStateException("Endpoint previously activated");
            }
            MockEndpointWorker worker = new MockEndpointWorker(this, key);
            endpointWorkers.put(key, worker);
            worker.start();

        } else {
            throw new NotSupportedException("That type of ActicationSpec not supported: "+activationSpec.getClass());
        }
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointDeactivation(javax.resource.spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
     */
    synchronized public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec){
        if (activationSpec.getClass().equals(MockActivationSpec.class)) {
            MockEndpointActivationKey key = new MockEndpointActivationKey(messageEndpointFactory, (MockActivationSpec)activationSpec);
            MockEndpointWorker worker = (MockEndpointWorker) endpointWorkers.get(key);
            if (worker == null) {
                // This is weird.. that endpoint was not activated..  oh well.. this method
                // does not throw exceptions so just return.
                return;
            }
            try {
                worker.stop();
            } catch (InterruptedException e) {
                // We interrupted.. we won't throw an exception but will stop waiting for the worker
                // to stop..  we tried our best.  Keep trying to interrupt the thread.
                Thread.currentThread().interrupt();
            }

        }
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.ActivationSpec[])
     */
    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        // TODO: this is for recovery..
        return new XAResource[] {
        };
    }
}
