
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
import java.util.Vector;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.util.ArrayEnumeration;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * Represents the EJB Server's responsibility in handling the finder and create 
 * methods that are declared in the bean's home interface.
 * 
 * @see javax.ejb.EJBHome
 */
public class EjbHomeIntfcProcessor {

    /**
     * Internally processes the finder and create methods from the bean's home 
     * interface.
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    public static Object processMethod(MethodInvocation mi, String securityToken, Server server) throws OpenEJBException {

        if ( mi.getMethod().getName().equals("create") )        return create(mi, securityToken, server);
        else if ( mi.getMethod().getName().startsWith("find") ) return find(mi, securityToken, server);

        return server.invokeMethod(mi);
    }

    /**
     * Invokes the finder method on the OpenEJB container system and then generates
     * the appropriate EJB Server specific EJBObjects and EJBHomes to return to the 
     * client.
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    protected static Object find(MethodInvocation mi, String securityToken, Server server) throws OpenEJBException {
        Object retValue = server.invokeMethod(mi);
        Class proxyClass = null;
        DeploymentInfo di = mi.getDeploymentInfo();

        if ( retValue instanceof java.util.Collection ) {

            Object [] elements = ((java.util.Collection)retValue).toArray();
            Vector proxies = new Vector();
            for ( int i = 0; i < elements.length; i++ ) {
                proxies.addElement( createProxy(elements[i], di.getRemoteInterface(), securityToken, server, new javax.ejb.FinderException()) );
            }

            if ( mi.getMethod().getReturnType() == java.util.Enumeration.class ) {
                return new ArrayEnumeration(proxies);
            } else
                return proxies;// vector is a type of Collection.


        } else {
            return createProxy(retValue, di.getRemoteInterface(), securityToken, server, new javax.ejb.FinderException()); 
        }
    }

    /**
     * Invokes the create method on the OpenEJB container system and then generates
     * the appropriate EJB Server specific EJBObjects to return to the
     * client.
     * 
     * @param mi
     * @param securityToken
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    protected static Object create( MethodInvocation mi, String securityToken, Server server) throws OpenEJBException{
        Object retValue = server.invokeMethod(mi);
        Exception exception = new RemoteException();
        DeploymentInfo di = mi.getDeploymentInfo();
        return createProxy(retValue,di.getRemoteInterface(), securityToken, server, exception);
    }

    /**
     * Utility method to aid this EJB Server implementation in creating EjbProxyHandlers 
     * to represent the EJBObject and EJBHome to the client.
     * 
     * @param retValue
     * @param proxyClass
     * @param securityToken
     * @param server
     * @param exception
     * @return 
     */
    private static Object createProxy(Object retValue, Class interfce, String securityToken, Server server, Exception exception) {
        ProxyInfo pInfo = (ProxyInfo)retValue;
        try{
            InvocationHandler handler = new EjbProxyHandler(server.port, server.ip, pInfo.getPrimaryKey(),pInfo.getDeploymentInfo().getDeploymentID(), securityToken);
            return ProxyManager.newProxyInstance(interfce, handler);
        } catch (Exception e){
            return exception;
        }
    }
}

