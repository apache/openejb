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
package org.apache.openejb.client.naming;

import java.net.UnknownHostException;
import java.util.List;
import java.util.ListIterator;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.reference.SimpleReference;
import org.apache.openejb.client.Client;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBHomeProxy;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.RequestInfo;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.ResponseInfo;
import org.apache.openejb.client.ServerMetaData;

/**
 * @version $Revision$ $Date$
 */
public class RemoteEJBReference extends SimpleReference {
	private static final Log log = LogFactory.getLog(RemoteEJBReference.class);
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

    private String containerId;
    private List servers;

    public RemoteEJBReference() {
    }

    public RemoteEJBReference(String containerId) {
        this.containerId = containerId;
    }

    public RemoteEJBReference(String containerId, List servers) {
        this.containerId = containerId;
        this.servers = servers;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public List getServers() {
        return servers;
    }

    public void setServers(List servers) {
        this.servers = servers;
    }

    public Object getContent() throws NamingException {
        ServerMetaData[] servers;
        if (this.servers != null) {
            servers = (ServerMetaData[]) this.servers.toArray(new ServerMetaData[this.servers.size() + 1]);
            for (ListIterator iterator = this.servers.listIterator(); iterator.hasNext();) {
                ServerMetaData serverMetaData = (ServerMetaData) iterator.next();
                int index = iterator.previousIndex();
                servers[index] = serverMetaData;
            }
        } else {
            servers = new ServerMetaData[1];
        }

        try {
            servers[servers.length - 1] = new ServerMetaData("BOOT", IP, PORT);
        } catch (UnknownHostException e) {
            throw new NamingException(e.getMessage());
        }

        JNDIRequest req = new JNDIRequest(JNDIRequest.JNDI_LOOKUP, containerId);

        ResponseInfo resInfo = new ResponseInfo(new JNDIResponse());
        try{
            Client.request(new RequestInfo(req, servers), resInfo);
        } catch (Exception e){
            throw (NamingException)new NamingException("Cannot lookup " + containerId).initCause(e);
        }
        
        JNDIResponse res = (JNDIResponse) resInfo.getResponse();
        switch ( res.getResponseCode() ) {
            case ResponseCodes.JNDI_EJBHOME:
                // Construct a new handler and proxy.
                EJBMetaDataImpl ejb = (EJBMetaDataImpl)res.getResult();
                ServerMetaData[] newServers = resInfo.getServers();
                EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejb, newServers);
                EJBHomeProxy proxy = handler.createEJBHomeProxy();
                ejb.setEJBHomeProxy(proxy);
                return proxy;

            case ResponseCodes.JNDI_NOT_FOUND:
                throw new NameNotFoundException(containerId + " not found");

            case ResponseCodes.JNDI_NAMING_EXCEPTION:
                throw (NamingException) res.getResult();

            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
                throw (RuntimeException) res.getResult();

            case ResponseCodes.JNDI_ERROR:
                throw (Error) res.getResult();

            default:
                throw new RuntimeException("Invalid response from server :"+res.getResponseCode());
        }
    }
}
