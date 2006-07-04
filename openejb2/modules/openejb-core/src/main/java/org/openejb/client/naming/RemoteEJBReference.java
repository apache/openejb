/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.client.naming;

import java.net.UnknownHostException;
import java.util.List;
import java.util.ListIterator;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.reference.SimpleReference;
import org.openejb.client.Client;
import org.openejb.client.EJBHomeHandler;
import org.openejb.client.EJBHomeProxy;
import org.openejb.client.EJBMetaDataImpl;
import org.openejb.client.JNDIRequest;
import org.openejb.client.JNDIResponse;
import org.openejb.client.RequestInfo;
import org.openejb.client.ResponseCodes;
import org.openejb.client.ResponseInfo;
import org.openejb.client.ServerMetaData;

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
