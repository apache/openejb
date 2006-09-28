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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * $Id: CallContext.java 445872 2006-02-01 11:50:15Z dain $
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


