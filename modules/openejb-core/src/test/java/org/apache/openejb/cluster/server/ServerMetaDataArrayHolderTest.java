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

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$ $Date$
 */
public class ServerMetaDataArrayHolderTest extends TestCase {
    private ServerMetaDataArrayHolder holder;
    
    public void testAdd() throws Exception {
        ServerMetaData server1 = new ServerMetaData("node1", "localhost", 1234);
        holder.add(server1);
        
        ServerMetaData[] servers = holder.getServers();
        assertEquals(1, servers.length);
        assertSame(server1, servers[0]);
        
        ServerMetaData server2 = new ServerMetaData("node2", "localhost", 1235);
        holder.add(server2);
        servers = holder.getServers();
        assertEquals(2, servers.length);
        assertSame(server1, servers[0]);
        assertSame(server2, servers[1]);
    }

    public void testRemove() throws Exception {
        ServerMetaData server1 = new ServerMetaData("node1", "localhost", 1234);
        holder.add(server1);
        ServerMetaData server2 = new ServerMetaData("node2", "localhost", 1235);
        holder.add(server2);
        holder.remove(server1);

        ServerMetaData[] servers = holder.getServers();
        assertEquals(1, servers.length);
        assertSame(server2, servers[0]);
    }

    public void testRemoveNode() throws Exception {
        ServerMetaData server1 = new ServerMetaData("node1", "localhost", 1234);
        holder.add(server1);
        ServerMetaData server2 = new ServerMetaData("node2", "localhost", 1235);
        holder.add(server2);
        holder.removeNode(server1.getNodeName());

        ServerMetaData[] servers = holder.getServers();
        assertEquals(1, servers.length);
        assertSame(server2, servers[0]);
    }

    protected void setUp() throws Exception {
        holder = new ServerMetaDataArrayHolder(new ServerMetaData[0]);
    }
    
}
