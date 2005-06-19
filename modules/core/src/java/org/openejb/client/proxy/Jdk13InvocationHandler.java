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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.client.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Implementation of JDK 1.3 InvocationHandler for JDK 1.3 Proxies.
 * This only compiles on JDK 1.3 or better.  This is the interface
 * between the OpenEJB InvocationHandler and the JDK 1.3 proxy.  It
 * allows the OpenEJB InvocationHandler to be null or changed after
 * proxy instantiation, which is not normally allowed for JDK 1.3
 * proxies.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class Jdk13InvocationHandler implements java.lang.reflect.InvocationHandler, Serializable {
    
    /**
     * The InvocationHandler that will receive all the calls on the proxy
     */
    private InvocationHandler delegate;

    /**
     * Constructs a new JDK 1.3 compatable InvocationHandler that delegates
     * all invocations to an OpenEJB invocation handler.
     */
    public Jdk13InvocationHandler() {
    }

    /**
     * 
     * Constructs a new JDK 1.3 compatable InvocationHandler that delegates
     * all invocations to an OpenEJB invocation handler.
     * 
     * @param delegate
     */
    public Jdk13InvocationHandler(InvocationHandler delegate) {
        setInvocationHandler(delegate);
    }

    /**
     * Returns the InvocationHandler that will receive all the calls on the proxy
     * 
     * @return InvocationHandler
     */
    public InvocationHandler getInvocationHandler() {
        return delegate;
    }

    /**
     * Sets the InvocationHandler that will receive all the calls on the proxy
     * 
     * @param handler
     * @return InvocationHandler
     */
    public InvocationHandler setInvocationHandler(InvocationHandler handler) {
        InvocationHandler old = delegate;
        delegate = handler;
        return old;
    }

    /**
     * Invoked by the proxy instance when one of its methods have been called.
     * The invocation is delegated to the OpenEJB invocation handler.
     * 
     * @param proxy
     * @param method
     * @param args
     * @return Object
     * @exception Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        if ( delegate == null ) throw new NullPointerException("No invocation handler for proxy "+proxy);
        
        if(args == null) {
            args = new Object[0];
            // A bug in the proxy call?
        }
        
        return delegate.invoke(proxy, method, args);
    }
}

