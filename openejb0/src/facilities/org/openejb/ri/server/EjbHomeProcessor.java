
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
  * declared in the javax.ejb.EJBHome interface.
  * 
  * @see javax.ejb.EJBHome
  */
public class EjbHomeProcessor {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    /**
     * Internally processes the getEJBMetaData, getHomeHandle and remove methods 
     * from the EJBHome proxy.
     * 
     * @param mi
     * @param server
     * @return 
     * @exception OpenEJBException
     */
    public static Object processMethod(MethodInvocation mi, Server server) throws OpenEJBException{

        try {
            server.checkAuthorization(mi);
        } catch ( org.openejb.OpenEJBException oje ) {// checkAuthorization exception
            return oje.getRootCause();
        }

        // Process the specific method invoked
        if ( mi.getMethod().getName().equals("getEJBMetaData") )      return getEJBMetaData(mi);                                
        else if ( mi.getMethod().getName().equals("getHomeHandle") )  return getHomeHandle();                                
        else if ( mi.getMethod().getName().equals("remove") )         return remove(mi, server);                                
        
        return null;
    }

    /**
     * Creates and returns the EJB Server specific meta data for the deployed bean.
     * 
     * @param mi
     * @return a new instance of RiMetaData
     */
    protected static Object getEJBMetaData(MethodInvocation mi){
        byte compType = RiMetaData.ENTITY;
        switch ( mi.getDeploymentInfo().getComponentType() ) {
            case DeploymentInfo.STATEFUL:
                compType = RiMetaData.STATEFUL;
                break;
            case DeploymentInfo.STATELESS:
                compType = RiMetaData.STATELESS;
        }
        // component type is identified outside the RiMetaData so that RiMetaData doenst reference DeploymentInfo avoiding the need to load the DeploymentInfo class into the client VM.
        return new RiMetaData(mi.getDeploymentInfo().getHomeInterface(), mi.getDeploymentInfo().getRemoteInterface(),compType);
    }

    /**
     * Creates and returns an EJB Server specific handle to the bean's server
     * specific EJBHome object.
     * 
     * This implementation creates and returns a new instance of RiBaseHandle
     * 
     * @return a new instance of RiBaseHandle
     */
    protected static Object getHomeHandle(){
        return new RiBaseHandle();
    }
    

    /**
     * Invokes the remove method on the OpenEJB container system using the 
     * information in the EJB Server specific javax.ejb.Home implementation or
     * the specific information the server has about the client.
     * 
     * @param mi
     * @param server
     * @return null
     * @exception OpenEJBException
     */
    protected static Object remove(MethodInvocation mi, Server server) throws OpenEJBException{

        Class [] types = mi.getMethod().getParameterTypes();
        
        // EJBHome.remove(Handle handle)
        if ( types[0] == javax.ejb.Handle.class ) {
            
            // Extract the primary key from the handle
            RiBaseHandle handle = (RiBaseHandle)mi.getArguments()[0];

            EjbProxyHandler proxyHandler = (EjbProxyHandler)ProxyManager.getInvocationHandler(handle.getEJBObject());
            Object primKey = proxyHandler.primaryKey;
            mi.setPrimaryKey(primKey);    
            // invoke the remove on the container
            server.invokeMethod(mi);
        
        // EJBHome.remove(Object primaryKey)
        } else {
            byte type = mi.getDeploymentInfo().getComponentType();
            
            if ( type == DeploymentInfo.BMP_ENTITY || type == DeploymentInfo.CMP_ENTITY ) {
                mi.setPrimaryKey(mi.getArguments()[0]);
                server.invokeMethod(mi);
            } else {
                return new RemoteException( _messages.message( "ejbHomeProcessor.invalidOperation" ) );
            }
        }
        return null;
    }
}

