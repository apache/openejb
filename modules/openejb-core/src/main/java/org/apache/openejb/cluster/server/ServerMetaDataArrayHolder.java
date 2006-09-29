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
