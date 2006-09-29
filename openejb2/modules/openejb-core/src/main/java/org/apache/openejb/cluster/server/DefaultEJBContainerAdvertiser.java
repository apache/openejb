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

import javax.jms.ObjectMessage;

import org.codehaus.wadi.gridstate.Dispatcher;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.cluster.server.ClusteredEJBContainerEvent.EventType;

/**
 * 
 * @version $Revision$ $Date$
 */
class DefaultEJBContainerAdvertiser {
    private final ServerMetaData server;
    private final Dispatcher dispatcher;
    
    public DefaultEJBContainerAdvertiser(ServerMetaData server, Dispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    public void advertiseJoin(Object containerId) {
        sendToCluster(containerId, ClusteredEJBContainerEvent.JOIN);
    }
    
    public void advertiseLeave(Object containerId) {
        sendToCluster(containerId, ClusteredEJBContainerEvent.LEAVE);
    }

    private void sendToCluster(Object containerId, EventType type) {
        try {
            ObjectMessage message = dispatcher.createObjectMessage();
            message.setObject(new ClusteredEJBContainerEvent(type, containerId, server));
            dispatcher.send(dispatcher.getClusterDestination(), message);
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
    }
}