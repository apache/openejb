/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.mdb.mockra;

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
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
     */
    synchronized public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
        throws ResourceException {

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
    synchronized public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
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
