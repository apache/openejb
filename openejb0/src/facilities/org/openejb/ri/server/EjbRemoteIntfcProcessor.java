
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


package org.openejb.ri.server;

import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.util.Messages;
import org.openejb.util.proxy.ProxyManager;


/**
 * Represents the EJB Server's responsibility in handling the business methods
 * methods that are declared in the bean's remote interface.
 * 
 * @see javax.ejb.EJBHome
 */
public class EjbRemoteIntfcProcessor {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    /**
     * Internally processes the business methods from the bean's remote 
     * interface.
     *
     * Business methods that return EJBHome or EJBObject references to local
     * beans (beans in the same container system) must have the return value
     * converted to a ProxyInfo object, so that the server can provide the client
     * with a proper remote reference.  Non-local remote references are assumed to be serializable and valid
     * return types for the clients.
     * <p>
     * If a ProxyInfo object is returned in must be converted into a Ri Proxy
     * before its returned to the client.
     *
     * See Section 2.2.1.2.5 Remote References of the OpenEJB specification
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    public static Object processMethod(MethodInvocation mi, String securityToken, Server server) throws OpenEJBException {
        
        Object returnValue = server.invokeMethod(mi);
        
        if(returnValue instanceof ProxyInfo){
            ProxyInfo prxInfo = (ProxyInfo)returnValue;
            
            EjbProxyHandler handler = new EjbProxyHandler(server.port, server.ip, prxInfo.getPrimaryKey() ,prxInfo.getDeploymentInfo().getDeploymentID(), securityToken);
            Object proxy = null;
            try{
            proxy = ProxyManager.newProxyInstance(prxInfo.getInterface(), handler);
            }catch(IllegalAccessException iae){
                throw new RuntimeException( _messages.format( "ejbRemoteIntfcProcessor.couldNotCreateIVMProxy", prxInfo.getInterface() ) );
            }
            return proxy;
        }
        else
            return returnValue;
        
    }
}
