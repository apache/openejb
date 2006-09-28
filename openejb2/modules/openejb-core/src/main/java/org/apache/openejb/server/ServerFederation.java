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
 * $Id: ServerFederation.java 444667 2004-04-09 19:04:02Z dain $
 */
package org.apache.openejb.server;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.spi.ApplicationServer;

/**
 * This class is passed in as the ApplicationServer implementation
 * when OpenEJB is initialized.  This class allows several application
 * server implementations to be used on the same contianer system
 * 
 * Each one calls setApplicationServer before making a call to OpenEJB.
 * Then, when OpenEJB eventually makes a call to the ApplicationServer
 * implementation, which is this object, we can actually delegate the
 * call to the real application server.
 * 
 * This allows us to have several ApplicationServer implamentations
 * all using the same OpenEJB instance at the same time, whereas we
 * would normally be limited to one.
 * 
 */
public class ServerFederation implements ApplicationServer {
    private static ThreadLocal threadStorage = new ThreadLocal();

    /**
     * Delegates this call to the application server implementation
     * associated with this thread.
     * 
     * @param proxyInfo A proxy info instance describing the deployment
     * 
     * @return 
     */
    public Handle getHandle(ProxyInfo proxyInfo) {
        return getApplicationServer().getHandle(proxyInfo);
    }

    /**
     * Delegates this call to the application server implementation
     * associated with this thread.
     * 
     * @param proxyInfo A proxy info instance describing the deployment
     * 
     * @return 
     */
    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBMetaData(proxyInfo);
    }

    /**
     * Delegates this call to the application server implementation
     * associated with this thread.
     * 
     * @param proxyInfo A proxy info instance describing the deployment
     * 
     * @return 
     */
    public HomeHandle getHomeHandle(ProxyInfo proxyInfo) {
        return getApplicationServer().getHomeHandle(proxyInfo);
    }

    /**
     * Delegates this call to the application server implementation
     * associated with this thread.
     * 
     * @param proxyInfo A proxy info instance describing the deployment
     * 
     * @return 
     */
    public EJBObject getEJBObject(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBObject(proxyInfo);
    }

    /**
     * Delegates this call to the application server implementation
     * associated with this thread.
     * 
     * @param proxyInfo A proxy info instance describing the deployment
     * 
     * @return 
     */
    public EJBHome getEJBHome(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBHome(proxyInfo);
    }


    //-------------------------------------------//
    
    /**
     * Makes the ApplicationServer implementation specified the 
     * one that will be used for all actions on this thread.
     * 
     * @param server
     */
    public static void setApplicationServer(ApplicationServer server) {
        threadStorage.set(server);
    }
    
    /**
     * Gets the ApplicationServer implementation associates
     * with this thread.
     * 
     * @return 
     */
    public static ApplicationServer getApplicationServer( ) {
        Object obj = threadStorage.get();
        //System.out.println("[] Get App Server "+obj);
        return (ApplicationServer)obj;
    }

}


