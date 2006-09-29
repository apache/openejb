/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


