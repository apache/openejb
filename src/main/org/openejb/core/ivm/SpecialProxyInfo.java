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

import org.openejb.util.proxy.ProxyManager;

/**
 * Business methods that return EJBHome or EJBObject references to local
 * beans (beans in the same container system) must have the return value
 * converted to a ProxyInfo object so that the server can provide the client
 * with a proper remote reference.  Local remote references -- called proxies --
 * are handled using the org.openejb.core.ivm.BaseEjbProxyHandler types, which
 * should not be returned to the client.  Non-local remote references are assumed
 * to be serializable and valid return types for the clients.
 * <P>
 * If the reference is a local remote reference -- a proxy -- this subtype of
 * ProxyInfo is returned. This class type is useful when the calling server is
 * the IntraVM server.  Instead of creating a new remote ref from the proxy the
 * IntraVM takes a short cut and reuses the original local remote reference -- 
 * they are thread safe with no synchronization.
 * <P>
 * See Section 2.2.1.2.5 Remote References of the OpenEJB specification.
 * <P>
 * 
 * @see org.openejb.ProxyInfo
 * 
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class SpecialProxyInfo extends org.openejb.ProxyInfo {
    
    protected Object proxy;
    
    public SpecialProxyInfo(Object proxy){
        super();
        
        this.proxy = proxy;
        
        BaseEjbProxyHandler handler = (BaseEjbProxyHandler)ProxyManager.getInvocationHandler( proxy );
        
        deploymentInfo = handler.deploymentInfo;
        primaryKey = handler.primaryKey;
        beanContainer = handler.container;
        
        if(handler instanceof EjbHomeProxyHandler)
            type = deploymentInfo.getHomeInterface();
        else
            type = deploymentInfo.getRemoteInterface();
        
    }
    
    public Object getProxy(){
        return proxy;
    }
}

