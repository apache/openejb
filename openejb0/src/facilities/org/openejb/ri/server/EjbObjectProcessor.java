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

import java.rmi.RemoteException;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJBException;
import org.openejb.util.Messages;
import org.openejb.util.proxy.ProxyManager;

/**
 * Represents the EJB Server's responsibility in handling methods that are
 * declared in the javax.ejb.EJBObject interface.
 * 
 * @see javax.ejb.EJBObject
 */
public class EjbObjectProcessor {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    /**
     * Internally processes the getHandle, getPrimaryKey, isIdentical, remove
     * and getEJBHome methods from the EJBObject proxy.
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    public static Object processMethod(MethodInvocation mi, String securityToken, Server server) throws OpenEJBException{

        try {
            server.checkAuthorization(mi);
        } catch ( org.openejb.ApplicationException ae ) {// checkAuthorization exception
            return ae.getRootCause();
        }

        if ( mi.getMethod().getName().equals("getHandle") )           return getHandle();
        else if ( mi.getMethod().getName().equals("getPrimaryKey") )  return getPrimaryKey(mi);
        else if ( mi.getMethod().getName().equals("isIdentical") )    return isIdentical(mi);
        else if ( mi.getMethod().getName().equals("remove") )         return remove(mi, server);
        else if ( mi.getMethod().getName().equals("getEJBHome") )     return getEJBHome(mi, securityToken, server);
        
        return null;

    }
    
    /**
     * Creates and returns a new EJB Server specific handle for locating server and
     * obtaining a reference to the EJBObject.
     * 
     * @return 
     */
    protected static Object getHandle(){
        return new RiBaseHandle();
    }
    
    /**
     * Returns the primary key of the EJBObject if the bean is a type of EntityBean.
     * Returns a RemoteException if the bean is any other type.
     * 
     * @param mi
     * @return 
     */
    protected static Object getPrimaryKey(MethodInvocation mi){
        byte type = mi.getDeploymentInfo().getComponentType();
        
        if ( type == DeploymentInfo.BMP_ENTITY || type == DeploymentInfo.CMP_ENTITY ) 
            return mi.getPrimaryKey();
        else 
            return new RemoteException( _messages.message( "ejbObjectProcessor.invalidOperation" ) );
    }

    /**
     * Checks to see if the EJBObjects are identical.
     * 
     * @param mi
     * @return true if the EJBObjects share the same primary key
     */
    protected static Object isIdentical(MethodInvocation mi){
        return new Boolean(mi.getArguments()[0].equals(mi.getPrimaryKey()));
    }
    
    /**
     * Invokes the remove method on the container and passes back a null.
     * 
     * @param mi
     * @param server
     * @return null
     * @exception OpenEJBException
     */
    protected static Object remove(MethodInvocation mi, Server server) throws OpenEJBException{
        server.invokeMethod(mi);
        return null;
    }
    
    /**
     * Creates and returns an EjbProxyHandler that will be serialized and sent
     * back to the client.
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return a new instance of EjbProxyHandler
     */
    protected static Object getEJBHome(MethodInvocation mi, String securityToken, Server server){
        EjbProxyHandler handler = new EjbProxyHandler(server.port, server.ip, null,mi.getDeploymentInfo().getDeploymentID(), securityToken);
        Object proxy = null;
        DeploymentInfo di = mi.getDeploymentInfo();
        try{
        proxy = ProxyManager.newProxyInstance(di.getHomeInterface(), handler);
        }catch(IllegalAccessException iae){
            throw new RuntimeException( _messages.format( "ejbObjectProcessor.couldNotCreateIVMProxy", di.getHomeInterface() ) );
        }
        return proxy;
    }
}

