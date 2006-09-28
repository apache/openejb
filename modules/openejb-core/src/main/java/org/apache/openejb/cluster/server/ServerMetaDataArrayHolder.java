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
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.cluster.server;

import org.apache.openejb.client.ServerMetaData;


/**
 * 
 * @version $Revision$ $Date$
 */
public class ServerMetaDataArrayHolder {
    private ServerMetaData[] servers;
    
    public ServerMetaDataArrayHolder(ServerMetaData[] servers) {
        this.servers = servers;
    }

    public ServerMetaData[] getServers() {
        synchronized (servers) {
            return servers;
        }
    }

    public void add(ServerMetaData server) {
        synchronized (servers) {
            ServerMetaData[] newServers = new ServerMetaData[servers.length + 1];
            System.arraycopy(servers, 0, newServers, 0, servers.length);
            newServers[newServers.length - 1] = server;
            servers = newServers;
        }
    }
    
    public void remove(ServerMetaData server) {
        synchronized (servers) {
            ServerMetaData[] newServers = new ServerMetaData[servers.length];
            int newServersIdx = 0;
            for (int i = 0; i < servers.length; i++) {
                ServerMetaData curServer = servers[i];
                if (false == curServer.equals(server)) {
                    newServers[newServersIdx++] = curServer;
                }
            }
            newServers = resize(newServers, newServersIdx);
            servers = newServers;
        }
    }

    public void removeNode(String nodeName) {
        synchronized (servers) {
            ServerMetaData[] newServers = new ServerMetaData[servers.length];
            int newServersIdx = 0;
            for (int i = 0; i < servers.length; i++) {
                ServerMetaData curServer = servers[i];
                if (false == curServer.getNodeName().equals(nodeName)) {
                    newServers[newServersIdx++] = curServer;
                }
            }
            newServers = resize(newServers, newServersIdx);
            servers = newServers;
        }
    }

    private ServerMetaData[] resize(ServerMetaData[] newServers, int newServersIdx) {
        if (newServersIdx < servers.length) {
            ServerMetaData[] newServers2 = new ServerMetaData[newServersIdx];
            System.arraycopy(newServers, 0, newServers2, 0, newServersIdx);
            newServers = newServers2;
        }
        return newServers;
    }
}
