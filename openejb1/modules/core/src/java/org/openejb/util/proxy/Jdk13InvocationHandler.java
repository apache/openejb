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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util.proxy;

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
 * @version $Revision$
 */
public class Jdk13InvocationHandler implements java.lang.reflect.InvocationHandler, Serializable {
    private org.openejb.util.proxy.InvocationHandler delegate;

    public Jdk13InvocationHandler() {
    }

    public Jdk13InvocationHandler(org.openejb.util.proxy.InvocationHandler delegate) {
        setInvocationHandler(delegate);
    }

    public org.openejb.util.proxy.InvocationHandler getInvocationHandler() {
        return delegate;
    }

    public org.openejb.util.proxy.InvocationHandler setInvocationHandler(org.openejb.util.proxy.InvocationHandler handler) {
        org.openejb.util.proxy.InvocationHandler old = delegate;
        delegate = handler;
        return old;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(delegate != null) {
            if(args == null) {
                args = new Object[0];
                // A bug in the proxy call?
            }
            return delegate.invoke(proxy, method, args);
        } else {
            throw new NullPointerException("No invocation handler for proxy "+proxy);
        }
    }
}

