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
 * $Id$
 */
package org.openejb.server.ejbd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openejb.ContainerIndex;
import org.openejb.client.EJBHomeHandle;
import org.openejb.client.EJBHomeHandler;
import org.openejb.client.EJBMetaDataImpl;
import org.openejb.client.EJBObjectHandle;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.ServerMetaData;
import org.openejb.proxy.ProxyInfo;


/**
 * The implementation of ApplicationServer used to create all client-side
 * implementations of the javax.ejb.* interaces as
 * 
 */
class ClientObjectFactory implements org.openejb.spi.ApplicationServer {
    private final ContainerIndex containerIndex;
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

    protected ServerMetaData sMetaData;

    public ClientObjectFactory(ContainerIndex containerIndex) throws Exception {
        this.containerIndex = containerIndex;
        this.sMetaData = new ServerMetaData(ClientObjectFactory.IP, ClientObjectFactory.PORT);
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
     * @see org.openejb.client.EJBMetaDataImpl
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
        return containerIndex.getContainerIndex(info.getContainerID());
    }

    /**
     * Creates an EJBMetaDataImpl object that can be serialized and
     * sent to the client.
     * 
     * @param call
     * @param info
     * 
     * @return 
     * @see org.openejb.client.EJBObjectHandle
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

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,primKey);

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
     * @see org.openejb.client.EJBHomeHandle
     */
    protected javax.ejb.HomeHandle _getHomeHandle(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);
        
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                info.getRemoteInterface(),
                info.getPrimaryKeyClass(),
                info.getComponentType(),
                info.getContainerID(),
                idCode);
        
        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,sMetaData);

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
     * @see org.openejb.client.EJBObjectHandler
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

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,primKey);

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
     * @see org.openejb.client.EJBHomeHandler
     */
    protected javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info) {

        int idCode = getContainerId(info);

        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(info.getHomeInterface(),
                                                        info.getRemoteInterface(),
                                                        info.getPrimaryKeyClass(),
                                                        info.getComponentType(),
                                                        info.getContainerID(),
                                                        idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,sMetaData);

        //EJBHomeProxyHandle handle = new EJBHomeProxyHandle( hanlder );

        return hanlder.createEJBHomeProxy();
    }
}