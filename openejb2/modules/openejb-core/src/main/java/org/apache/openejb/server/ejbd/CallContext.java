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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.EjbDeployment;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.util.FastThreadLocal;

/**
 * TODO: Add comment
 *
 */
public class CallContext {

    /**
     * Hashtable of threads executing in this server
     */
    protected static FastThreadLocal threads = new FastThreadLocal();

    /**
     * The container of the bean executed
     */
    protected EjbDeployment container;

    /**
     * The EJBRequest object from the client
     */
    protected EJBRequest request;

    /**
     * Constructs a new CallContext
     */
    public CallContext(){
    }

    /**
     * Invalidates the data in this CallContext
     */
    public void reset() {
        container = null;
        request        = null;
    }
    
    /**
     * Returns the EJBContainer assigned to this CallContext
     */
    public EjbDeployment getContainer() {
        return container;
    }
    
    /**
     * Sets the EJBContainer assigned to this CallContext
     */
    public void setContainer(EjbDeployment container) {
        this.container = container;
    }
    
    /**
     * Returns the EJBRequest this thread is satisfying.
     */
    public EJBRequest getEJBRequest(){
        return request;
    }
    
    /**
     * Sets the EJBRequest this thread is satisfying.
     */
    public void setEJBRequest(EJBRequest request){
        this.request = request;
    }
    
    /**
     * Sets the CallContext assigned to the current thread with the CallContext
     * instance passed in
     */
    public static void setCallContext(CallContext ctx) {
        if ( ctx == null ) {
            ctx = (CallContext)threads.get();
            if ( ctx != null ) ctx.reset();
        } else {
            threads.set( ctx );
        }
    }
    
    /**
     * Gets the CallContext assigned to the current thread
     */
    public static CallContext getCallContext( ) {
        CallContext ctx = (CallContext)threads.get();
        if ( ctx == null ) {
            ctx = new CallContext();
            threads.set( ctx );
        }
        return ctx;
    }
}


