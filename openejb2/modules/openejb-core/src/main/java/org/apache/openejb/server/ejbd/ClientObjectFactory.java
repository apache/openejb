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
package org.apache.openejb.server.ejbd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.client.EJBHomeHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.proxy.ProxyInfo;


/**
 * The implementation of ApplicationServer used to create all client-side
 * implementations of the javax.ejb.* interaces as
 * 
 */
class ClientObjectFactory implements org.apache.openejb.spi.ApplicationServer {
    private final DeploymentIndex deploymentIndex;
    private static Log log = LogFactory.getLog(ClientObjectFactory.class);
	private static final int PORT;
	private static final String IP;
	
	static {
		int port;
		
		try {
			port = Integer.parseInt(System.getProperty("openejb.server.port", "4201"));
		} catch (NumberFormatException nfe) {
			port = 4201;
			
			log.warn("openejb.server.port [" + 
				System.getProperty("openejb.server.port") + 
				"] is invalid.  Using the default [" + port + "].");
		}
		
		PORT = port;
		IP = System.getProperty("openejb.server.ip", "127.0.0.1");
	}

    protected ServerMetaData[] servers;

    public ClientObjectFactory(DeploymentIndex deploymentIndex) throws Exception {
        this.deploymentIndex = deploymentIndex;
        servers = new ServerMetaData[] {new ServerMetaData("BOOT", ClientObjectFactory.IP, ClientObjectFactory.PORT)};
    }

    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBMetaData(call, info);
    }

    /**
     * Creates a Handle object that can be serialized and 
     * sent to the client.
     * 
     * @param info
     * 
     * @return 
     */
    public javax.ejb.Handle getHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHandle(call, info);
    }

    /**
     * Creates a HomeHandle object that can be serialized and 
     * sent to the client.
     * 
     * @param info
     * 
     * @return 
     */
    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHomeHandle(call, info);
    }

    /**
     * Creates an EJBObject object that can be serialized and 
     * sent to the client.
     * 
     * @param info
     * 
     * @return 
     */
    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBObject(call, info);
    }

    /**
     * Creates an EJBHome object that can be serialized and 
     * sent to the client.
     * 
     * @param info
     * 
     * @return 
     */
    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBHome(call, info);
    }

    /**
     * Creates an EJBMetaDataImpl object that can be serialized and
     * sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.apache.openejb.client.EJBMetaDataImpl
     */
    protected javax.ejb.EJBMetaData _getEJBMetaData(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);
        
        EJBMetaDataImpl metaData = new EJBMetaDataImpl(info.getHomeInterface(),
                info.getRemoteInterface(),
                info.getPrimaryKeyClass(),
                info.getComponentType(),
                info.getContainerID(),
                idCode);
        return metaData;
    }

    private int getContainerId(ProxyInfo info) {
        return deploymentIndex.getDeploymentIndex(info.getContainerID());
    }

    /**
     * Creates an EJBMetaDataImpl object that can be serialized and
     * sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.apache.openejb.client.EJBObjectHandle
     */
    protected javax.ejb.Handle _getHandle(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);
        
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                info.getRemoteInterface(),
                info.getPrimaryKeyClass(),
                info.getComponentType(),
                info.getContainerID(),
                idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,servers,primKey);

        return new EJBObjectHandle( hanlder.createEJBObjectProxy() );
    }

    /**
     * Creates an EJBHomeHandle object that can be serialized and
     * sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.apache.openejb.client.EJBHomeHandle
     */
    protected javax.ejb.HomeHandle _getHomeHandle(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);
        
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                info.getRemoteInterface(),
                info.getPrimaryKeyClass(),
                info.getComponentType(),
                info.getContainerID(),
                idCode);
        
        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,servers);

        return new EJBHomeHandle( hanlder.createEJBHomeProxy() );
    }

    /**
     * Creates an EJBObjectHandler and EJBObject proxy object that can
     * be serialized and sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.apache.openejb.client.EJBObjectHandler
     */
    protected javax.ejb.EJBObject _getEJBObject(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);
        
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                info.getRemoteInterface(),
                info.getPrimaryKeyClass(),
                info.getComponentType(),
                info.getContainerID(),
                idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,servers,primKey);

        return hanlder.createEJBObjectProxy();
    }

    /**
     * Creates an EJBHomeHandler and EJBHome proxy object that can
     * be serialized and sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.apache.openejb.client.EJBHomeHandler
     */
    protected javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);

        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                                                        info.getRemoteInterface(),
                                                        info.getPrimaryKeyClass(),
                                                        info.getComponentType(),
                                                        info.getContainerID(),
                                                        idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,servers);

        //EJBHomeProxyHandle handle = new EJBHomeProxyHandle( hanlder );

        return hanlder.createEJBHomeProxy();
    }
}