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
 * $Id$
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
