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
package org.openejb.core.ivm;

import java.io.ObjectStreamException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.openejb.util.proxy.ProxyManager;

/**
 * IntraVM implementation of the interface javax.ejb.Handle
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class IntraVmHandle implements java.io.Serializable, javax.ejb.HomeHandle, javax.ejb.Handle {
    /**
     * The Proxy subclass that represents the bean 
     * deployment's EJBHome or EJBObject.
     * 
     * @see org.openejb.util.proxy.Proxy
     */
    protected Object theProxy;

    /**
     * Constructs an IntraVmHandle that has no refernce to
     * an EJBHome or EJBObject.
     */
    public IntraVmHandle() {
        this(null);    
    }

    /**
     * Constructs an IntraVmHandle that has a refernce to
     * the specified EJBHome or EJBObject stub/proxy.
     * 
     * @param proxy
     */
    public IntraVmHandle(Object proxy) {
        this.theProxy = proxy;
    }

    /**
     * Sets the EJBHome or EJBObject stub/proxy that this 
     * handle is a reference to.
     * 
     * @param prxy   The proxy object that this handle will reference.
     */
    protected void setProxy(Object prxy) {
        theProxy = prxy;
    }
    
    /**
     * Returns the stub/proxy referenced by this handle as an EJBHome.
     * 
     * @return the proxy object this handle is a reference to.
     */
    public EJBHome getEJBHome( ) {
        return(EJBHome)theProxy;
    }
    
    /**
     * Returns the stub/proxy referenced by this handle as an EJBObject.
     * 
     * @return the proxy object this handle is a reference to.
     */
    public EJBObject getEJBObject( ) {
        return(EJBObject)theProxy;
    }       

    public Object getPrimaryKey() {
        return ((BaseEjbProxyHandler) org.openejb.util.proxy.ProxyManager.getInvocationHandler(theProxy)).primaryKey;
    }
    
    /**
     * If the handle is being  copied between bean instances in a RPC
     * call we use the IntraVmArtifact
     * 
     * If the handle is referenced by a stateful bean that is being
     * passivated by the container, we allow this object to be serialized.
     * 
     * If the handle is serialized outside the core container system, we
     * allow the application server to handle it.
     * 
     * @return Object 
     * @exception ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException{
        /*
         * If the handle is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact 
         */
        if(IntraVmCopyMonitor.isIntraVmCopyOperation()){
            return new IntraVmArtifact(this);
        /* 
         * If the handle is referenced by a stateful bean that is being 
         * passivated by the container, we allow this object to be serialized. 
         */
        }else if(IntraVmCopyMonitor.isStatefulPassivationOperation()){
            return this;
        /* 
         * If the handle is serialized outside the core container system, we
         * allow the application server to handle it. 
         */
        }else{
            BaseEjbProxyHandler handler = (BaseEjbProxyHandler)ProxyManager.getInvocationHandler(theProxy);
            if(theProxy instanceof javax.ejb.EJBObject) {
                return org.openejb.OpenEJB.getApplicationServer().getHandle(handler.getProxyInfo());
            } else if(theProxy instanceof javax.ejb.EJBHome) {
                return org.openejb.OpenEJB.getApplicationServer().getHomeHandle(handler.getProxyInfo());
            }else {
                throw new RuntimeException("Invalid proxy type. Handles are only supported by EJBObject types in EJB 1.1");
            }
        }
    }

}